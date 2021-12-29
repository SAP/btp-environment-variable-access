package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@FunctionalInterface
public interface ListParser {
    @Nullable
    List<Object> parseAsList(@Nonnull final String rawList);
}
