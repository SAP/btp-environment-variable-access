package com.sap.cloud.environment.api.exception;

import javax.annotation.Nonnull;

public class UnsupportedPropertyTypeException extends RuntimeException
{
    private static final long serialVersionUID = -880511175727343442L;

    public UnsupportedPropertyTypeException( @Nonnull final Class<?> unsupportedType )
    {
        super(String.format("The type '%s' is not supported.", unsupportedType.getName()));
    }
}
