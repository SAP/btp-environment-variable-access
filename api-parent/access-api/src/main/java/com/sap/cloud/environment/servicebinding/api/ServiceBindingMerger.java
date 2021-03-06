/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

public class ServiceBindingMerger implements ServiceBindingAccessor
{
    @Nonnull
    public static final EqualityComparer KEEP_EVERYTHING = ( a, b ) -> false;

    @Nonnull
    private final Collection<ServiceBindingAccessor> accessors;

    @Nonnull
    private final EqualityComparer equalityComparer;

    public ServiceBindingMerger(
        @Nonnull final Collection<ServiceBindingAccessor> accessors,
        @Nonnull final EqualityComparer equalityComparer )
    {
        this.accessors = new ArrayList<>(accessors);
        this.equalityComparer = equalityComparer;
    }

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings()
    {
        final List<ServiceBinding> mergedServiceBindings = new ArrayList<>();
        accessors
            .stream()
            .map(ServiceBindingAccessor::getServiceBindings)
            .flatMap(List::stream)
            .forEachOrdered(binding -> {
                if( contains(mergedServiceBindings, binding) ) {
                    return;
                }

                mergedServiceBindings.add(binding);
            });

        return mergedServiceBindings;
    }

    private
        boolean
        contains( @Nonnull final List<ServiceBinding> existingBindings, @Nonnull final ServiceBinding newBinding )
    {
        return existingBindings.stream().anyMatch(contained -> equalityComparer.areEqual(contained, newBinding));
    }

    @FunctionalInterface
    public interface EqualityComparer
    {
        boolean areEqual( @Nonnull final ServiceBinding a, @Nonnull final ServiceBinding b );
    }
}
