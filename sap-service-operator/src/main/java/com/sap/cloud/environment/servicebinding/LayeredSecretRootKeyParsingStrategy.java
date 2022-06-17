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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sap.cloud.environment.servicebinding.api.DefaultServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

public final class LayeredSecretRootKeyParsingStrategy implements LayeredParsingStrategy
{
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
    private static final Map<String, LayeredPropertySetter> DEFAULT_PROPERTY_SETTERS;

    @Nonnull
    private static final LayeredPropertySetter DEFAULT_FALLBACK_PROPERTY_SETTER = LayeredPropertySetter.TO_CREDENTIALS;

    static {
        final Map<String, LayeredPropertySetter> defaultPropertySetters = new HashMap<>();
        defaultPropertySetters.put(PLAN_KEY, LayeredPropertySetter.TO_ROOT);
        defaultPropertySetters.put(INSTANCE_GUID_KEY, LayeredPropertySetter.TO_ROOT);
        defaultPropertySetters.put(INSTANCE_NAME_KEY, LayeredPropertySetter.TO_ROOT);
        defaultPropertySetters.put(LABEL_KEY, LayeredPropertySetter.TO_ROOT);
        defaultPropertySetters.put(TAGS_KEY, LayeredPropertySetter.TO_ROOT);

        DEFAULT_PROPERTY_SETTERS = Collections.unmodifiableMap(defaultPropertySetters);
    }

    @Nonnull
    private final Charset charset;
    @Nonnull
    private final Map<String, LayeredPropertySetter> propertySetters;
    @Nonnull
    private final LayeredPropertySetter fallbackPropertySetter;

    private LayeredSecretRootKeyParsingStrategy( @Nonnull final Charset charset,
                                                 @Nonnull final Map<String, LayeredPropertySetter> propertySetters,
                                                 @Nonnull final LayeredPropertySetter fallbackPropertySetter )
    {
        this.charset = charset;
        this.propertySetters = propertySetters;
        this.fallbackPropertySetter = fallbackPropertySetter;
    }

    @Nonnull
    public static LayeredSecretRootKeyParsingStrategy newDefault()
    {
        return new LayeredSecretRootKeyParsingStrategy(StandardCharsets.UTF_8,
                                                       DEFAULT_PROPERTY_SETTERS,
                                                       DEFAULT_FALLBACK_PROPERTY_SETTER);
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
            // service binding directory must contain exactly one file
            return Optional.empty();
        }

        if (propertyFiles.size() > 1) {
            // service binding directory must contain exactly one file
            return Optional.empty();
        }

        final String fileContent = String.join("\n", Files.readAllLines(propertyFiles.get(0), charset));

        if (fileContent.isEmpty()) {
            return Optional.empty();
        }

        @Nullable
        final Map<String, Object> parsedFileContent;

        try {
            parsedFileContent = new JSONObject(fileContent).toMap();
        } catch (final JSONException e) {
            return Optional.empty();
        }

        if (parsedFileContent == null) {
            // the property file must contain a Json structure
            return Optional.empty();
        }

        final Map<String, Object> rawServiceBinding = new HashMap<>();
        for (final Map.Entry<String, Object> property : parsedFileContent.entrySet()) {
            final String name = property.getKey();
            final Object value = property.getValue();

            if (name == null || value == null) {
                continue;
            }

            getPropertySetter(name).setProperty(rawServiceBinding, name, value);
        }

        final DefaultServiceBinding serviceBinding = DefaultServiceBinding.builder()
                                                                          .copy(rawServiceBinding)
                                                                          .withName(bindingName)
                                                                          .withServiceName(serviceName)
                                                                          .withServicePlanKey(PLAN_KEY)
                                                                          .withTagsKey(TAGS_KEY)
                                                                          .withCredentialsKey(LayeredPropertySetter.CREDENTIALS_KEY)
                                                                          .build();
        return Optional.of(serviceBinding);
    }

    @Nonnull
    private LayeredPropertySetter getPropertySetter( @Nonnull final String propertyName )
    {
        return propertySetters.getOrDefault(propertyName, fallbackPropertySetter);
    }
}