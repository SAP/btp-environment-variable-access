/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api.exception;

import javax.annotation.Nonnull;

import com.sap.cloud.environment.servicebinding.api.TypedMapView;

/**
 * A {@link RuntimeException} that is thrown if a specific key was not found in a {@link TypedMapView}.
 */
public class KeyNotFoundException extends RuntimeException
{
    private static final long serialVersionUID = 1457209610404439200L;

    @Nonnull
    private final TypedMapView typedMapView;

    @Nonnull
    private final String requestedKey;

    /**
     * Initializes a new {@link KeyNotFoundException} instance with a given {@code typedMapView} and
     * {@code requestedKey}.
     *
     * @param typedMapView
     *            The {@link TypedMapView} that was queries.
     * @param requestedKey
     *            The key that wasn't found.
     */
    public KeyNotFoundException( @Nonnull final TypedMapView typedMapView, @Nonnull final String requestedKey )
    {
        this.typedMapView = typedMapView;
        this.requestedKey = requestedKey;
    }

    /**
     * Returns the queried {@link TypedMapView}.
     * 
     * @return The queried {@link TypedMapView}.
     */
    @Nonnull
    public TypedMapView getTypedMapView()
    {
        return typedMapView;
    }

    /**
     * Returns the requested key that wasn't found.
     * 
     * @return The requestde key that wasn't found.
     */
    @Nonnull
    public String getRequestedKey()
    {
        return requestedKey;
    }
}
