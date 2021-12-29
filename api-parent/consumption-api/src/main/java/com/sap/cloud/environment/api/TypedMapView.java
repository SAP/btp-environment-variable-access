/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.sap.cloud.environment.api.exception.KeyNotFoundException;
import com.sap.cloud.environment.api.exception.ValueCastException;

public final class TypedMapView {
    @Nonnull
    public static TypedMapView of(@Nonnull final ServiceBinding serviceBinding) {
        return of(serviceBinding.toMap());
    }

    @Nonnull
    public static TypedMapView of(@Nonnull final Map<String, Object> map) {
        final Map<String, Object> properties = new HashMap<>(map.size());
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            if (key == null) {
                continue;
            }

            if (value instanceof Map) {
                properties.put(key, of(value));
                continue;
            }

            if (value instanceof Collection) {
                properties.put(key, TypedListView.of(value));
                continue;
            }

            properties.put(key, value);
        }

        return new TypedMapView(properties);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    static TypedMapView of(@Nonnull final Object plainMap) {
        try {
            return of((Map<String, Object>) plainMap);
        } catch (final ClassCastException e) {
            throw new ValueCastException();
        }
    }

    @Nonnull
    private final Map<String, Object> map;

    private TypedMapView(@Nonnull final Map<String, Object> map) {
        this.map = map;
    }

    @Nonnull
    public Stream<String> getKeys() {
        return map.keySet().stream();
    }

    public boolean containsKey(@Nonnull final String key) {
        return map.containsKey(key);
    }

    @Nullable
    public Object get(@Nonnull final String key) throws KeyNotFoundException {
        if (!containsKey(key)) {
            throw new KeyNotFoundException();
        }

        return map.get(key);
    }

    public boolean getBoolean(@Nonnull final String key) throws KeyNotFoundException, ValueCastException {
        final Object value = get(key);

        if (value instanceof Boolean) {
            return (boolean) value;
        }

        throw new ValueCastException();
    }

    public int getInteger(@Nonnull final String key) throws KeyNotFoundException, ValueCastException {
        return getNumber(key).intValue();
    }

    public double getDouble(@Nonnull final String key) throws KeyNotFoundException, ValueCastException {
        return getNumber(key).doubleValue();
    }

    @Nonnull
    public Number getNumber(@Nonnull final String key) throws KeyNotFoundException, ValueCastException {
        final Object value = get(key);

        if (value instanceof Number) {
            return (Number) value;
        }

        throw new ValueCastException();
    }

    @Nonnull
    public String getString(@Nonnull final String key) throws KeyNotFoundException, ValueCastException {
        final Object value = get(key);

        if (value instanceof String) {
            return (String) value;
        }

        throw new ValueCastException();
    }

    @Nonnull
    public TypedMapView getMapView(@Nonnull final String key) throws KeyNotFoundException, ValueCastException {
        final Object value = get(key);

        if (value instanceof TypedMapView) {
            return (TypedMapView) value;
        }

        throw new ValueCastException();
    }

    @Nonnull
    public TypedListView getListView(@Nonnull final String key) throws KeyNotFoundException, ValueCastException {
        final Object value = get(key);

        if (value instanceof TypedListView) {
            return (TypedListView) value;
        }

        throw new ValueCastException();
    }
}
