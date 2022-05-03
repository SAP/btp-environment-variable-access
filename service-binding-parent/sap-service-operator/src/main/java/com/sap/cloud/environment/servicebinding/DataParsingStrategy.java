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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sap.cloud.environment.api.DefaultServiceBinding;
import com.sap.cloud.environment.api.ServiceBinding;

public final class DataParsingStrategy implements ParsingStrategy
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
    private static final String DOMAINS_KEY = "domains";

    @Nonnull
    private static final Map<String, PropertySetter> DEFAULT_PROPERTY_SETTERS;

    @Nonnull
    private static final PropertySetter DEFAULT_FALLBACK_PROPERTY_SETTER = PropertySetter.TO_CREDENTIALS;

    static {
        final Map<String, PropertySetter> defaultPropertySetters = new HashMap<>();
        defaultPropertySetters.put(PLAN_KEY, PropertySetter.TO_ROOT);
        defaultPropertySetters.put(INSTANCE_GUID_KEY, PropertySetter.TO_ROOT);
        defaultPropertySetters.put(INSTANCE_NAME_KEY, PropertySetter.TO_ROOT);
        defaultPropertySetters.put(LABEL_KEY, PropertySetter.TO_ROOT);
        defaultPropertySetters.put(TAGS_KEY, PropertySetter.asList(PropertySetter.TO_ROOT));
        defaultPropertySetters.put(DOMAINS_KEY, PropertySetter.asList(PropertySetter.TO_CREDENTIALS));

        DEFAULT_PROPERTY_SETTERS = Collections.unmodifiableMap(defaultPropertySetters);
    }

    @Nonnull
    private final Charset charset;
    @Nonnull
    private final Map<String, PropertySetter> propertySetters;
    @Nonnull
    private final PropertySetter fallbackPropertySetter;

    private DataParsingStrategy( @Nonnull final Charset charset,
                                 @Nonnull final Map<String, PropertySetter> propertySetters,
                                 @Nonnull final PropertySetter fallbackPropertySetter )
    {
        this.charset = charset;
        this.propertySetters = propertySetters;
        this.fallbackPropertySetter = fallbackPropertySetter;
    }

    @Nonnull
    public static DataParsingStrategy newDefault()
    {
        return new DataParsingStrategy(StandardCharsets.UTF_8,
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
            // service binding directory must contain at least one file
            return Optional.empty();
        }

        final Map<String, Object> rawServiceBinding = new HashMap<>();
        for (final Path propertyFile : propertyFiles) {
            final String propertyName = propertyFile.getFileName().toString();
            final String fileContent = String.join("\n", Files.readAllLines(propertyFile, charset));
            if (fileContent.isEmpty()) {
                continue;
            }

            try {
                final JSONObject jsonContent = new JSONObject(fileContent);

                // service binding must not contain a valid JSON object
                return Optional.empty();
            } catch (final JSONException e) {
                // ignore
            }

            getPropertySetter(propertyName).setProperty(rawServiceBinding, propertyName, fileContent);
        }

        if (rawServiceBinding.get(PropertySetter.CREDENTIALS_KEY) == null) {
            // service bindings must contain credentials
            return Optional.empty();
        }

        final DefaultServiceBinding serviceBinding = DefaultServiceBinding.builder()
                                                                          .copy(rawServiceBinding)
                                                                          .withName(bindingName)
                                                                          .withServiceName(serviceName)
                                                                          .withServicePlanKey(PLAN_KEY)
                                                                          .withTagsKey(TAGS_KEY)
                                                                          .withCredentialsKey(PropertySetter.CREDENTIALS_KEY)
                                                                          .build();
        return Optional.of(serviceBinding);
    }

    @Nonnull
    private PropertySetter getPropertySetter( @Nonnull final String propertyName )
    {
        return propertySetters.getOrDefault(propertyName, fallbackPropertySetter);
    }
}
