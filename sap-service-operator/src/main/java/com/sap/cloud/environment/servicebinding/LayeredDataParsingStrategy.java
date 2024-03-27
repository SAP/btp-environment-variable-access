package com.sap.cloud.environment.servicebinding;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
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
 * A {@link LayeredParsingStrategy} that expects all property files to contain "plain" text (i.e. no JSON structures).
 * Credentials and metadata of the {@link ServiceBinding} are distinguished by their name - in contrast to the
 * {@link LayeredSecretKeyParsingStrategy} where credentials and metadata are distinguished by their structure.
 */
public final class LayeredDataParsingStrategy implements LayeredParsingStrategy
{
    @Nonnull
    private static final Logger logger = LoggerFactory.getLogger(LayeredDataParsingStrategy.class);

    @Nonnull
    private static final String PLAN_KEY = "plan";

    @Nonnull
    private static final String INSTANCE_GUID_KEY = "instance_guid";

    @Nonnull
    private static final String INSTANCE_NAME_KEY = "instance_name";

    @Nonnull
    private static final String LABEL_KEY = "label";

    @Nonnull
    private static final String TAGS_KEY = "tags";

    @Nonnull
    private static final String DOMAINS_KEY = "domains";

    @Nonnull
    private static final Map<String, LayeredPropertySetter> DEFAULT_PROPERTY_SETTERS;

    @Nonnull
    private static final LayeredPropertySetter DEFAULT_FALLBACK_PROPERTY_SETTER = LayeredPropertySetter.TO_CREDENTIALS;

    static {
        final Map<String, LayeredPropertySetter> defaultPropertySetters = new HashMap<>();
        defaultPropertySetters.put(PLAN_KEY, LayeredPropertySetter.TO_ROOT);
        defaultPropertySetters.put(INSTANCE_GUID_KEY, LayeredPropertySetter.TO_ROOT);
        defaultPropertySetters.put(INSTANCE_NAME_KEY, LayeredPropertySetter.TO_ROOT);
        defaultPropertySetters.put(LABEL_KEY, LayeredPropertySetter.TO_ROOT);
        defaultPropertySetters.put(TAGS_KEY, LayeredPropertySetter.asList(LayeredPropertySetter.TO_ROOT));
        defaultPropertySetters.put(DOMAINS_KEY, LayeredPropertySetter.asList(LayeredPropertySetter.TO_CREDENTIALS));

        DEFAULT_PROPERTY_SETTERS = Collections.unmodifiableMap(defaultPropertySetters);
    }

    @Nonnull
    private final Charset charset;

    @Nonnull
    private final Map<String, LayeredPropertySetter> propertySetters;

    @Nonnull
    private final LayeredPropertySetter fallbackPropertySetter;

    private LayeredDataParsingStrategy(
        @Nonnull final Charset charset,
        @Nonnull final Map<String, LayeredPropertySetter> propertySetters,
        @Nonnull final LayeredPropertySetter fallbackPropertySetter )
    {
        this.charset = charset;
        this.propertySetters = propertySetters;
        this.fallbackPropertySetter = fallbackPropertySetter;
    }

    /**
     * Initializes a new {@link LayeredDataParsingStrategy} instance using the default configuration.
     *
     * @return A new {@link LayeredDataParsingStrategy} instance with default configuration.
     */
    @Nonnull
    public static LayeredDataParsingStrategy newDefault()
    {
        return new LayeredDataParsingStrategy(
            StandardCharsets.UTF_8,
            DEFAULT_PROPERTY_SETTERS,
            DEFAULT_FALLBACK_PROPERTY_SETTER);
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
            // service binding directory must contain at least one file
            logger.debug("Skipping '{}': The directory is empty.", bindingPath);
            return Optional.empty();
        }

        final Map<String, Object> rawServiceBinding = new HashMap<>();
        for( final Path propertyFile : propertyFiles ) {
            final String propertyName = propertyFile.getFileName().toString();
            final String fileContent = String.join("\n", Files.readAllLines(propertyFile, charset));
            if( fileContent.isEmpty() ) {
                logger.debug("Ignoring empty property file '{}'.", propertyFile);
                continue;
            }

            try {
                final JSONObject jsonContent = new JSONObject(fileContent);

                logger
                    .debug(
                        "Skipping '{}': The directory contains an unexpected JSON file ('{}').",
                        bindingPath,
                        propertyFile);
                // service binding must not contain a valid JSON object
                return Optional.empty();
            }
            catch( final JSONException e ) {
                // ignore
            }

            getPropertySetter(propertyName).setProperty(rawServiceBinding, propertyName, fileContent);
        }

        if( rawServiceBinding.get(LayeredPropertySetter.CREDENTIALS_KEY) == null ) {
            // service bindings must contain credentials
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
                .withTagsKey(TAGS_KEY)
                .withCredentialsKey(LayeredPropertySetter.CREDENTIALS_KEY)
                .build();
        logger.debug("Successfully read service binding from '{}'.", bindingPath);
        return Optional.of(serviceBinding);
    }

    @Nonnull
    private LayeredPropertySetter getPropertySetter( @Nonnull final String propertyName )
    {
        return propertySetters.getOrDefault(propertyName, fallbackPropertySetter);
    }
}
