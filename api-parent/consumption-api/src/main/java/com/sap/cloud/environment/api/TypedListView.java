/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sap.cloud.environment.api.exception.ValueCastException;

public final class TypedListView {
    @Nonnull
    public static TypedListView of(@Nonnull final Collection<Object> list) {
        final List<Object> elements = new ArrayList<>(list.size());
        for (final Object element : list) {
            if (element instanceof Map) {
                elements.add(TypedMapView.of(element));
                continue;
            }

            if (element instanceof Collection) {
                elements.add(of(element));
                continue;
            }

            elements.add(element);
        }

        return new TypedListView(elements);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    static TypedListView of(@Nonnull final Object rawList) {
        try {
            return of((Collection<Object>) rawList);
        } catch (final ClassCastException e) {
            throw new ValueCastException();
        }
    }

    @Nonnull
    private final List<Object> list;

    private TypedListView(@Nonnull final List<Object> list) {
        this.list = list;
    }

    public int getSize() {
        return list.size();
    }

    @Nullable
    public Object get(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= getSize()) {
            throw new IndexOutOfBoundsException();
        }

        return list.get(index);
    }

    public boolean getBoolean(final int index) throws IndexOutOfBoundsException, ValueCastException {
        final Object value = get(index);

        if (value instanceof Boolean) {
            return (boolean) value;
        }

        throw new ValueCastException();
    }

    public int getInteger(final int index) throws IndexOutOfBoundsException, ValueCastException {
        return getNumber(index).intValue();
    }

    public double getDouble(final int index) throws IndexOutOfBoundsException, ValueCastException {
        return getNumber(index).doubleValue();
    }

    @Nonnull
    public Number getNumber(final int index) throws IndexOutOfBoundsException, ValueCastException {
        final Object value = get(index);

        if (value instanceof Number) {
            return (Number) value;
        }

        throw new ValueCastException();
    }

    @Nonnull
    public String getString(final int index) throws IndexOutOfBoundsException, ValueCastException {
        final Object value = get(index);

        if (value instanceof String) {
            return (String) value;
        }

        throw new ValueCastException();
    }

    @Nonnull
    public TypedMapView getMapView(final int index) throws IndexOutOfBoundsException, ValueCastException {
        final Object value = get(index);

        if (value instanceof TypedMapView) {
            return (TypedMapView) value;
        }

        throw new ValueCastException();
    }

    @Nonnull
    public TypedListView getListView(final int index) throws IndexOutOfBoundsException, ValueCastException {
        final Object value = get(index);

        if (value instanceof TypedListView) {
            return (TypedListView) value;
        }

        throw new ValueCastException();
    }
}
