/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.metadata;

import javax.annotation.Nonnull;

public enum PropertyFormat
{
    TEXT("text"),
    JSON("json");

    @Nonnull
    private final String value;

    PropertyFormat( @Nonnull final String value )
    {
        this.value = value;
    }

    @Nonnull
    public String getValue()
    {
        return value;
    }
}
