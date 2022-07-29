/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api.exception;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link RuntimeException} that is thrown if a value cannot be cast to an expected target type.
 */
public class ValueCastException extends RuntimeException
{
    private static final long serialVersionUID = -2894546268341628598L;

    @Nonnull
    private final Class<?> requestedType;

    @Nullable
    private final Object actualObject;

    /**
     * Initializes a new {@link ValueCastException} instance with the given {@code requestedType} and the
     * {@code actualObject}.
     *
     * @param requestedType
     *            The {@link Class}, which the {@code actualObject} should be cast to.
     * @param actualObject
     *            The actual object found, that couldn't be cast to the {@code requestedType}.
     */
    public ValueCastException( @Nonnull final Class<?> requestedType, @Nullable final Object actualObject )
    {
        this.requestedType = requestedType;
        this.actualObject = actualObject;
    }

    /**
     * Returns the {@link Class} that was requested as the target type.
     *
     * @return The {@link Class} that was requested as the target type.
     */
    @Nonnull
    public Class<?> getRequestedType()
    {
        return requestedType;
    }

    /**
     * Returns an {@link Optional} that might contain the object (if present) that should have been cast.
     *
     * @return An {@link Optional} that might contain the object (if present) that should have been cast.
     */
    @Nonnull
    public Optional<Object> getActualObject()
    {
        return Optional.ofNullable(actualObject);
    }

    /**
     * Returns an {@link Optional} that might contain the actual {@link Class} of the object (if present) that should
     * have been cast.
     *
     * @return An {@link Optional} that might contain the actual {@link Class} of the object (if present) that should
     *         have been cast.
     */
    @Nonnull
    public Optional<Class<?>> getActualType()
    {
        return getActualObject().map(Object::getClass);
    }
}
