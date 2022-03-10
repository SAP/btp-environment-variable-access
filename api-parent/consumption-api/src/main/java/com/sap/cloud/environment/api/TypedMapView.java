/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.sap.cloud.environment.api.exception.KeyNotFoundException;
import com.sap.cloud.environment.api.exception.ValueCastException;

public final class TypedMapView
{
    @Nonnull
    private final Map<String, Object> map;

    private TypedMapView( @Nonnull final Map<String, Object> map )
    {
        this.map = map;
    }

    @Nonnull
    public static TypedMapView of( @Nonnull final ServiceBinding serviceBinding )
    {
        final Map<String, Object> properties = new TreeMap<>(String::compareToIgnoreCase);
        for (final String key : serviceBinding.getKeys()) {
            if (key == null || key.isEmpty()) {
                continue;
            }

            final Object value = serviceBinding.get(key).orElse(null);
            insertElement(properties, key, value);
        }

        return new TypedMapView(properties);
    }

    @Nonnull
    public static TypedMapView ofCredentials( @Nonnull final ServiceBinding serviceBinding )
    {
        return fromMap(serviceBinding.getCredentials());
    }

    private static void insertElement( @Nonnull final Map<String, Object> properties,
                                       @Nonnull final String key,
                                       @Nullable final Object value )
    {
        if (value instanceof Map) {
            properties.put(key, fromRawMap(value));
            return;
        }

        if (value instanceof List) {
            properties.put(key, TypedListView.fromRawList(value));
            return;
        }

        properties.put(key, value);
    }

    @Nonnull
    @SuppressWarnings( "unchecked" )
    static TypedMapView fromRawMap( @Nonnull final Object rawMap )
    {
        try {
            return fromMap((Map<String, Object>) rawMap);
        } catch (final ClassCastException e) {
            throw new ValueCastException();
        }
    }

    @Nonnull
    static TypedMapView fromMap( @Nonnull final Map<String, Object> map )
    {
        final Map<String, Object> properties = new TreeMap<>(String::compareToIgnoreCase);
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            if (key == null) {
                continue;
            }

            insertElement(properties, key, value);
        }

        return new TypedMapView(properties);
    }

    @Nonnull
    public Set<String> getKeys()
    {
        return Collections.unmodifiableSet(map.keySet());
    }

    public boolean getBoolean( @Nonnull final String key ) throws KeyNotFoundException, ValueCastException
    {
        final Object value = get(key);

        if (value instanceof Boolean) {
            return (boolean) value;
        }

        throw new ValueCastException();
    }

    @Nullable
    public Object get( @Nonnull final String key ) throws KeyNotFoundException
    {
        if (!containsKey(key)) {
            throw new KeyNotFoundException();
        }

        return map.get(key);
    }

    public boolean containsKey( @Nonnull final String key )
    {
        return map.containsKey(key);
    }

    public int getInteger( @Nonnull final String key ) throws KeyNotFoundException, ValueCastException
    {
        return getNumber(key).intValue();
    }

    @Nonnull
    public Number getNumber( @Nonnull final String key ) throws KeyNotFoundException, ValueCastException
    {
        final Object value = get(key);

        if (value instanceof Number) {
            return (Number) value;
        }

        throw new ValueCastException();
    }

    public double getDouble( @Nonnull final String key ) throws KeyNotFoundException, ValueCastException
    {
        return getNumber(key).doubleValue();
    }

    @Nonnull
    public String getString( @Nonnull final String key ) throws KeyNotFoundException, ValueCastException
    {
        final Object value = get(key);

        if (value instanceof String) {
            return (String) value;
        }

        throw new ValueCastException();
    }

    @Nonnull
    public TypedMapView getMapView( @Nonnull final String key ) throws KeyNotFoundException, ValueCastException
    {
        final Object value = get(key);

        if (value instanceof TypedMapView) {
            return (TypedMapView) value;
        }

        throw new ValueCastException();
    }

    @Nonnull
    public TypedListView getListView( @Nonnull final String key ) throws KeyNotFoundException, ValueCastException
    {
        final Object value = get(key);

        if (value instanceof TypedListView) {
            return (TypedListView) value;
        }

        throw new ValueCastException();
    }


    public <T> Map<String, T> getEntries( @Nonnull final Class<? extends T> mapType )
    {
        return map.entrySet()
                  .stream()
                  .filter(e -> e.getValue() != null && e.getValue().getClass() == mapType)
                  .collect(Collectors.toMap(e -> e.getKey(), e -> (T) e.getValue()));
    }
}
