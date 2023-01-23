package com.sap.cloud.environment.servicebinding.api;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface CollectionFilterBuilder<T>
{
    class Default<T> implements CollectionFilterBuilder<T>
    {
        @Nonnull
        private final ServiceBindingPropertySelector<Collection<T>> propertySelector;

        Default( @Nonnull final ServiceBindingPropertySelector<Collection<T>> propertySelector )
        {
            this.propertySelector = propertySelector;
        }

        @Nonnull
        @Override
        public
            ServiceBindingFilter
            contains( @Nullable final T item, @Nonnull final EqualityComparison<T> equalityComparison )
        {
            return binding -> {
                @Nullable
                final Collection<T> maybeItems = propertySelector.select(binding);
                if( maybeItems == null || maybeItems.isEmpty() ) {
                    return false;
                }

                return maybeItems.stream().anyMatch(i -> equalityComparison.areEqual(i, item));
            };
        }
    }

    class StringImpl implements CollectionFilterBuilder<String>
    {
        @Nonnull
        private final Default<String> delegate;

        StringImpl( @Nonnull final ServiceBindingPropertySelector<Collection<String>> propertySelector )
        {
            delegate = new Default<>(propertySelector);
        }

        @Nonnull
        @Override
        public
            ServiceBindingFilter
            contains( @Nullable final String item, @Nonnull final EqualityComparison<String> equalityComparison )
        {
            return delegate.contains(item, equalityComparison);
        }

        @Nonnull
        @Override
        public ServiceBindingFilter contains( @Nullable final String item )
        {
            return contains(item, EqualityComparison.String.CASE_INSENSITIVE);
        }
    }

    @Nonnull
    ServiceBindingFilter contains( @Nullable final T item, @Nonnull final EqualityComparison<T> equalityComparison );

    @Nonnull
    default ServiceBindingFilter contains( @Nullable final T item )
    {
        return contains(item, Objects::equals);
    }
}
