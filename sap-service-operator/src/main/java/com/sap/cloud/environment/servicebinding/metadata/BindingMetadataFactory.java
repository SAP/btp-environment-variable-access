/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.metadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public final class BindingMetadataFactory
{
    private BindingMetadataFactory()
    {
        throw new AssertionError("This utility class must not be instantiated");
    }

    @Nonnull
    public static Optional<BindingMetadata> tryFromJsonFile( @Nonnull final Path filePath )
    {
        try {
            return Optional.of(getFromJsonFile(filePath));
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    @Nonnull
    public static Optional<BindingMetadata> tryFromJson( @Nonnull final String jsonMetadata )
    {
        try {
            return Optional.of(getFromJson(jsonMetadata));
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    @Nonnull
    public static BindingMetadata getFromJsonFile( @Nonnull final Path filePath )
    {
        try {
            final String fileContent = String.join("\n", Files.readAllLines(filePath));
            return getFromJson(fileContent);
        } catch (final IOException e) {
            throw new IllegalStateException(String.format("Unable to read the file content of '%s'",
                                                          filePath.getFileName()), e);
        }
    }

    @Nonnull
    public static BindingMetadata getFromJson( @Nonnull final String jsonMetadata )
    {
        try {
            return getFromJson(new JSONObject(jsonMetadata));
        } catch (final JSONException e) {
            throw new IllegalArgumentException("The given metadata must be a valid JSON object.", e);
        }
    }

    @Nonnull
    public static BindingMetadata getFromJson( @Nonnull final JSONObject jsonMetadata )
    {
        final Collection<BindingProperty> metadataProperties = readProperties(jsonMetadata.optJSONArray(
                "metaDataProperties"));
        final Collection<BindingProperty> credentialProperties = readProperties(jsonMetadata.optJSONArray(
                "credentialProperties"));

        return new BindingMetadata(metadataProperties, credentialProperties);
    }

    @Nonnull
    private static Collection<BindingProperty> readProperties( @Nullable final JSONArray jsonProperties )
    {
        if (jsonProperties == null) {
            return Collections.emptyList();
        }

        final Collection<BindingProperty> properties = new ArrayList<>(jsonProperties.length());
        for (int i = 0; i < jsonProperties.length(); ++i) {
            final JSONObject jsonMetadataProperty = jsonProperties.optJSONObject(i);
            if (jsonMetadataProperty == null) {
                continue;
            }

            final String name = readNameField(jsonMetadataProperty);
            final String sourceName = readSourceNameField(jsonMetadataProperty).orElse(name);
            final PropertyFormat format = readFormatField(jsonMetadataProperty);
            final boolean isContainer = readContainerField(jsonMetadataProperty);

            if (!isValidName(name) || !isValidSourceName(sourceName) || !isValidFormat(format, isContainer)) {
                continue;
            }

            properties.add(new BindingProperty(name, sourceName, format, isContainer));
        }

        return properties;
    }

    @Nullable
    private static String readNameField( @Nonnull final JSONObject jsonProperty )
    {
        return jsonProperty.optString("name", null);
    }

    @Nullable
    private static PropertyFormat readFormatField( @Nonnull final JSONObject jsonProperty )
    {
        final String rawFormat = jsonProperty.optString("format", null);
        if (rawFormat == null) {
            return null;
        }

        return Arrays.stream(PropertyFormat.values())
                     .filter(format -> format.getValue().equalsIgnoreCase(rawFormat))
                     .findFirst()
                     .orElse(null);
    }

    @Nonnull
    private static Optional<String> readSourceNameField( @Nonnull final JSONObject jsonProperty )
    {
        return Optional.ofNullable(jsonProperty.optString("sourceName", null));
    }

    private static boolean readContainerField( @Nonnull final JSONObject jsonProperty )
    {
        return jsonProperty.optBoolean("container", false);
    }

    private static boolean isValidName( @Nullable final String name )
    {
        return name != null && !name.isEmpty();
    }

    private static boolean isValidSourceName( @Nullable final String sourceName )
    {
        return isValidName(sourceName);
    }

    private static boolean isValidFormat( @Nullable final PropertyFormat format, final boolean isContainer )
    {
        if (format == null) {
            return false;
        }

        if (!isContainer) {
            return true;
        }

        return format == PropertyFormat.JSON;
    }
}
