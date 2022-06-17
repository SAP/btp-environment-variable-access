/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sap.cloud.environment.servicebinding.api.DefaultServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBindingAccessor;
import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;
import com.sap.cloud.environment.servicebinding.metadata.BindingMetadata;
import com.sap.cloud.environment.servicebinding.metadata.BindingMetadataFactory;
import com.sap.cloud.environment.servicebinding.metadata.BindingProperty;

public class SapServiceOperatorServiceBindingIoAccessor implements ServiceBindingAccessor
{
    @Nonnull
    public static final Function<String, String> DEFAULT_ENVIRONMENT_VARIABLE_READER = System::getenv;

    @Nonnull
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    @Nonnull
    private static final String ROOT_DIRECTORY_KEY = "SERVICE_BINDING_ROOT";

    @Nonnull
    private static final String METADATA_FILE = ".metadata";

    @Nonnull
    private static final String SERVICE_NAME_KEY = "type";

    @Nonnull
    private static final String TAGS_KEY = "tags";

    @Nonnull
    private static final String PLAN_KEY = "plan";

    @Nonnull
    private final Function<String, String> environmentVariableReader;
    @Nonnull
    private final Charset charset;

    public SapServiceOperatorServiceBindingIoAccessor()
    {
        this(DEFAULT_ENVIRONMENT_VARIABLE_READER, DEFAULT_CHARSET);
    }

    public SapServiceOperatorServiceBindingIoAccessor( @Nonnull final Function<String, String> environmentVariableReader,
                                                       @Nonnull final Charset charset )
    {
        this.environmentVariableReader = environmentVariableReader;
        this.charset = charset;
    }

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings() throws ServiceBindingAccessException
    {
        final Path rootDirectory = getRootDirectory();
        if (rootDirectory == null) {
            return Collections.emptyList();
        }

        try (final Stream<Path> bindingRoots = Files.list(rootDirectory).filter(Files::isDirectory)) {
            return bindingRoots.map(this::parseServiceBinding)
                               .filter(Optional::isPresent)
                               .map(Optional::get)
                               .collect(Collectors.toList());
        } catch (final IOException e) {
            return Collections.emptyList();
        }
    }

    @Nullable
    private Path getRootDirectory()
    {
        final String maybeRootDirectory = environmentVariableReader.apply(ROOT_DIRECTORY_KEY);
        if (maybeRootDirectory == null || maybeRootDirectory.isEmpty()) {
            return null;
        }

        final Path rootDirectory = Paths.get(maybeRootDirectory);
        if (!Files.exists(rootDirectory) || !Files.isDirectory(rootDirectory)) {
            return null;
        }

        return rootDirectory;
    }

    @Nonnull
    private Optional<ServiceBinding> parseServiceBinding( @Nonnull final Path rootDirectory )
    {
        final Path metadataFile = rootDirectory.resolve(METADATA_FILE);
        if (!Files.exists(metadataFile) || !Files.isRegularFile(metadataFile)) {
            // every service binding must contain a metadata file
            return Optional.empty();
        }

        final Optional<BindingMetadata> maybeBindingMetadata = BindingMetadataFactory.tryFromJsonFile(metadataFile);
        if (!maybeBindingMetadata.isPresent()) {
            // metadata file cannot be parsed
            return Optional.empty();
        }

        final String bindingName = rootDirectory.getFileName().toString();

        final BindingMetadata bindingMetadata = maybeBindingMetadata.get();

        final Map<String, Object> rawServiceBinding = new HashMap<>();
        for (final BindingProperty metadataProperty : bindingMetadata.getMetadataProperties()) {
            addProperty(rawServiceBinding, rootDirectory, metadataProperty);
        }

        final Optional<String> maybeServiceName = getServiceName(rawServiceBinding);
        if (!maybeServiceName.isPresent()) {
            // the service name property is mandatory
            return Optional.empty();
        }

        final Map<String, Object> rawCredentials = new HashMap<>();
        for (final BindingProperty credentialProperty : bindingMetadata.getCredentialProperties()) {
            addProperty(rawCredentials, rootDirectory, credentialProperty);
        }
        if (rawCredentials.isEmpty()) {
            // bindings must always have credentials
            return Optional.empty();
        }

        final String credentialsKey = generateNewKey(rawServiceBinding);
        rawServiceBinding.put(credentialsKey, rawCredentials);

        final DefaultServiceBinding serviceBinding = DefaultServiceBinding.builder()
                                                                          .copy(rawServiceBinding)
                                                                          .withName(bindingName)
                                                                          .withServiceName(maybeServiceName.get())
                                                                          .withTagsKey(TAGS_KEY)
                                                                          .withServicePlanKey(PLAN_KEY)
                                                                          .withCredentialsKey(credentialsKey)
                                                                          .build();

        return Optional.of(serviceBinding);
    }

    private void addProperty( @Nonnull final Map<String, Object> properties,
                              @Nonnull final Path rootDirectory,
                              @Nonnull final BindingProperty property )
    {
        final Optional<String> maybeValue = getPropertyFilePath(rootDirectory, property).flatMap(this::readFile);
        if (!maybeValue.isPresent()) {
            return;
        }

        final String value = maybeValue.get();

        switch (property.getFormat()) {
            case TEXT: {
                addTextProperty(properties, property, value);
                break;
            }
            case JSON: {
                addJsonProperty(properties, property, value);
                break;
            }
            default: {
                throw new IllegalStateException(String.format("The format '%s' is currently not supported",
                                                              property.getFormat()));
            }
        }
    }

    private void addTextProperty( @Nonnull final Map<String, Object> properties,
                                  @Nonnull final BindingProperty property,
                                  @Nonnull final String propertyValue )
    {
        properties.put(property.getName(), propertyValue);
    }

    private void addJsonProperty( @Nonnull final Map<String, Object> properties,
                                  @Nonnull final BindingProperty property,
                                  @Nonnull final String propertyValue )
    {
        // Wrap the property value inside another JSON object.
        // This way we don't have to manually take care of correctly parsing the property type
        // (it could be an integer, a boolean, a list, or even an entire JSON object).
        // Instead, we can delegate the parsing logic to our JSON library.
        final String jsonWrapper = String.format("{\"content\": %s}", propertyValue);

        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonWrapper);
        } catch (final JSONException e) {
            return;
        }

        if (!property.isContainer()) {
            // property is not a container, so the content should be attached as a flat value
            properties.put(property.getName(), jsonObject.get("content"));
            return;
        }

        // the property is a container, so we need to unpack it
        final JSONObject content = jsonObject.optJSONObject("content");
        if (content == null) {
            // the provided value is not a JSON object
            return;
        }

        for (final String key : content.keySet()) {
            properties.put(key, content.get(key));
        }
    }

    @Nonnull
    private Optional<Path> getPropertyFilePath( @Nonnull final Path rootDirectory,
                                                @Nonnull final BindingProperty property )
    {
        final Path propertyFile = rootDirectory.resolve(property.getSourceName());
        if (!Files.exists(propertyFile) || !Files.isRegularFile(propertyFile)) {
            return Optional.empty();
        }

        return Optional.of(propertyFile);
    }

    @Nonnull
    private Optional<String> readFile( @Nonnull final Path path )
    {
        try {
            return Optional.of(String.join("\n", Files.readAllLines(path, charset)));
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    private Optional<String> getServiceName( @Nonnull final Map<String, Object> rawServiceBinding )
    {
        final Object maybeValue = rawServiceBinding.get(SERVICE_NAME_KEY);
        if (!(maybeValue instanceof String)) {
            return Optional.empty();
        }

        return Optional.of((String) maybeValue);
    }

    @Nonnull
    private String generateNewKey( @Nonnull final Map<String, Object> map )
    {
        for (int i = 0; i < 100; ++i) {
            final String key = UUID.randomUUID().toString();
            if (map.containsKey(key)) {
                continue;
            }

            return key;
        }

        throw new IllegalStateException("Unable to generate a new random key. This should never happen!");
    }
}
