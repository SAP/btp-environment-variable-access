/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ServiceBinding
{
    @Nonnull
    Set<String> getKeys();

    boolean containsKey( @Nonnull final String key );

    @Nonnull
    Optional<Object> get( @Nonnull final String key );

    @Nonnull
    Optional<String> getName();

    @Nonnull
    Optional<String> getServiceName();

    @Nonnull
    Optional<String> getServicePlan();

    @Nonnull
    List<String> getTags();

    @Nonnull
    Map<String, Object> getCredentials();
}
