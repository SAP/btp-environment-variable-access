/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents an <b>immutable</b> collection of (secret) key-value properties. These properties can be used to establish
 * a connection to the bound service.
 */
public interface ServiceBinding
{
    /**
     * Returns the {@link Set} of contained keys.
     *
     * @return The {@link Set} of contained keys.
     */
    @Nonnull
    Set<String> getKeys();

    /**
     * Checks whether the given {@code key} is contained in this {@link ServiceBinding}.
     *
     * @param key
     *            The key to check.
     * @return {@code true} if the given {@code key} is contained, {@code false} otherwise.
     */
    boolean containsKey( @Nonnull final String key );

    /**
     * Returns an {@link Optional} that might contain a value (if it exists) for the given {@code key}.
     *
     * @param key
     *            The key of the value to get.
     * @return An {@link Optional} that might contain a value (if it exists) for the given {@code key}.
     */
    @Nonnull
    Optional<Object> get( @Nonnull final String key );

    /**
     * Returns an {@link Optional} that might contain the name (if it exists) of this {@link ServiceBinding}.
     *
     * @return An {@link Optional} that might contain the name (if it exists) of this {@link ServiceBinding}.
     */
    @Nonnull
    Optional<String> getName();

    /**
     * Returns an {@link Optional} that might contain the name of the bound service (if it exists) of this
     * {@link ServiceBinding}.
     *
     * @return An {@link Optional} that might contain the name of the bound service (if it exists) of this
     *         {@link ServiceBinding}.
     */
    @Nonnull
    Optional<String> getServiceName();

    /**
     * Returns an {@link Optional} that might contain the {@link ServiceIdentifier} of the bound service (if it exists)
     * of this {@link ServiceBinding}.
     *
     * @return An {@link Optional} that might contain the {@link ServiceIdentifier} of the bound service (if it exists)
     *         of this {@link ServiceBinding}.
     */
    @Nonnull
    default Optional<ServiceIdentifier> getServiceIdentifier()
    {
        return getServiceName().map(ServiceIdentifier::of);
    }

    /**
     * Returns an {@link Optional} that might contain the plan of the bound service (if it exists) of this
     * {@link ServiceBinding}.
     *
     * @return An {@link Optional} that might contain the plan of the bound service (if it exists) of this
     *         {@link ServiceBinding}.
     */
    @Nonnull
    Optional<String> getServicePlan();

    /**
     * Returns a {@link List} of all tags of this {@link ServiceBinding}.
     *
     * @return A {@link List} of all tags of this {@link ServiceBinding}.
     */
    @Nonnull
    List<String> getTags();

    /**
     * Returna a {@link Map} of credentials that are required to connect to the bound service.
     *
     * @return A {@link Map} of credentials that are required to connect to the bound service.
     */
    @Nonnull
    Map<String, Object> getCredentials();

    /**
     * Returns a <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     * but with the given {@code properties}.
     * <p>
     * <b>Note</b>: The given {@code properties} will affect {@link #getKeys()}, {@link #containsKey(String)}, and
     * {@link #get(String)} <b>only</b>. All other methods will return the exact same values as this
     * {@link ServiceBinding}.
     * </p>
     * <p>
     * Example:
     *
     * <pre>
     * ServiceBinding originalBinding;
     * originalBinding.getKeys(); // ["old-key1", "old-key2"]
     *
     * ServiceBinding bindingWithArbitraryKey =
     *     originalBinding.withProperties(Collections.singletonMap("new-key", "some value"));
     * bindingWithArbitraryKey.getKeys(); // ["new-key"]
     *
     * originalBinding.getCredentials(); // {"key-1": "value-1", "key-2": "value-2"}
     * ServiceBinding bindingWithCredentialsProperty =
     *     originalBinding.withProperties(Collections.singletonMap("credentials", "some value"));
     * bindingWithCredentialsProperty.getCredentials(); // {"key-1": "value-1", "key-2": "value-2"} <-- this has NOT been changed
     * </pre>
     * </p>
     *
     * @param properties
     *            The properties to set. Supplying {@code null} is equivalent to supplying an empty {@link Map}.
     * @return A <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     *         but with the given {@code properties}.
     * @since 0.10.0
     */
    @Nonnull
    default ServiceBinding withProperties( @Nullable final Map<String, Object> properties )
    {
        return DelegatingServiceBinding.builder(this).withProperties(properties).build();
    }

    /**
     * Returns a <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     * but with the given {@code name}.
     * <p>
     * <b>Note</b>: The given {@code name} will affect {@link #getName()} <b>only</b>. All other methods will return the
     * exact same values as this {@link ServiceBinding}.
     * </p>
     *
     * @param name
     *            The name to set.
     * @return A <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     *         but with the given {@code name}.
     * @since 0.10.0
     */
    @Nonnull
    default ServiceBinding withName( @Nullable final String name )
    {
        return DelegatingServiceBinding.builder(this).withName(name).build();
    }

    /**
     * Returns a <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     * but with the given {@code serviceName}.
     * <p>
     * <b>Note</b>: The given {@code serviceName} will affect {@link #getServiceName()} <b>only</b>. All other methods
     * will return the exact same values as this {@link ServiceBinding}.
     * </p>
     *
     * @param serviceName
     *            The serviceName to set.
     * @return A <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     *         but with the given {@code serviceName}.
     * @since 0.10.0
     */
    @Nonnull
    default ServiceBinding withServiceName( @Nullable final String serviceName )
    {
        return DelegatingServiceBinding.builder(this).withServiceName(serviceName).build();
    }

    /**
     * Returns a <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     * but with the given {@code serviceIdentifier}.
     * <p>
     * <b>Note</b>: The given {@code serviceIdentifier} will affect {@link #getServiceIdentifier()} <b>only</b>. All
     * other methods will return the exact same values as this {@link ServiceBinding}.
     * </p>
     *
     * @param serviceIdentifier
     *            The serviceIdentifier to set.
     * @return A <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     *         but with the given {@code serviceIdentifier}.
     * @since 0.10.0
     */
    @Nonnull
    default ServiceBinding withServiceIdentifier( @Nullable final ServiceIdentifier serviceIdentifier )
    {
        return DelegatingServiceBinding.builder(this).withServiceIdentifier(serviceIdentifier).build();
    }

    /**
     * Returns a <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     * but with the given {@code servicePlan}.
     * <p>
     * <b>Note</b>: The given {@code servicePlan} will affect {@link #getServicePlan()} <b>only</b>. All other methods
     * will return the exact same values as this {@link ServiceBinding}.
     * </p>
     *
     * @param servicePlan
     *            The servicePlan to set.
     * @return A <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     *         but with the given {@code servicePlan}.
     * @since 0.10.0
     */
    @Nonnull
    default ServiceBinding withServicePlan( @Nullable final String servicePlan )
    {
        return DelegatingServiceBinding.builder(this).withServicePlan(servicePlan).build();
    }

    /**
     * Returns a <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     * but with the given {@code tags}.
     * <p>
     * <b>Note</b>: The given {@code tags} will affect {@link #getTags()} <b>only</b>. All other methods will return the
     * exact same values as this {@link ServiceBinding}.
     * </p>
     *
     * @param tags
     *            The tags to set. Supplying {@code null} is equivalent to supplying an empty {@link List}.
     * @return A <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     *         but with the given {@code tags}.
     */
    @Nonnull
    default ServiceBinding withTags( @Nullable final List<String> tags )
    {
        return DelegatingServiceBinding.builder(this).withTags(tags).build();
    }

    /**
     * Returns a <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     * but with the given {@code credentials}.
     * <p>
     * <b>Note</b>: The given {@code credentials} will affect {@link #getCredentials()} <b>only</b>. All other methods
     * will return the exact same values as this {@link ServiceBinding}.
     * </p>
     *
     * @param credentials
     *            The credentials to set. Supplying {@code null} is equivalent to supplying an empty {@link Map}.
     * @return A <b>NEW</b> {@link ServiceBinding} instance that contains the same values as this {@link ServiceBinding}
     *         but with the given {@code credentials}.
     * @since 0.10.0
     */
    @Nonnull
    default ServiceBinding withCredentials( @Nullable final Map<String, Object> credentials )
    {
        return DelegatingServiceBinding.builder(this).withCredentials(credentials).build();
    }
}
