package com.sap.cloud.environment.servicebinding.api;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface EqualityFilterBuilder<T>
{
    class Default<T> implements EqualityFilterBuilder<T>
    {
        @Nonnull
        private final ServiceBindingPropertySelector<T> propertySelector;

        Default( @Nonnull final ServiceBindingPropertySelector<T> propertySelector )
        {
            this.propertySelector = propertySelector;
        }

        @Nonnull
        @Override
        public
            ServiceBindingFilter
            isEqualTo( @Nullable final T value, @Nonnull final EqualityComparison<T> equalityComparison )
        {
            return binding -> equalityComparison.areEqual(propertySelector.select(binding), value);
        }
    }

    class StringImpl implements EqualityFilterBuilder<String>
    {
        @Nonnull
        private final Default<String> delegate;

        StringImpl( @Nonnull final ServiceBindingPropertySelector<String> propertySelector )
        {
            delegate = new Default<>(propertySelector);
        }

        @Nonnull
        @Override
        public
            ServiceBindingFilter
            isEqualTo( @Nullable final String value, @Nonnull final EqualityComparison<String> equalityComparison )
        {
            return delegate.isEqualTo(value, equalityComparison);
        }

        @Nonnull
        @Override
        public ServiceBindingFilter isEqualTo( @Nullable final String value )
        {
            return isEqualTo(value, EqualityComparison.String.CASE_INSENSITIVE);
        }
    }

    @Nonnull
    ServiceBindingFilter isEqualTo( @Nullable final T value, @Nonnull final EqualityComparison<T> equalityComparison );

    @Nonnull
    default ServiceBindingFilter isEqualTo( @Nullable final T value )
    {
        return isEqualTo(value, Objects::equals);
    }

    @Nonnull
    default ServiceBindingFilter isNull()
    {
        return isEqualTo(null);
    }
}
