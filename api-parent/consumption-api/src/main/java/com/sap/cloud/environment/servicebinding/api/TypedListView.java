/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.sap.cloud.environment.servicebinding.api.exception.ValueCastException;

/**
 * A utility class that allows type-safe access to items of a {@link List}.
 */
public final class TypedListView
{
    @Nonnull
    private final List<Object> list;

    private TypedListView( @Nonnull final List<Object> list )
    {
        this.list = list;
    }

    @Nonnull
    static TypedListView fromIterable( @Nonnull final Iterable<Object> list )
    {
        final List<Object> items = new ArrayList<>();
        for( final Object item : list ) {
            if( item instanceof Map ) {
                items.add(TypedMapView.fromRawMap(item));
                continue;
            }

            if( item instanceof List ) {
                items.add(fromRawIterable(item));
                continue;
            }

            items.add(item);
        }

        return new TypedListView(items);
    }

    @Nonnull
    @SuppressWarnings( "unchecked" )
    static TypedListView fromRawIterable( @Nonnull final Object rawIterable )
    {
        try {
            return fromIterable((Iterable<Object>) rawIterable);
        }
        catch( final ClassCastException e ) {
            throw new ValueCastException(Iterable.class, rawIterable);
        }
    }

    /**
     * Returns the size of the underlying {@link List}.
     *
     * @return The size of the underlying {@link List}.
     */
    public int getSize()
    {
        return list.size();
    }

    /**
     * Returns the item at the given {@code index}.
     *
     * @param index
     *            The list index of the item.
     * @return The item at the given {@code index}.
     * @throws IndexOutOfBoundsException
     *             Thrown if the given {@code index} is out of bounds.
     */
    @Nullable
    public Object get( final int index )
        throws IndexOutOfBoundsException
    {
        if( index < 0 || index >= getSize() ) {
            throw new IndexOutOfBoundsException();
        }

        return list.get(index);
    }

    /**
     * Returns an {@link Optional}, which may contain an item at the given {@code index}.
     *
     * @param index
     *            The list index of the item.
     * @return An {@link Optional#ofNullable(Object)} that contains the item if it exists, {@link Optional#empty()}
     *         otherwise.
     */
    @Nonnull
    public Optional<Object> getMaybe( final int index )
    {
        if( index < 0 || index >= getSize() ) {
            return Optional.empty();
        }

        return Optional.ofNullable(list.get(index));
    }

    /**
     * Returns the item at the given {@code index} as a {@code boolean}.
     *
     * @param index
     *            The list index of the item.
     * @return The item at the given {@code index} as a {@code boolean}.
     * @throws IndexOutOfBoundsException
     *             Thrown if the given {@code index} is out of bounds.
     * @throws ValueCastException
     *             Thrown if the item is not a {@code boolean}.
     */
    public boolean getBoolean( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        final Object value = get(index);

        if( value instanceof Boolean ) {
            return (boolean) value;
        }

        throw new ValueCastException(Boolean.class, value);
    }

    /**
     * Returns an {@link Optional}, which may contain a {@link Boolean} item that is stored at the given {@code index}.
     * The returned {@link Optional} is empty if the item is either not a {@link Boolean} or does not even exist in the
     * first place.
     *
     * @param index
     *            The list index of the item.
     * @return An {@link Optional} that contains the item if it exists and actually is a {@link Boolean},
     *         {@link Optional#empty()} otherwise.
     */
    @Nonnull
    public Optional<Boolean> getMaybeBoolean( final int index )
    {
        return getMaybe(index).filter(Boolean.class::isInstance).map(Boolean.class::cast);
    }

    /**
     * Returns the item at the given {@code index} as an {@code int}.
     *
     * @param index
     *            The list index of the item.
     * @return The item at the given {@code index} as an {@code int}.
     * @throws IndexOutOfBoundsException
     *             Thrown if the given {@code index} is out of bounds.
     * @throws ValueCastException
     *             Thrown if the item is not an {@code int}.
     */
    public int getInteger( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        return getNumber(index).intValue();
    }

    /**
     * Returns an {@link Optional}, which may contain an {@link Integer} item that is stored at the given {@code index}.
     * The returned {@link Optional} is empty if the item is either not an {@link Integer} or does not even exist in the
     * first place.
     *
     * @param index
     *            The list index of the item.
     * @return An {@link Optional} that contains the item if it exists and actually is an {@link Integer},
     *         {@link Optional#empty()} otherwise.
     */
    @Nonnull
    public Optional<Integer> getMaybeInteger( final int index )
    {
        return getMaybeNumber(index).map(Number::intValue);
    }

    /**
     * Returns the item at the given {@code index} as a {@code double}.
     *
     * @param index
     *            The list index of the item.
     * @return The item at the given {@code index} as a {@code double}.
     * @throws IndexOutOfBoundsException
     *             Thrown if the given {@code index} is out of bounds.
     * @throws ValueCastException
     *             Thrown if the item is not a {@code double}.
     */
    public double getDouble( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        return getNumber(index).doubleValue();
    }

    /**
     * Returns an {@link Optional}, which may contain a {@link Double} item that is stored at the given {@code index}.
     * The returned {@link Optional} is empty if the item is either not a {@link Double} or does not even exist in the
     * first place.
     *
     * @param index
     *            The list index of the item.
     * @return An {@link Optional} that contains the item if it exists and actually is a {@link Double},
     *         {@link Optional#empty()} otherwise.
     */
    @Nonnull
    public Optional<Double> getMaybeDouble( final int index )
    {
        return getMaybeNumber(index).map(Number::doubleValue);
    }

    /**
     * Returns the item at the given {@code index} as a {@link Number}.
     *
     * @param index
     *            The list index of the item.
     * @return The item at the given {@code index} as a {@link Number}.
     * @throws IndexOutOfBoundsException
     *             Thrown if the given {@code index} is out of bounds.
     * @throws ValueCastException
     *             Thrown if the item is not a {@link Number}.
     */
    @Nonnull
    public Number getNumber( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        final Object value = get(index);

        if( value instanceof Number ) {
            return (Number) value;
        }

        throw new ValueCastException(Number.class, value);
    }

    /**
     * Returns an {@link Optional}, which may contain a {@link Number} item that is stored at the given {@code index}.
     * The returned {@link Optional} is empty if the item is either not a {@link Number} or does not even exist in the
     * first place.
     *
     * @param index
     *            The list index of the item.
     * @return An {@link Optional} that contains the item if it exists and actually is a {@link Number},
     *         {@link Optional#empty()} otherwise.
     */
    @Nonnull
    public Optional<Number> getMaybeNumber( final int index )
    {
        return getMaybe(index).filter(Number.class::isInstance).map(Number.class::cast);
    }

    /**
     * Returns the item at the given {@code index} as a {@link String}.
     *
     * @param index
     *            The list index of the item.
     * @return The item at the given {@code index} as a {@link String}.
     * @throws IndexOutOfBoundsException
     *             Thrown if the given {@code index} is out of bounds.
     * @throws ValueCastException
     *             Thrown if the item is not a {@link String}.
     */
    @Nonnull
    public String getString( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        final Object value = get(index);

        if( value instanceof String ) {
            return (String) value;
        }

        throw new ValueCastException(String.class, value);
    }

    /**
     * Returns an {@link Optional}, which may contain a {@link String} item that is stored at the given {@code index}.
     * The returned {@link Optional} is empty if the item is either not a {@link String} or does not even exist in the
     * first place.
     *
     * @param index
     *            The list index of the item.
     * @return An {@link Optional} that contains the item if it exists and actually is a {@link String},
     *         {@link Optional#empty()} otherwise.
     */
    @Nonnull
    public Optional<String> getMaybeString( final int index )
    {
        return getMaybe(index).filter(String.class::isInstance).map(String.class::cast);
    }

    /**
     * Returns the item at the given {@code index} as a {@link TypedMapView}.
     *
     * @param index
     *            The list index of the item.
     * @return The item at the given {@code index} as a {@link TypedMapView}.
     * @throws IndexOutOfBoundsException
     *             Thrown if the given {@code index} is out of bounds.
     * @throws ValueCastException
     *             Thrown if the item is not a {@link TypedMapView}.
     */
    @Nonnull
    public TypedMapView getMapView( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        final Object value = get(index);

        if( value instanceof TypedMapView ) {
            return (TypedMapView) value;
        }

        throw new ValueCastException(TypedMapView.class, value);
    }

    /**
     * Returns an {@link Optional}, which may contain a {@link TypedMapView} item that is stored at the given
     * {@code index}. The returned {@link Optional} is empty if the item is either not a {@link TypedMapView} or does
     * not even exist in the first place.
     *
     * @param index
     *            The list index of the item.
     * @return An {@link Optional} that contains the item if it exists and actually is a {@link TypedMapView},
     *         {@link Optional#empty()} otherwise.
     */
    @Nonnull
    public Optional<TypedMapView> getMaybeMapView( final int index )
    {
        return getMaybe(index).filter(TypedMapView.class::isInstance).map(TypedMapView.class::cast);
    }

    /**
     * Returns the item at the given {@code index} as a {@link TypedListView}.
     *
     * @param index
     *            The list index of the item.
     * @return The item at the given {@code index} as a {@link TypedListView}.
     * @throws IndexOutOfBoundsException
     *             Thrown if the given {@code index} is out of bounds.
     * @throws ValueCastException
     *             Thrown if the item is not a {@link TypedListView}.
     */
    @Nonnull
    public TypedListView getListView( final int index )
        throws IndexOutOfBoundsException,
            ValueCastException
    {
        final Object value = get(index);

        if( value instanceof TypedListView ) {
            return (TypedListView) value;
        }

        throw new ValueCastException(TypedListView.class, value);
    }

    /**
     * Returns an {@link Optional}, which may contain a {@link TypedListView} item that is stored at the given
     * {@code index}. The returned {@link Optional} is empty if the item is either not a {@link TypedListView} or does
     * not even exist in the first place.
     *
     * @param index
     *            The list index of the item.
     * @return An {@link Optional} that contains the item if it exists and actually is a {@link TypedListView},
     *         {@link Optional#empty()} otherwise.
     */
    @Nonnull
    public Optional<TypedListView> getMaybeListView( final int index )
    {
        return getMaybe(index).filter(TypedListView.class::isInstance).map(TypedListView.class::cast);
    }

    /**
     * Returns all items that are of the given {@code itemType} - including sub-types.
     *
     * @param itemType
     *            The {@link Class} of the items that should be returned.
     * @param <T>
     *            The item type.
     * @return All items contained in this {@link TypedMapView} that are of (a sub-)type of the given {@code itemType}.
     */
    @SuppressWarnings( "unchecked" )
    @Nonnull
    public <T> List<T> getItems( @Nonnull final Class<? extends T> itemType )
    {
        return list
            .stream()
            .filter(item -> item != null && itemType.isAssignableFrom(item.getClass()))
            .map(item -> (T) item)
            .collect(Collectors.toList());
    }
}
