/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.sap.cloud.environment.servicebinding.api.exception.UnsupportedPropertyTypeException;

/**
 * A {@link com.sap.cloud.environment.servicebinding.api.DefaultServiceBinding.MapSelectionBuilder} and
 * {@link com.sap.cloud.environment.servicebinding.api.DefaultServiceBinding.TerminalBuilder} that can be used to build
 * {@link DefaultServiceBinding} instances.
 */
public class DefaultServiceBindingBuilder
    implements
    DefaultServiceBinding.MapSelectionBuilder,
    DefaultServiceBinding.TerminalBuilder
{
    private static final Collection<Class<?>> SUPPORTED_VALUE_TYPES =
        Arrays.asList(Boolean.class, Number.class, String.class, Map.class, Iterable.class);

    @Nonnull
    private Map<String, Object> map = Collections.emptyMap();

    @Nullable
    private String name;

    @Nullable
    private String serviceName;

    @Nullable
    private String servicePlan;

    @Nullable
    private List<String> tags;

    @Nullable
    private Map<String, Object> credentials;

    @Nonnull
    @SuppressWarnings( "unchecked" )
    private static Map<String, Object> copyRawMap(
        @Nonnull final Object rawMap,
        @Nonnull final Function<Map<String, Object>, Map<String, Object>> mapDecorator,
        @Nonnull final Function<List<Object>, List<Object>> listDecorator )
        throws ClassCastException
    {
        return copyMap((Map<String, Object>) rawMap, mapDecorator, listDecorator);
    }

    @Nonnull
    private static Map<String, Object> copyMap(
        @Nonnull final Map<String, Object> map,
        @Nonnull final Function<Map<String, Object>, Map<String, Object>> mapDecorator,
        @Nonnull final Function<List<Object>, List<Object>> listDecorator )
    {
        final Map<String, Object> copiedMap = new TreeMap<>(String::compareToIgnoreCase);
        for( final Map.Entry<String, Object> entry : map.entrySet() ) {
            @Nullable
            final String key = entry.getKey();
            @Nullable
            final Object value = entry.getValue();

            if( key == null ) {
                continue;
            }

            if( value == null ) {
                copiedMap.put(key, null);
                continue;
            }

            if( !isSupportedType(value.getClass()) ) {
                throw new UnsupportedPropertyTypeException(value.getClass());
            }

            if( value instanceof Map ) {
                copiedMap.put(key, mapDecorator.apply(copyRawMap(value, mapDecorator, listDecorator)));
                continue;
            }

            if( value instanceof Iterable ) {
                copiedMap.put(key, listDecorator.apply(copyRawIterable(value, mapDecorator, listDecorator)));
                continue;
            }

            copiedMap.put(key, value);
        }

        return mapDecorator.apply(copiedMap);
    }

    @Nonnull
    @SuppressWarnings( "unchecked" )
    private static List<Object> copyRawIterable(
        @Nonnull final Object rawCollection,
        @Nonnull final Function<Map<String, Object>, Map<String, Object>> mapDecorator,
        @Nonnull final Function<List<Object>, List<Object>> listDecorator )
        throws ClassCastException
    {
        return copyIterable((Iterable<Object>) rawCollection, mapDecorator, listDecorator);
    }

    @Nonnull
    private static List<Object> copyIterable(
        @Nonnull final Iterable<Object> iterable,
        @Nonnull final Function<Map<String, Object>, Map<String, Object>> mapDecorator,
        @Nonnull final Function<List<Object>, List<Object>> listDecorator )
    {
        final List<Object> copiedList = new ArrayList<>();
        for( final Object element : iterable ) {

            if( element == null ) {
                copiedList.add(null);
                continue;
            }

            if( !isSupportedType(element.getClass()) ) {
                throw new UnsupportedPropertyTypeException(element.getClass());
            }

            if( element instanceof Map ) {
                copiedList.add(mapDecorator.apply(copyRawMap(element, mapDecorator, listDecorator)));
                continue;
            }

            if( element instanceof Iterable ) {
                copiedList.add(listDecorator.apply(copyRawIterable(element, mapDecorator, listDecorator)));
                continue;
            }

            copiedList.add(element);
        }

        return listDecorator.apply(copiedList);
    }

    private static boolean isSupportedType( @Nonnull final Class<?> valueType )
    {
        return SUPPORTED_VALUE_TYPES.stream().anyMatch(supportedType -> supportedType.isAssignableFrom(valueType));
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder copy( @Nonnull final Map<String, Object> properties )
    {
        map = copyMap(properties, Collections::unmodifiableMap, Collections::unmodifiableList);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withName( @Nonnull final String name )
    {
        this.name = name;
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withNameKey( @Nonnull final String key )
    {
        name = extractString(key);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withServiceName( @Nonnull final String serviceName )
    {
        this.serviceName = serviceName;
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withServiceNameKey( @Nonnull final String key )
    {
        serviceName = extractString(key);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withServicePlan( @Nonnull final String servicePlan )
    {
        this.servicePlan = servicePlan;
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withServicePlanKey( @Nonnull final String key )
    {
        servicePlan = extractString(key);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withTags( @Nonnull final Iterable<String> tags )
    {
        this.tags =
            Collections.unmodifiableList(StreamSupport.stream(tags.spliterator(), false).collect(Collectors.toList()));
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withTagsKey( @Nonnull final String key )
    {
        tags = extractStringList(key);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withCredentials( @Nonnull final Map<String, Object> credentials )
    {
        this.credentials = copyMap(credentials, Collections::unmodifiableMap, Collections::unmodifiableList);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withCredentialsKey( @Nonnull final String key )
    {
        credentials = extractMap(key);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding build()
    {
        return new DefaultServiceBinding(
            map,
            name,
            serviceName,
            servicePlan,
            tags == null ? Collections.emptyList() : tags,
            credentials == null ? Collections.emptyMap() : credentials);
    }

    @Nonnull
    @SuppressWarnings( "unchecked" )
    private Map<String, Object> extractMap( @Nonnull final String key )
    {
        final Object maybeValue = map.get(key);
        if( !(maybeValue instanceof Map) ) {
            return Collections.emptyMap();
        }

        try {
            return (Map<String, Object>) maybeValue;
        }
        catch( final ClassCastException e ) {
            return Collections.emptyMap();
        }
    }

    @Nonnull
    @SuppressWarnings( "unchecked" )
    private List<String> extractStringList( @Nonnull final String key )
    {
        final Object maybeValue = map.get(key);
        if( !(maybeValue instanceof List) ) {
            return Collections.emptyList();
        }

        try {
            return (List<String>) maybeValue;
        }
        catch( final ClassCastException e ) {
            return Collections.emptyList();
        }
    }

    @Nullable
    private String extractString( @Nonnull final String key )
    {
        final Object maybeValue = map.get(key);
        if( !(maybeValue instanceof String) ) {
            return null;
        }

        try {
            return (String) maybeValue;
        }
        catch( final ClassCastException e ) {
            return null;
        }
    }
}
