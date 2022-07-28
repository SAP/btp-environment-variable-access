/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.util.List;

import javax.annotation.Nonnull;

import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;

/**
 * Represents a source for {@link ServiceBinding}s.
 */
@FunctionalInterface
public interface ServiceBindingAccessor
{
    /**
     * Retrieves all {@link ServiceBinding}s that are accessible for this {@link ServiceBindingAccessor}.
     *
     * @return All accessible {@link ServiceBinding}s.
     * @throws ServiceBindingAccessException
     *             Thrown if anything went wrong while loading the {@link ServiceBinding}s.
     */
    @Nonnull
    List<ServiceBinding> getServiceBindings()
        throws ServiceBindingAccessException;
}
