package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import com.sap.cloud.environment.api.exception.UnsupportedPropertyTypeException;

class DefaultServiceBindingBuilder implements DefaultServiceBinding.MapSelectionBuilder, DefaultServiceBinding.TerminalBuilder
{
    private static final Collection<Class<?>> SUPPORTED_VALUE_TYPES = Arrays.asList(Boolean.class, Number.class, String.class, Map.class, List.class);

    @Nonnull
    private Map<String, Object> map = Collections.emptyMap();
    @Nonnull
    private Function<ServiceBinding, String> nameResolver = resolveToNull();
    @Nonnull
    private Function<ServiceBinding, String> serviceNameResolver = resolveToNull();
    @Nonnull
    private Function<ServiceBinding, String> servicePlanResolver = resolveToNull();
    @Nonnull
    private Function<ServiceBinding, List<String>> tagsResolver = resolveToNull();
    @Nonnull
    private Function<ServiceBinding, Map<String, Object>> credentialsResolver = resolveToNull();

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder copy( @Nonnull final Map<String, Object> properties )
    {
        map = copyMap(properties, Collections::unmodifiableMap, Collections::unmodifiableList);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withNameKey( @Nonnull final String key )
    {
        nameResolver = resolveString(key);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withNameResolver( @Nonnull final Function<ServiceBinding, String> resolver )
    {
        nameResolver = resolver;
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withServiceNameKey( @Nonnull final String key )
    {
        serviceNameResolver = resolveString(key);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withServiceNameResolver( @Nonnull final Function<ServiceBinding, String> resolver )
    {
        serviceNameResolver = resolver;
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withServicePlanKey( @Nonnull final String key )
    {
        servicePlanResolver = resolveString(key);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withServicePlanResolver( @Nonnull final Function<ServiceBinding, String> resolver )
    {
        servicePlanResolver = resolver;
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withTagsKey( @Nonnull final String key )
    {
        tagsResolver = resolveStringList(key);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withTagsResolver( @Nonnull final Function<ServiceBinding, List<String>> resolver )
    {
        tagsResolver = resolver;
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withCredentialsKey( @Nonnull final String key )
    {
        credentialsResolver = resolveStringMap(key);
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding.TerminalBuilder withCredentialsResolver( @Nonnull final Function<ServiceBinding, Map<String, Object>> resolver )
    {
        credentialsResolver = resolver;
        return this;
    }

    @Nonnull
    @Override
    public DefaultServiceBinding build()
    {
        return new DefaultServiceBinding(map, nameResolver, serviceNameResolver, servicePlanResolver, tagsResolver, credentialsResolver);
    }

    @Nonnull
    @SuppressWarnings( "unchecked" )
    private static Map<String, Object> copyRawMap( @Nonnull final Object rawMap,
                                                   @Nonnull final Function<Map<String, Object>, Map<String, Object>> mapDecorator,
                                                   @Nonnull final Function<List<Object>, List<Object>> listDecorator ) throws ClassCastException
    {
        return copyMap((Map<String, Object>) rawMap, mapDecorator, listDecorator);
    }

    @Nonnull
    static Map<String, Object> copyMap( @Nonnull final Map<String, Object> map,
                                        @Nonnull final Function<Map<String, Object>, Map<String, Object>> mapDecorator,
                                        @Nonnull final Function<List<Object>, List<Object>> listDecorator )
    {
        final Map<String, Object> copiedMap = new TreeMap<>(String::compareToIgnoreCase);
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            @Nullable final String key = entry.getKey();
            @Nullable final Object value = entry.getValue();

            if (key == null) {
                continue;
            }

            if (value == null) {
                copiedMap.put(key, null);
                continue;
            }

            if (!isSupportedType(value.getClass())) {
                throw new UnsupportedPropertyTypeException(value.getClass());
            }

            if (value instanceof Map) {
                copiedMap.put(key, mapDecorator.apply(copyRawMap(value, mapDecorator, listDecorator)));
                continue;
            }

            if (value instanceof List) {
                copiedMap.put(key, listDecorator.apply(copyRawList(value, mapDecorator, listDecorator)));
                continue;
            }

            copiedMap.put(key, value);
        }

        return mapDecorator.apply(copiedMap);
    }

    @Nonnull
    @SuppressWarnings( "unchecked" )
    private static List<Object> copyRawList( @Nonnull final Object rawCollection,
                                             @Nonnull final Function<Map<String, Object>, Map<String, Object>> mapDecorator,
                                             @Nonnull final Function<List<Object>, List<Object>> listDecorator ) throws ClassCastException
    {
        return copyList((List<Object>) rawCollection, mapDecorator, listDecorator);
    }

    @Nonnull
    private static List<Object> copyList( @Nonnull final List<Object> list,
                                          @Nonnull final Function<Map<String, Object>, Map<String, Object>> mapDecorator,
                                          @Nonnull final Function<List<Object>, List<Object>> listDecorator )
    {
        final List<Object> copiedList = new ArrayList<>(list.size());
        for (final Object element : list) {

            if (element == null) {
                copiedList.add(null);
                continue;
            }

            if (!isSupportedType(element.getClass())) {
                throw new UnsupportedPropertyTypeException(element.getClass());
            }

            if (element instanceof Map) {
                copiedList.add(mapDecorator.apply(copyRawMap(element, mapDecorator, listDecorator)));
                continue;
            }

            if (element instanceof List) {
                copiedList.add(listDecorator.apply(copyRawList(element, mapDecorator, listDecorator)));
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
    private static Function<ServiceBinding, String> resolveString( @Nonnull final String key )
    {
        return binding -> {
            try {
                return (String) binding.get(key).orElse(null);
            } catch (final ClassCastException e) {
                return null;
            }
        };
    }

    @Nonnull
    @SuppressWarnings( "unchecked" )
    private static Function<ServiceBinding, List<String>> resolveStringList( @Nonnull final String key )
    {
        return binding -> {
            try {
                return (List<String>) binding.get(key).orElse(Collections.emptyList());
            } catch (final ClassCastException e) {
                return Collections.emptyList();
            }
        };
    }

    @Nonnull
    @SuppressWarnings( "unchecked" )
    private static Function<ServiceBinding, Map<String, Object>> resolveStringMap( @Nonnull final String key )
    {
        return binding -> {
            try {
                return (Map<String, Object>) binding.get(key).orElse(Collections.emptyMap());
            } catch (final ClassCastException e) {
                return Collections.emptyMap();
            }
        };
    }

    @Nonnull
    private static <T> Function<ServiceBinding, T> resolveToNull()
    {
        return binding -> null;
    }
}
