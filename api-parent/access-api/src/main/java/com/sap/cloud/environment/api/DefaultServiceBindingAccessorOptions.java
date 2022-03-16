/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class DefaultServiceBindingAccessorOptions implements ServiceBindingAccessorOptions
{
    private final Map<String, Object> options;

    public DefaultServiceBindingAccessorOptions( @Nonnull final Map<String, Object> options )
    {
        this.options = options;
    }

    public static ServiceBindingAccessorOptions.Builder builder()
    {
        return new Builder();
    }

    @Override
    public boolean containsKey( @Nonnull final String key )
    {
        return options.containsKey(key);
    }

    @SuppressWarnings( "unchecked" )
    @Nonnull
    @Override
    public <T> Optional<T> getValue( @Nonnull final String key )
    {
        try {
            return Optional.ofNullable((T) options.get(key));
        } catch (final ClassCastException e) {
            return Optional.empty();
        }
    }

    private static class Builder implements ServiceBindingAccessorOptions.Builder
    {
        @Nonnull
        private final Map<String, Object> options = new HashMap<>();

        @Nonnull
        @Override
        public ServiceBindingAccessorOptions.Builder withOption( @Nonnull final String key,
                                                                 @Nullable final Object value )
        {
            options.put(key, value);
            return this;
        }

        @Nonnull
        @Override
        public ServiceBindingAccessorOptions.Builder withOption( @Nonnull final Consumer<ServiceBindingAccessorOptions.Builder> optionSetter )
        {
            optionSetter.accept(this);
            return this;
        }

        @Nonnull
        @Override
        public ServiceBindingAccessorOptions build()
        {
            return new DefaultServiceBindingAccessorOptions(options);
        }
    }
}
