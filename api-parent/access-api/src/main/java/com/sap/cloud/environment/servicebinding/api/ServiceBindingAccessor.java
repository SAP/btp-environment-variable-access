/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import javax.annotation.Nonnull;
import java.util.List;

import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;

@FunctionalInterface
public interface ServiceBindingAccessor
{
    @Nonnull
    List<ServiceBinding> getServiceBindings() throws ServiceBindingAccessException;
}
