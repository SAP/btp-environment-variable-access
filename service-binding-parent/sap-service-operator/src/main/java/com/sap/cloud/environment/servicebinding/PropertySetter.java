package com.sap.cloud.environment.servicebinding;

import org.json.JSONArray;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FunctionalInterface
interface PropertySetter
{
    @Nonnull
    String CREDENTIALS_KEY = "credentials";

    @Nonnull
    @SuppressWarnings( "unchecked" )
    PropertySetter TO_CREDENTIALS = ( binding, name, value ) -> {
        Map<String, Object> credentials = null;
        if (binding.containsKey(CREDENTIALS_KEY)) {
            final Object maybeCredentials = binding.get(CREDENTIALS_KEY);
            if (maybeCredentials instanceof Map) {
                credentials = (Map<String, Object>) maybeCredentials;
            } else {
                throw new IllegalStateException(String.format("The '%s' property must be of type %s.", CREDENTIALS_KEY, Map.class.getSimpleName()));
            }
        }

        if (credentials == null) {
            credentials = new HashMap<>();
            binding.put(CREDENTIALS_KEY, credentials);
        }

        credentials.put(name, value);
    };

    @Nonnull
    PropertySetter TO_ROOT = Map::put;

    @Nonnull
    @SuppressWarnings( "unchecked" )
    static PropertySetter asList( @Nonnull final PropertySetter actualSetter )
    {
        return ( binding, name, value ) -> {
            final List<Object> list;
            if (value instanceof List) {
                list = (List<Object>) value;
            } else if (value instanceof String) {
                list = new JSONArray((String) value).toList();
            } else {
                throw new IllegalStateException(String.format("The provided value '%s' cannot be converted to a %s.", value, List.class.getSimpleName()));
            }

            actualSetter.setProperty(binding, name, list);
        };
    }

    void setProperty( @Nonnull final Map<String, Object> rawServiceBinding, @Nonnull final String propertyName, @Nonnull final Object propertyValue );
}
