package com.sap.cloud.environment.servicebinding.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
public interface ServiceBindingPropertySelector<T>
{
    @Nullable
    T select( @Nonnull final ServiceBinding serviceBinding );
}
