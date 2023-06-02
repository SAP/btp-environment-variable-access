/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.environment.servicebinding.api.DefaultServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

/**
 * A {@link LayeredParsingStrategy} that expects <b>exactly one</b> JSON file that contains the credentials. All other
 * properties (i.e. metadata) are expected to be contained in their own files.
 */
public final class LayeredSecretKeyParsingStrategy implements LayeredParsingStrategy
{
    @Nonnull
    private static final Logger logger = LoggerFactory.getLogger(LayeredSecretKeyParsingStrategy.class);

    @Nonnull
    private static final String PLAN_KEY = "plan";

    @Nonnull
    private final Charset charset;

    private LayeredSecretKeyParsingStrategy( @Nonnull final Charset charset )
    {
        this.charset = charset;
    }

    /**
     * Initializes a new {@link LayeredSecretKeyParsingStrategy} instance using the default configuration.
     *
     * @return A new {@link LayeredSecretKeyParsingStrategy} instance with default configuration.
     */
    @Nonnull
    public static LayeredSecretKeyParsingStrategy newDefault()
    {
        return new LayeredSecretKeyParsingStrategy(StandardCharsets.UTF_8);
    }

    @Nonnull
    @Override
    public
        Optional<ServiceBinding>
        parse( @Nonnull final String serviceName, @Nonnull final String bindingName, @Nonnull final Path bindingPath )
            throws IOException
    {
        logger.debug("Trying to read service binding from '{}'.", bindingPath);

        final List<Path> propertyFiles =
            Files.list(bindingPath).filter(Files::isRegularFile).collect(Collectors.toList());

        if( propertyFiles.isEmpty() ) {
            // service binding directory must contain at least one json file
            logger.debug("Skipping '{}': The directory is empty.", bindingPath);
            return Optional.empty();
        }

        final Map<String, Object> rawServiceBinding = new HashMap<>();
        boolean credentialsFound = false;
        for( final Path propertyFile : propertyFiles ) {
            final String propertyName = propertyFile.getFileName().toString();
            final String fileContent = String.join("\n", Files.readAllLines(propertyFile, charset));
            if( fileContent.isEmpty() ) {
                logger.debug("Ignoring empty property file '{}'.", propertyFile);
                continue;
            }

            try {
                final Map<String, Object> parsedCredentials = new JSONObject(fileContent).toMap();

                if( credentialsFound ) {
                    // we expect exactly one valid json object in this service binding
                    logger.debug("Skipping '{}': More than one JSON file found.", bindingPath);
                    return Optional.empty();
                }

                credentialsFound = true;
                rawServiceBinding.put(LayeredPropertySetter.CREDENTIALS_KEY, parsedCredentials);
            }
            catch( final JSONException e ) {
                // property is not a valid json object --> it cannot be the credentials object
                rawServiceBinding.put(propertyName, fileContent);
            }
        }

        if( !credentialsFound ) {
            // the service binding is expected to have credentials attached to it
            logger.debug("Skipping '{}': No credentials property found.", bindingPath);
            return Optional.empty();
        }

        final DefaultServiceBinding serviceBinding =
            DefaultServiceBinding
                .builder()
                .copy(rawServiceBinding)
                .withName(bindingName)
                .withServiceName(serviceName)
                .withServicePlanKey(PLAN_KEY)
                .withCredentialsKey(LayeredPropertySetter.CREDENTIALS_KEY)
                .build();
        logger.debug("Successfully read service binding from '{}'.", bindingPath);
        return Optional.of(serviceBinding);
    }
}
