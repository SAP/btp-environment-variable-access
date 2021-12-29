package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;

import com.sap.cloud.environment.api.exception.ServiceBindingAccessException;

@FunctionalInterface
public interface ServiceBindingAccessor {
    @Nonnull
    Iterable<ServiceBinding> getServiceBindings() throws ServiceBindingAccessException;
}
