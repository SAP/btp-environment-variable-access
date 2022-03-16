/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api.exception;

import javax.annotation.Nonnull;

public class ServiceBindingAccessException extends RuntimeException
{
    private static final long serialVersionUID = 8589108462580396260L;

    public ServiceBindingAccessException()
    {
    }

    public ServiceBindingAccessException( @Nonnull final String message )
    {
        super(message);
    }

    public ServiceBindingAccessException( @Nonnull final String message, @Nonnull final Throwable cause )
    {
        super(message, cause);
    }

    public ServiceBindingAccessException( @Nonnull final Throwable cause )
    {
        super(cause);
    }
}
