/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Represents an <bold>immutable</bold> collection of (secret) key-value properties. These properties can be used to
 * establish a connection to the bound service.
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
}
