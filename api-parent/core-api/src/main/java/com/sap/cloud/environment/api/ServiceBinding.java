/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface ServiceBinding
{
    @Nonnull
    Iterable<String> getKeys();

    @Nonnull
    Iterable<Map.Entry<String, Object>> getEntries();

    boolean containsKey(@Nonnull final String key);

    @Nullable
    Object get(@Nonnull final String key);

    @Nonnull
    Map<String, Object> toMap();
}
