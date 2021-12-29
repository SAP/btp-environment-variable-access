package com.sap.cloud.environment.servicebinding;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
public interface PropertySetter {
    @Nonnull
    @SuppressWarnings("unchecked")
    PropertySetter TO_CREDENTIALS = (binding, name, value) -> {
        Map<String, Object> credentials = null;
        if (binding.containsKey("credentials")) {
            final Object maybeCredentials = binding.get("credentials");
            if (maybeCredentials instanceof Map) {
                credentials = (Map<String, Object>) maybeCredentials;
            }
        }

        if (credentials == null) {
            credentials = new HashMap<>();
            binding.put("credentials", credentials);
        }

        credentials.put(name, value);
    };

    @Nonnull
    PropertySetter TO_ROOT = Map::put;

    void setProperty(@Nonnull final Map<String, Object> rawServiceBinding, @Nonnull final String propertyName, @Nonnull final Object propertyValue);
}
