/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.sap.cloud.environment.servicebinding.api.exception.KeyNotFoundException;
import com.sap.cloud.environment.servicebinding.api.exception.ValueCastException;

/**
 * A utility class that allows type-safe access to entries of a {@link Map}.
 */
public final class TypedMapView
{
    @Nonnull
    private final Map<String, Object> map;

    private TypedMapView( @Nonnull final Map<String, Object> map )
    {
        this.map = map;
    }

    /**
     * Initializes a new {@link TypedMapView} instance from the given {@code serviceBinding}.
     *
     * @param serviceBinding
     *            The {@link ServiceBinding}, which should be converted.
     * @return A new {@link TypedMapView} instance.
     */
    @Nonnull
    public static TypedMapView of( @Nonnull final ServiceBinding serviceBinding )
    {
        final Map<String, Object> properties = new TreeMap<>(String::compareToIgnoreCase);
        for( final String key : serviceBinding.getKeys() ) {
            if( key == null || key.isEmpty() ) {
                continue;
            }

            final Object value = serviceBinding.get(key).orElse(null);
            insertElement(properties, key, value);
        }

        return new TypedMapView(properties);
    }

    /**
     * Initializes a new {@link TypedMapView} instance from the {@link ServiceBinding#getCredentials()} of the given
     * {@code serviceBinding}.
     *
     * @param serviceBinding
     *            The {@link ServiceBinding} to take the credentials from.
     * @return A new {@link TypedMapView} instance.
     */
    @Nonnull
    public static TypedMapView ofCredentials( @Nonnull final ServiceBinding serviceBinding )
    {
        return fromMap(serviceBinding.getCredentials());
    }

    private static void insertElement(
        @Nonnull final Map<String, Object> properties,
        @Nonnull final String key,
        @Nullable final Object value )
    {
        if( value instanceof Map ) {
            properties.put(key, fromRawMap(value));
            return;
        }

        if( value instanceof List ) {
            properties.put(key, TypedListView.fromRawIterable(value));
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
        }
        catch( final ClassCastException e ) {
            throw new ValueCastException(Map.class, rawMap);
        }
    }

    @Nonnull
    static TypedMapView fromMap( @Nonnull final Map<String, Object> map )
    {
        final Map<String, Object> properties = new TreeMap<>(String::compareToIgnoreCase);
        for( final Map.Entry<String, Object> entry : map.entrySet() ) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            if( key == null ) {
                continue;
            }

            insertElement(properties, key, value);
        }

        return new TypedMapView(properties);
    }

    /**
     * Returns an <bold>immutable</bold> {@link Set} of keys, which are contained in this {@link TypedMapView}.
     *
     * @return An <bold>immutable</bold> {@link Set} of keys.
     */
    @Nonnull
    public Set<String> getKeys()
    {
        return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * Returns the entry that is stored under the given {@code key} as a {@code boolean}.
     *
     * @param key
     *            The key of the entry.
     * @return The entry that is stored under the given {@code key} as a {@code boolean}.
     * @throws KeyNotFoundException
     *             Thrown if the given {@code key} is not contained in this {@link TypedMapView}.
     * @throws ValueCastException
     *             Thrown if the entry is not a {@code boolean}.
     */
    public boolean getBoolean( @Nonnull final String key )
        throws KeyNotFoundException,
            ValueCastException
    {
        final Object value = get(key);

        if( value instanceof Boolean ) {
            return (boolean) value;
        }

        throw new ValueCastException(Boolean.class, value);
    }

    /**
     * Returns the entry that is stored under the given {@code key}.
     *
     * @param key
     *            The key of the entry.
     * @return The entry that is stored under the given {@code key}.
     * @throws KeyNotFoundException
     *             Thrown if the given {@code key} is not contained in this {@link TypedMapView}.
     */
    @Nullable
    public Object get( @Nonnull final String key )
        throws KeyNotFoundException
    {
        if( !containsKey(key) ) {
            throw new KeyNotFoundException(this, key);
        }

        return map.get(key);
    }

    /**
     * Checks whether the given {@code key} is contained in this {@link TypedMapView}.
     *
     * @param key
     *            The key to check.
     * @return {@code true} if the key is contained, {@code false} otherwise.
     */
    public boolean containsKey( @Nonnull final String key )
    {
        return map.containsKey(key);
    }

    /**
     * Returns the entry that is stored under the given {@code key} as an {@code int}.
     *
     * @param key
     *            The key of the entry.
     * @return The entry that is stored under the given {@code key} as an {@code int}.
     * @throws KeyNotFoundException
     *             Thrown if the given {@code key} is not contained in this {@link TypedMapView}.
     * @throws ValueCastException
     *             Thrown if the entry is not an {@code int}.
     */
    public int getInteger( @Nonnull final String key )
        throws KeyNotFoundException,
            ValueCastException
    {
        return getNumber(key).intValue();
    }

    /**
     * Returns the entry that is stored under the given {@code key} as a {@link Number}.
     *
     * @param key
     *            The key of the entry.
     * @return The entry that is stored under the given {@code key} as a {@link Number}.
     * @throws KeyNotFoundException
     *             Thrown if the given {@code key} is not contained in this {@link TypedMapView}.
     * @throws ValueCastException
     *             Thrown if the entry is not a {@link Number}.
     */
    @Nonnull
    public Number getNumber( @Nonnull final String key )
        throws KeyNotFoundException,
            ValueCastException
    {
        final Object value = get(key);

        if( value instanceof Number ) {
            return (Number) value;
        }

        throw new ValueCastException(Number.class, value);
    }

    /**
     * Returns the entry that is stored under the given {@code key} as a {@code double}.
     *
     * @param key
     *            The key of the entry.
     * @return The entry that is stored under the given {@code key} as a {@code double}.
     * @throws KeyNotFoundException
     *             Thrown if the given {@code key} is not contained in this {@link TypedMapView}.
     * @throws ValueCastException
     *             Thrown if the entry is not a {@code double}.
     */
    public double getDouble( @Nonnull final String key )
        throws KeyNotFoundException,
            ValueCastException
    {
        return getNumber(key).doubleValue();
    }

    /**
     * Returns the entry that is stored under the given {@code key} as a {@link String}.
     *
     * @param key
     *            The key of the entry.
     * @return The entry that is stored under the given {@code key} as a {@link String}.
     * @throws KeyNotFoundException
     *             Thrown if the given {@code key} is not contained in this {@link TypedMapView}.
     * @throws ValueCastException
     *             Thrown if the entry is not a {@link String}.
     */
    @Nonnull
    public String getString( @Nonnull final String key )
        throws KeyNotFoundException,
            ValueCastException
    {
        final Object value = get(key);

        if( value instanceof String ) {
            return (String) value;
        }

        throw new ValueCastException(String.class, value);
    }

    /**
     * Returns the entry that is stored under the given {@code key} as a {@link TypedMapView}.
     *
     * @param key
     *            The key of the entry.
     * @return The entry that is stored under the given {@code key} as a {@link TypedMapView}.
     * @throws KeyNotFoundException
     *             Thrown if the given {@code key} is not contained in this {@link TypedMapView}.
     * @throws ValueCastException
     *             Thrown if the entry is not a {@link TypedMapView}.
     */
    @Nonnull
    public TypedMapView getMapView( @Nonnull final String key )
        throws KeyNotFoundException,
            ValueCastException
    {
        final Object value = get(key);

        if( value instanceof TypedMapView ) {
            return (TypedMapView) value;
        }

        throw new ValueCastException(TypedMapView.class, value);
    }

    /**
     * Returns the entry that is stored under the given {@code key} as a {@link TypedListView}.
     *
     * @param key
     *            The key of the entry.
     * @return The entry that is stored under the given {@code key} as a {@link TypedListView}.
     * @throws KeyNotFoundException
     *             Thrown if the given {@code key} is not contained in this {@link TypedMapView}.
     * @throws ValueCastException
     *             Thrown if the entry is not a {@link TypedListView}.
     */
    @Nonnull
    public TypedListView getListView( @Nonnull final String key )
        throws KeyNotFoundException,
            ValueCastException
    {
        final Object value = get(key);

        if( value instanceof TypedListView ) {
            return (TypedListView) value;
        }

        throw new ValueCastException(TypedListView.class, value);
    }

    /**
     * Returns a {@link Map} of entries (including their key) that are of the given {@code entryType} - including
     * sub-types.
     *
     * @param entryType
     *            The {@link Class} of the entries that should be returned.
     * @param <T>
     *            The entry type.
     * @return A {@link Map} of all entries (including their keys) contained in this {@link TypedMapView} that are of (a
     *         sub-)type of the given {@code entryType}.
     */
    @SuppressWarnings( "unchecked" )
    @Nonnull
    public <T> Map<String, T> getEntries( @Nonnull final Class<? extends T> entryType )
    {
        return map
            .entrySet()
            .stream()
            .filter(e -> e.getValue() != null && entryType.isAssignableFrom(e.getValue().getClass()))
            .collect(Collectors.toMap(Map.Entry::getKey, e -> (T) e.getValue()));
    }
}
