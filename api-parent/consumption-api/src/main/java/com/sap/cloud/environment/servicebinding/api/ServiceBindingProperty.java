package com.sap.cloud.environment.servicebinding.api;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ServiceBindingProperty
{
    public static final EqualityFilterBuilder<String> NAME =
        new EqualityFilterBuilder.StringImpl(binding -> binding.getName().orElse(null));

    public static final EqualityFilterBuilder<String> SERVICE_NAME =
        new EqualityFilterBuilder.StringImpl(binding -> binding.getServiceName().orElse(null));

    public static final EqualityFilterBuilder<String> SERVICE_PLAN =
        new EqualityFilterBuilder.StringImpl(binding -> binding.getServicePlan().orElse(null));

    public static final StringCollectionFilterBuilder TAGS = new StringCollectionFilterBuilder(ServiceBinding::getTags);

    public static final class StringCollectionFilterBuilder
        implements
        CollectionFilterBuilder<String>,
        EqualityFilterBuilder<Collection<String>>
    {
        @Nonnull
        private final CollectionFilterBuilder.StringImpl collectionDelegate;
        @Nonnull
        private final EqualityFilterBuilder<Collection<String>> equalityDelegate;

        StringCollectionFilterBuilder(
            @Nonnull final ServiceBindingPropertySelector<Collection<String>> propertySelector )
        {
            collectionDelegate = new CollectionFilterBuilder.StringImpl(propertySelector);
            equalityDelegate = new EqualityFilterBuilder.Default<>(propertySelector);
        }

        @Nonnull
        @Override
        public
            ServiceBindingFilter
            contains( @Nullable final String item, @Nonnull final EqualityComparison<String> equalityComparison )
        {
            return collectionDelegate.contains(item, equalityComparison);
        }

        @Nonnull
        @Override
        public ServiceBindingFilter contains( @Nullable final String item )
        {
            return collectionDelegate.contains(item);
        }

        @Nonnull
        @Override
        public ServiceBindingFilter isEqualTo(
            @Nullable final Collection<String> value,
            @Nonnull final EqualityComparison<Collection<String>> equalityComparison )
        {
            return equalityDelegate.isEqualTo(value, equalityComparison);
        }

        @Nonnull
        @Override
        public ServiceBindingFilter isEqualTo( @Nullable final Collection<String> value )
        {
            return equalityDelegate.isEqualTo(value);
        }
    }

    private ServiceBindingProperty()
    {
        throw new IllegalStateException("This static utility class must not be instantiated!");
    }
}
