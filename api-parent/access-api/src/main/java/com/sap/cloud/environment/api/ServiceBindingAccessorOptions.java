/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public interface ServiceBindingAccessorOptions
{
    ServiceBindingAccessorOptions NONE = new ServiceBindingAccessorOptions()
    {
        @Override
        public boolean containsKey( @Nonnull final String key )
        {
            return false;
        }

        @Nonnull
        @Override
        public <T> Optional<T> getValue( @Nonnull final String key )
        {
            return Optional.empty();
        }
    };

    boolean containsKey( @Nonnull final String key );

    @Nonnull
    <T> Optional<T> getValue( @Nonnull final String key );

    interface Builder
    {
        @Nonnull
        Builder withOption( @Nonnull final String key, @Nullable final Object value );

        @Nonnull
        Builder withOption( @Nonnull final Consumer<Builder> optionSetter );

        @Nonnull
        ServiceBindingAccessorOptions build();
    }
}
