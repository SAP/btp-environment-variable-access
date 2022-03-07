/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ServiceBinding
{
    @Nonnull
    List<String> getKeys();

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
