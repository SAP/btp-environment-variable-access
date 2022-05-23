/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import com.sap.cloud.environment.api.exception.ServiceBindingAccessException;

@FunctionalInterface
public interface ServiceBindingAccessor
{
    @Nonnull
    List<ServiceBinding> getServiceBindings() throws ServiceBindingAccessException;

    @Nonnull
    default ServiceBinding getServiceBindingByName( @Nonnull final String name )
    {
        final List<ServiceBinding> serviceByName = getServiceBindings().stream()
                                                                 .filter(b -> name.equals(b.getName().orElse(null)))
                                                                 .collect(Collectors.toList());

        if (serviceByName.isEmpty()) {
            throw new IllegalStateException("There is no service binding with name '" + name + "'");
        } else if (serviceByName.size() > 1) {
            throw new IllegalStateException("There are multiple service binding with name '" + name + "'");
        }
        return serviceByName.get(0);
    }
}
