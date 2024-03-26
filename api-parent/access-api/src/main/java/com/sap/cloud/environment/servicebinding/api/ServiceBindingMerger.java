package com.sap.cloud.environment.servicebinding.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A {@link ServiceBindingAccessor} that merges the result of multiple other {@link ServiceBindingAccessor}s. This is
 * done in the following manner:<br>
 * 1. Create an empty {@link java.util.Set} of {@link ServiceBinding}s - this is the final result that should be
 * returned by {@link #getServiceBindings()}.<br>
 * 2. For each delegate {@link ServiceBindingAccessor}:<br>
 * &emsp;1. Call {@link ServiceBindingAccessor#getServiceBindings()}.<br>
 * &emsp;2. For each {@link ServiceBinding}:<br>
 * &emsp;&emsp;1. Check whether the given {@link ServiceBinding} already exists<b>*</b> in the result
 * {@link java.util.Set}.<br>
 * &emsp;&emsp;2. If the {@link ServiceBinding} does not yet exist, add it to the result.<br>
 * <b>*:</b> This class uses the {@link EqualityComparer} to determine whether a given {@link ServiceBinding} already
 * exists in the result {@link java.util.Set}.
 */
public class ServiceBindingMerger implements ServiceBindingAccessor
{
    /**
     * A {@link EqualityComparer} that always evaluates to {@code false}. Therefore, all {@link ServiceBinding}s will be
     * included in the combined result set.
     */
    @Nonnull
    public static final EqualityComparer KEEP_EVERYTHING = ( a, b ) -> false;

    /**
     * A {@link EqualityComparer} that compares {@link ServiceBinding} instance by using the
     * {@link Object#equals(Object)} implementation. Therefore, duplicated (i.e. non-unique) {@link ServiceBinding}s
     * will <b>not</b> be included in the combined result set.
     */
    @Nonnull
    public static final EqualityComparer KEEP_UNIQUE = Object::equals;

    @Nonnull
    private final Collection<ServiceBindingAccessor> accessors;

    @Nonnull
    private final EqualityComparer equalityComparer;

    /**
     * Initializes a new {@link ServiceBindingMerger} instance. It will use the given {@code equalityComparer} to
     * combine the individual {@link ServiceBinding}s returned from the given {@code accessors}.
     *
     * @param accessors
     *            The {@link ServiceBindingAccessor}s that should be used to get the actual {@link ServiceBinding}s
     *            from.
     * @param equalityComparer
     *            The {@link EqualityComparer} to check whether a given {@link ServiceBinding} is already part of the
     *            combined result set.
     */
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

    /**
     * Represents a method object that is capable of comparing two instances of {@link ServiceBinding}. An instance of
     * this interface will be used when checking whether a given {@link ServiceBinding} is already contained in the
     * combined set of {@link ServiceBinding}s.
     */
    @FunctionalInterface
    public interface EqualityComparer
    {
        /**
         * Determines whether the given {@link ServiceBinding} instances ({@code a} and {@code b}) are equal.
         *
         * @param a
         *            The first {@link ServiceBinding} instance.
         * @param b
         *            The second {@link ServiceBinding} instance.
         * @return {@code true} if {@code a} and {@code b} are equal, {@code false} otherwise.
         */
        boolean areEqual( @Nonnull final ServiceBinding a, @Nonnull final ServiceBinding b );
    }
}
