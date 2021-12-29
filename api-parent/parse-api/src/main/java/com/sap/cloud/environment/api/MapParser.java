package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@FunctionalInterface
public interface MapParser {
    @Nullable
    Map<String, Object> parseAsMap(@Nonnull final String rawMap);
}
