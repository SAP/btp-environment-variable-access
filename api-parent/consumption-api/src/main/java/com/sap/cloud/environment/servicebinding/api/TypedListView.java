/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.sap.cloud.environment.servicebinding.api.exception.ValueCastException;

public final class TypedListView
{
    @Nonnull
    private final List<Object> list;

    private TypedListView( @Nonnull final List<Object> list )
    {
        this.list = list;
    }

    @Nonnull
    static TypedListView fromList( @Nonnull final List<Object> list )
    {
        final List<Object> elements = new ArrayList<>(list.size());
        for( final Object element : list ) {
            if( element instanceof Map ) {
                elements.add(TypedMapView.fromRawMap(element));
                continue;
            }

            if( element instanceof List ) {
                elements.add(fromRawList(element));
                continue;
            }

            elements.add(element);
        }

        return new TypedListView(elements);
    }

    @Nonnull
    @SuppressWarnings( "unchecked" )
    static TypedListView fromRawList( @Nonnull final Object rawList )
    {
        try {
            return fromList((List<Object>) rawList);
        }
        catch( final ClassCastException e ) {
            throw new ValueCastException();
        }
    }

    public boolean getBoolean( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        final Object value = get(index);

        if( value instanceof Boolean ) {
            return (boolean) value;
        }

        throw new ValueCastException();
    }

    @Nullable
    public Object get( final int index )
        throws IndexOutOfBoundsException
    {
        if( index < 0 || index >= getSize() ) {
            throw new IndexOutOfBoundsException();
        }

        return list.get(index);
    }

    public int getSize()
    {
        return list.size();
    }

    public int getInteger( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        return getNumber(index).intValue();
    }

    @Nonnull
    public Number getNumber( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        final Object value = get(index);

        if( value instanceof Number ) {
            return (Number) value;
        }

        throw new ValueCastException();
    }

    public double getDouble( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        return getNumber(index).doubleValue();
    }

    @Nonnull
    public String getString( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        final Object value = get(index);

        if( value instanceof String ) {
            return (String) value;
        }

        throw new ValueCastException();
    }

    @Nonnull
    public TypedMapView getMapView( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        final Object value = get(index);

        if( value instanceof TypedMapView ) {
            return (TypedMapView) value;
        }

        throw new ValueCastException();
    }

    @Nonnull
    public TypedListView getListView( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        final Object value = get(index);

        if( value instanceof TypedListView ) {
            return (TypedListView) value;
        }

        throw new ValueCastException();
    }

    @SuppressWarnings( "unchecked" )
    @Nonnull
    public <T> List<T> getItems( @Nonnull final Class<? extends T> listType )
    {
        return list
            .stream()
            .filter(item -> item != null && item.getClass() == listType)
            .map(item -> (T) item)
            .collect(Collectors.toList());
    }
}
