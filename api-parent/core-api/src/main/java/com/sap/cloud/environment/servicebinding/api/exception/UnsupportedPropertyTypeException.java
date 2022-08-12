/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api.exception;

import javax.annotation.Nonnull;

/**
 * A {@link RuntimeException} that is thrown if an unsupported property type is encountered while initializing a
 * {@link com.sap.cloud.environment.servicebinding.api.ServiceBinding}.
 */
public class UnsupportedPropertyTypeException extends RuntimeException
{
    private static final long serialVersionUID = -880511175727343442L;

    /**
     * Initializes a new {@link UnsupportedPropertyTypeException} with the given {@code unsupportedType}.
     *
     * @param unsupportedType
     *            The {@link Class} of the unsupported property.
     */
    public UnsupportedPropertyTypeException( @Nonnull final Class<?> unsupportedType )
    {
        super(String.format("The type '%s' is not supported.", unsupportedType.getName()));
    }
}
