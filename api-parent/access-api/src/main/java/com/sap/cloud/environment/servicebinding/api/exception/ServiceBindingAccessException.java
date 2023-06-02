/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api.exception;

import javax.annotation.Nonnull;

/**
 * A {@link RuntimeException} that is thrown if anything goes wrong while accessing
 * {@link com.sap.cloud.environment.servicebinding.api.ServiceBinding}s.
 */
public class ServiceBindingAccessException extends RuntimeException
{
    private static final long serialVersionUID = 8589108462580396260L;

    /**
     * Initializes a new {@link ServiceBindingAccessException} instance with a dedicated {@code message}.
     *
     * @param message
     *            The exception message.
     */
    public ServiceBindingAccessException( @Nonnull final String message )
    {
        super(message);
    }

    /**
     * Initializes a new {@link ServiceBindingAccessException} instance with a dedicated {@code cause}.
     *
     * @param cause
     *            The exception cause.
     */
    public ServiceBindingAccessException( @Nonnull final Throwable cause )
    {
        super(cause);
    }

    /**
     * Initializes a new {@link ServiceBindingAccessException} instance with a dedicated {@code message} and
     * {@code cause}.
     *
     * @param message
     *            The exception message.
     * @param cause
     *            The exception cause.
     */
    public ServiceBindingAccessException( @Nonnull final String message, @Nonnull final Throwable cause )
    {
        super(message, cause);
    }
}
