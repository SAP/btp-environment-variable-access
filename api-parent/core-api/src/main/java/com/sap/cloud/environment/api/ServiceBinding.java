/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Stream;

public interface ServiceBinding
{
    @Nonnull
    Stream<String> getKeys();

    boolean containsKey(@Nonnull final String key);

    @Nullable
    Object get(@Nonnull final String key);

    @Nonnull
    Map<String, Object> toMap();
}
