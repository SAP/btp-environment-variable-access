/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;

import javax.annotation.Nonnull;
import java.util.List;

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
