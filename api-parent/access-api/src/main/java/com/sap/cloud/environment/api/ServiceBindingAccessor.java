package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;

import java.util.List;

import com.sap.cloud.environment.api.exception.ServiceBindingAccessException;

@FunctionalInterface
public interface ServiceBindingAccessor
{
    @Nonnull
    List<ServiceBinding> getServiceBindings() throws ServiceBindingAccessException;
}
