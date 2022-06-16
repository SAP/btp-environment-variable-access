/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
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

import com.sap.cloud.environment.servicebinding.api.DefaultServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

public final class LayeredSecretKeyParsingStrategy implements LayeredParsingStrategy
{
    @Nonnull
    private static final String PLAN_KEY = "plan";

    @Nonnull
    private final Charset charset;

    private LayeredSecretKeyParsingStrategy( @Nonnull final Charset charset )
    {
        this.charset = charset;
    }

    @Nonnull
    public static LayeredSecretKeyParsingStrategy newDefault()
    {
        return new LayeredSecretKeyParsingStrategy(StandardCharsets.UTF_8);
    }

    @Nonnull
    @Override
    public Optional<ServiceBinding> parse( @Nonnull final String serviceName,
                                           @Nonnull final String bindingName,
                                           @Nonnull final Path bindingPath ) throws IOException
    {
        final List<Path> propertyFiles = Files.list(bindingPath)
                                              .filter(Files::isRegularFile)
                                              .collect(Collectors.toList());

        if (propertyFiles.isEmpty()) {
            // service binding directory must contain at least one json file
            return Optional.empty();
        }

        final Map<String, Object> rawServiceBinding = new HashMap<>();
        boolean credentialsFound = false;
        for (final Path propertyFile : propertyFiles) {
            final String propertyName = propertyFile.getFileName().toString();
            final String fileContent = String.join("\n", Files.readAllLines(propertyFile, charset));
            if (fileContent.isEmpty()) {
                continue;
            }

            try {
                final Map<String, Object> parsedCredentials = new JSONObject(fileContent).toMap();

                if (credentialsFound) {
                    // we expect exactly one valid json object in this service binding
                    return Optional.empty();
                }

                credentialsFound = true;
                rawServiceBinding.put(LayeredPropertySetter.CREDENTIALS_KEY, parsedCredentials);
            } catch (final JSONException e) {
                // property is not a valid json object --> it cannot be the credentials object
                rawServiceBinding.put(propertyName, fileContent);
            }
        }

        if (!credentialsFound) {
            // the service binding is expected to have credentials attached to it
            return Optional.empty();
        }

        final DefaultServiceBinding serviceBinding = DefaultServiceBinding.builder()
                                                                          .copy(rawServiceBinding)
                                                                          .withName(bindingName)
                                                                          .withServiceName(serviceName)
                                                                          .withServicePlanKey(PLAN_KEY)
                                                                          .withCredentialsKey(LayeredPropertySetter.CREDENTIALS_KEY)
                                                                          .build();
        return Optional.of(serviceBinding);
    }
}
