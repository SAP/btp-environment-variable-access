package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DefaultServiceBinding implements ServiceBinding {

    @Nonnull
    public static DefaultServiceBinding wrapUnmodifiableMap(@Nonnull final Map<String, Object> map) {
        return new DefaultServiceBinding(map);
    }

    @Nonnull
    public static DefaultServiceBinding copyOf(@Nonnull final Map<String, Object> map)
    {
        return new DefaultServiceBinding(copyMap(map));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private static Map<String, Object> copyRawMap(@Nonnull final Object rawMap) throws ClassCastException
    {
        return copyMap((Map<String, Object>) rawMap);
    }

    @Nonnull
    private static Map<String, Object> copyMap(@Nonnull final Map<String, Object> map) {
        final Map<String, Object> copiedMap = new HashMap<>(map.size());
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            @Nullable
            final String key = entry.getKey();
            @Nullable
            final Object value = entry.getValue();

            if (key == null) {
                continue;
            }

            if (value instanceof Map) {
                copiedMap.put(key, copyRawMap(value));
                continue;
            }

            if (value instanceof Collection) {
                copiedMap.put(key, copyRawCollection(value));
                continue;
            }

            copiedMap.put(key, value);
        }

        return copiedMap;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private static Collection<Object> copyRawCollection(@Nonnull final Object rawCollection) throws ClassCastException
    {
        return copyCollection((Collection<Object>) rawCollection);
    }

    @Nonnull
    private static Collection<Object> copyCollection(@Nonnull final Collection<Object> collection)
    {
        final Collection<Object> copiedCollection = new ArrayList<>(collection.size());
        for (final Object element : collection) {
            if (element instanceof Map) {
                copiedCollection.add(copyRawMap(element));
                continue;
            }

            if (element instanceof Collection) {
                copiedCollection.add(copyRawCollection(element));
                continue;
            }

            copiedCollection.add(element);
        }

        return copiedCollection;
    }

    @Nonnull
    private final Map<String, Object> map;

    private DefaultServiceBinding(@Nonnull final Map<String, Object> map)
    {
        this.map = map;
    }

    @Nonnull
    @Override
    public Iterable<String> getKeys() {
        return map.keySet();
    }

    @Nonnull
    @Override
    public Iterable<Map.Entry<String, Object>> getEntries()
    {
        return map.entrySet();
    }

    @Override
    public boolean containsKey(@Nonnull final String key) {
        return map.containsKey(key);
    }

    @Nullable
    @Override
    public Object get(@Nonnull final String key) {
        return map.get(key);
    }

    @Nonnull
    @Override
    public Map<String, Object> toMap() {
        return copyMap(map);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DefaultServiceBinding that = (DefaultServiceBinding) o;
        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }
}
