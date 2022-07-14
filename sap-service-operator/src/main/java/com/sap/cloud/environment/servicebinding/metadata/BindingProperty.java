/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.metadata;

import java.util.Objects;

import javax.annotation.Nonnull;

public class BindingProperty
{
    @Nonnull
    private final String name;

    @Nonnull
    private final String sourceName;

    @Nonnull
    private final PropertyFormat format;

    private final boolean isContainer;

    @Nonnull
    public static BindingProperty container( @Nonnull final String name )
    {
        return container(name, name);
    }

    @Nonnull
    public static BindingProperty container( @Nonnull final String name, @Nonnull final String sourceName )
    {
        return new BindingProperty(name, sourceName, PropertyFormat.JSON, true);
    }

    @Nonnull
    public static BindingProperty text( @Nonnull final String name )
    {
        return text(name, name);
    }

    @Nonnull
    public static BindingProperty text( @Nonnull final String name, @Nonnull final String sourceName )
    {
        return new BindingProperty(name, sourceName, PropertyFormat.TEXT, false);
    }

    @Nonnull
    public static BindingProperty json( @Nonnull final String name )
    {
        return json(name, name);
    }

    @Nonnull
    public static BindingProperty json( @Nonnull final String name, @Nonnull final String sourceName )
    {
        return new BindingProperty(name, sourceName, PropertyFormat.JSON, false);
    }

    BindingProperty(
        @Nonnull final String name,
        @Nonnull final String sourceName,
        @Nonnull final PropertyFormat format,
        final boolean isContainer )
    {
        this.name = name;
        this.sourceName = sourceName;
        this.format = format;
        this.isContainer = isContainer;
    }

    @Nonnull
    public String getName()
    {
        return name;
    }

    @Nonnull
    public String getSourceName()
    {
        return sourceName;
    }

    @Nonnull
    public PropertyFormat getFormat()
    {
        return format;
    }

    public boolean isContainer()
    {
        return isContainer;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if( !(obj instanceof BindingProperty) ) {
            return false;
        }

        final BindingProperty other = (BindingProperty) obj;

        return getName().equals(other.getName())
            && getSourceName().equals(other.getSourceName())
            && getFormat() == other.getFormat()
            && isContainer() == other.isContainer();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, sourceName, format, isContainer);
    }
}
