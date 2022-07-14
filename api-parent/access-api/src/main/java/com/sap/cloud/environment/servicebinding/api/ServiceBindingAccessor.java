/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.util.List;

import javax.annotation.Nonnull;

import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;

@FunctionalInterface
public interface ServiceBindingAccessor
{
    @Nonnull
    List<ServiceBinding> getServiceBindings()
        throws ServiceBindingAccessException;
}
