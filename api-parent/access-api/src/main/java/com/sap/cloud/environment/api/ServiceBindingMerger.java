/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ServiceBindingMerger implements ServiceBindingAccessor
{
    @Nonnull
    public static final EqualityComparer KEEP_EVERYTHING = ( a, b ) -> false;

    @Nonnull
    private final Collection<ServiceBindingAccessor> accessors;
    @Nonnull
    private final EqualityComparer equalityComparer;

    public ServiceBindingMerger( @Nonnull final Collection<ServiceBindingAccessor> accessors,
                                 @Nonnull final EqualityComparer equalityComparer )
    {
        this.accessors = new ArrayList<>(accessors);
        this.equalityComparer = equalityComparer;
    }

    @Nonnull
    public static InitialBuilder builder()
    {
        return new Builder();
    }

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings( @Nonnull final ServiceBindingAccessorOptions options )
    {
        final List<ServiceBinding> mergedServiceBindings = new ArrayList<>();
        accessors.stream()
                 .map(accessor -> accessor.getServiceBindings(options))
                 .flatMap(List::stream)
                 .forEachOrdered(binding -> {
                     if (contains(mergedServiceBindings, binding)) {
                         return;
                     }

                     mergedServiceBindings.add(binding);
                 });

        return mergedServiceBindings;
    }

    private boolean contains( @Nonnull final List<ServiceBinding> existingBindings,
                              @Nonnull final ServiceBinding newBinding )
    {
        return existingBindings.stream().anyMatch(contained -> equalityComparer.areEqual(contained, newBinding));
    }

    @FunctionalInterface
    public interface EqualityComparer
    {
        boolean areEqual( @Nonnull final ServiceBinding a, @Nonnull final ServiceBinding b );
    }

    public interface InitialBuilder
    {
        @Nonnull
        AccessorBuilder withAccessor( @Nonnull final ServiceBindingAccessor accessor );
    }

    public interface AccessorBuilder
    {
        @Nonnull
        AccessorBuilder withAccessor( @Nonnull final ServiceBindingAccessor accessor );

        @Nonnull
        TerminalBuilder andKeySelector( @Nonnull final EqualityComparer equalityComparer );
    }

    public interface TerminalBuilder
    {
        @Nonnull
        ServiceBindingMerger build();
    }

    private static class Builder implements InitialBuilder, AccessorBuilder, TerminalBuilder
    {
        @Nonnull
        private final List<ServiceBindingAccessor> accessors = new ArrayList<>();
        @Nullable
        private EqualityComparer equalityComparer;

        @Nonnull
        @Override
        public AccessorBuilder withAccessor( @Nonnull final ServiceBindingAccessor accessor )
        {
            accessors.add(accessor);
            return this;
        }

        @Nonnull
        @Override
        public TerminalBuilder andKeySelector( @Nonnull final EqualityComparer equalityComparer )
        {
            this.equalityComparer = equalityComparer;
            return this;
        }

        @Nonnull
        @Override
        public ServiceBindingMerger build()
        {
            return new ServiceBindingMerger(accessors,
                                            Objects.requireNonNull(equalityComparer,
                                                                   "The KeySelector must not be null!"));
        }
    }
}
