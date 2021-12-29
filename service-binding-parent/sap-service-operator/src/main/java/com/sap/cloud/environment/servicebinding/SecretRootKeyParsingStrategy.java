package com.sap.cloud.environment.servicebinding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sap.cloud.environment.api.DefaultServiceBinding;
import com.sap.cloud.environment.api.MapParser;
import com.sap.cloud.environment.api.ServiceBinding;

public class SecretRootKeyParsingStrategy implements ParsingStrategy {

    @Nonnull
    public static final Map<String, PropertySetter> DEFAULT_PROPERTY_SETTERS;

    @Nonnull
    public static final PropertySetter DEFAULT_FALLBACK_PROPERTY_SETTER = PropertySetter.TO_CREDENTIALS;

    static {
        final Map<String, PropertySetter> defaultPropertySetters = new HashMap<>();
        defaultPropertySetters.put("plan", PropertySetter.TO_ROOT);
        defaultPropertySetters.put("instance_guid", PropertySetter.TO_ROOT);
        defaultPropertySetters.put("instance_name", PropertySetter.TO_ROOT);
        defaultPropertySetters.put("label", PropertySetter.TO_ROOT);
        defaultPropertySetters.put("tags", PropertySetter.TO_ROOT);

        DEFAULT_PROPERTY_SETTERS = Collections.unmodifiableMap(defaultPropertySetters);
    }

    @Nonnull
    private final Charset charset;
    @Nonnull
    private final MapParser mapParser;
    @Nonnull
    private final Map<String, PropertySetter> propertySetters;
    @Nonnull
    private final PropertySetter fallbackPropertySetter;

    public SecretRootKeyParsingStrategy(@Nonnull final Charset charset,
                                        @Nonnull final MapParser mapParser,
                                        @Nonnull final Map<String, PropertySetter> propertySetters,
                                        @Nonnull final PropertySetter fallbackPropertySetter) {
        this.charset = charset;
        this.mapParser = mapParser;
        this.propertySetters = propertySetters;
        this.fallbackPropertySetter = fallbackPropertySetter;
    }

    @Nullable
    @Override
    public ServiceBinding parse(@Nonnull final String serviceName,
                                @Nonnull final String bindingName,
                                @Nonnull final Path bindingPath) throws IOException {
        final List<Path> propertyFiles = Files.list(bindingPath).filter(Files::isRegularFile).collect(Collectors.toList());

        if (propertyFiles.isEmpty()) {
            // service binding directory must contain exactly one file
            return null;
        }

        if (propertyFiles.size() > 1) {
            // service binding directory must contain exactly one file
            return null;
        }

        final String fileContent = String.join("\n", Files.readAllLines(propertyFiles.get(0), charset));

        @Nullable final Map<String, Object> parsedFileContent = mapParser.parseAsMap(fileContent);

        if (parsedFileContent == null) {
            // the property file must contain a map-like (e.g. Json) structure
            return null;
        }

        final Map<String, Object> rawServiceBinding = new HashMap<>();

        getPropertySetter("name").setProperty(rawServiceBinding, "name", bindingName);
        for (final Map.Entry<String, Object> property : parsedFileContent.entrySet())
        {
            final String name = property.getKey();
            final Object value = property.getValue();

            if (name == null || value == null) {
                continue;
            }

            getPropertySetter(name).setProperty(rawServiceBinding, name, value);
        }

        return DefaultServiceBinding.wrapUnmodifiableMap(rawServiceBinding);
    }

    @Nonnull
    private PropertySetter getPropertySetter(@Nonnull final String propertyName)
    {
        return propertySetters.getOrDefault(propertyName, fallbackPropertySetter);
    }
}