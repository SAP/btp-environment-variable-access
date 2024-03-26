package com.sap.cloud.environment.servicebinding;

import javax.annotation.Nonnull;

enum BindingPropertyFormat
{
    TEXT("text"),
    JSON("json");

    @Nonnull
    private final String value;

    BindingPropertyFormat( @Nonnull final String value )
    {
        this.value = value;
    }

    @Nonnull
    public String getValue()
    {
        return value;
    }
}
