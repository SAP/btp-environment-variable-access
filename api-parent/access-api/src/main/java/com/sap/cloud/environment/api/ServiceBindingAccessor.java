/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import java.util.List;

import com.sap.cloud.environment.api.exception.ServiceBindingAccessException;

@FunctionalInterface
public interface ServiceBindingAccessor
{
    @Nonnull
    default List<ServiceBinding> getServiceBindings() throws ServiceBindingAccessException
    {
        return getServiceBindings(ServiceBindingAccessorOptions.NONE);
    }

    @Nonnull
    List<ServiceBinding> getServiceBindings( @Nonnull final ServiceBindingAccessorOptions options )
            throws ServiceBindingAccessException;
}
