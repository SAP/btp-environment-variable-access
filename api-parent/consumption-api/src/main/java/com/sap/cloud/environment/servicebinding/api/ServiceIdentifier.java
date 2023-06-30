/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an <b>equatable</b> service identifier that can be used across applications.
 */
public final class ServiceIdentifier
{
    @Nonnull
    private static final Map<String, ServiceIdentifier> INSTANCES = new ConcurrentHashMap<>();

    /**
     * Returns a {@link ServiceIdentifier} instance based on the {@link ServiceBinding#getServiceName()} method.
     *
     * @param serviceBinding
     *            The {@link ServiceBinding} to get the service name from.
     * @return A {@link ServiceIdentifier}.
     * @throws IllegalArgumentException
     *             If the result of {@link ServiceBinding#getServiceName()} is empty.
     * @see #of(String)
     */
    @Nonnull
    public static ServiceIdentifier fromServiceName( @Nonnull final ServiceBinding serviceBinding )
    {
        final String serviceName = serviceBinding.getServiceName().orElse(null);
        if( serviceName == null ) {
            throw new IllegalArgumentException("The service name of the provided service binding is null.");
        }

        return of(serviceName);
    }

    /**
     * Returns a {@link ServiceIdentifier} instance based on the provided {@code id}.
     * <p>
     * The {@code id} will be modified using {@link String#trim()} and {@link String#toLowerCase()}.
     * </p>
     *
     * @param id
     *            The identifier to use.
     * @return A {@link ServiceIdentifier}.
     * @throws IllegalArgumentException
     *             If the provided {@code id} is empty.
     */
    @Nonnull
    public static ServiceIdentifier of( @Nonnull final String id )
    {
        final String trimmedId = id.trim().toLowerCase(Locale.ROOT);
        if( trimmedId.isEmpty() ) {
            throw new IllegalArgumentException("The provided service identifier is empty.");
        }

        return INSTANCES.computeIfAbsent(trimmedId, ServiceIdentifier::new);
    }

    @Nonnull
    private final String id;

    private ServiceIdentifier( @Nonnull final String id )
    {
        this.id = id;
    }

    @Override
    public String toString()
    {
        return id;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        return obj instanceof ServiceIdentifier && id.equals(((ServiceIdentifier) obj).id);
    }
}
