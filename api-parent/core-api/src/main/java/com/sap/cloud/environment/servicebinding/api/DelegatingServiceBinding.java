/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings( { "OptionalUsedAsFieldOrParameterType", "OptionalContainsCollection" } )
class DelegatingServiceBinding implements ServiceBinding
{
    @Nonnull
    private final ServiceBinding delegate;

    // CAUTION:
    // We are consciously violating multiple best-practices in the code below:
    // 1. You shouldn't use `Optional` as a field type.
    // 2. You shouldn't use `@Nullable Optional` anywhere.
    //
    // We are still doing it because we need a way to distinguish between these cases:
    // 1. The user doesn't want to change a property. It should be delegated instead (symbolized by `null`).
    // 2. The user wants to set a property explicitly to `null` (i.e. they want to "delete" a property from the Service Binding, symbolized by `Optional.empty()`).
    // 3. The user wants to set a property to a specific value (i.e. they want to "overwrite" a property, symbolized by `Optional.of(value)`).
    //
    // We could have used `@Nonnull Optional<Optional<...>>` instead, but that would have been even worse in my opinion.
    @Nullable
    private final Optional<Map<String, Object>> properties;
    @Nullable
    private final Optional<String> name;
    @Nullable
    private final Optional<String> serviceName;
    @Nullable
    private final Optional<ServiceIdentifier> serviceIdentifier;
    @Nullable
    private final Optional<String> servicePlan;
    @Nullable
    private final Optional<List<String>> tags;
    @Nullable
    private final Optional<Map<String, Object>> credentials;

    DelegatingServiceBinding(
        @Nonnull final ServiceBinding delegate,
        @Nullable final Optional<Map<String, Object>> properties,
        @Nullable final Optional<String> name,
        @Nullable final Optional<String> serviceName,
        @Nullable final Optional<ServiceIdentifier> serviceIdentifier,
        @Nullable final Optional<String> servicePlan,
        @Nullable final Optional<List<String>> tags,
        @Nullable final Optional<Map<String, Object>> credentials )
    {
        this.delegate = delegate;
        this.properties = properties;
        this.name = name;
        this.serviceName = serviceName;
        this.serviceIdentifier = serviceIdentifier;
        this.servicePlan = servicePlan;
        this.tags = tags;
        this.credentials = credentials;
    }

    @Nonnull
    @Override
    public Set<String> getKeys()
    {
        if( properties == null ) {
            // the `properties` have NOT been modified
            return delegate.getKeys();
        }

        if( !properties.isPresent() ) {
            // the `properties` have been set EXPLICITLY to `null`
            return Collections.emptySet();
        }

        // the `properties` have been EXPLICITLY overwritten
        return Collections.unmodifiableSet(properties.get().keySet());
    }

    @Override
    public boolean containsKey( @Nonnull String key )
    {
        return getKeys().contains(key);
    }

    @Nonnull
    @Override
    public Optional<Object> get( @Nonnull String key )
    {
        if( properties == null ) {
            // the `properties` have NOT been modified
            return delegate.get(key);
        }

        if( !properties.isPresent() ) {
            // the `properties` have been set EXPLICITLY to `null`
            return Optional.empty();
        }

        // the `properties` have been EXPLICITLY overwritten
        return Optional.ofNullable(properties.get().get(key));
    }

    @Nonnull
    @Override
    public Optional<String> getName()
    {
        if( name == null ) {
            // the `name` has NOT been modified
            return delegate.getName();
        }

        return name;
    }

    @Nonnull
    @Override
    public Optional<String> getServiceName()
    {
        if( serviceName == null ) {
            // the `serviceName` has NOT been modified
            return delegate.getServiceName();
        }

        return serviceName;
    }

    @Nonnull
    @Override
    public Optional<ServiceIdentifier> getServiceIdentifier()
    {
        if( serviceIdentifier == null ) {
            // the `serviceIdentifier` has NOT been modified
            return delegate.getServiceIdentifier();
        }

        return serviceIdentifier;
    }

    @Nonnull
    @Override
    public Optional<String> getServicePlan()
    {
        if( servicePlan == null ) {
            // the `servicePlan` has NOT been modified
            return delegate.getServicePlan();
        }

        return servicePlan;
    }

    @Nonnull
    @Override
    public List<String> getTags()
    {
        if( tags == null ) {
            // the `tags` have NOT been modified
            return delegate.getTags();
        }

        if( !tags.isPresent() ) {
            // the `tags` have been set EXPLICITLY to `null`
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(tags.get());
    }

    @Nonnull
    @Override
    public Map<String, Object> getCredentials()
    {
        if( credentials == null ) {
            // the `credentials` have NOT been modified
            return delegate.getCredentials();
        }

        if( !credentials.isPresent() ) {
            // the `credentials` have been set EXPLICITLY to `null`
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(credentials.get());
    }

    @Override
    public int hashCode()
    {
        return Objects
            .hash(
                getName(),
                getServiceName(),
                getServiceIdentifier(),
                getServicePlan(),
                getTags(),
                getCredentials(),
                properties);
    }

    @Override
    public boolean equals( final Object o )
    {
        if( this == o ) {
            return true;
        }
        if( o == null || getClass() != o.getClass() ) {
            return false;
        }

        final DelegatingServiceBinding that = (DelegatingServiceBinding) o;

        return getName().equals(that.getName())
            && getServiceName().equals(that.getServiceName())
            && getServicePlan().equals(that.getServicePlan())
            && getServiceIdentifier().equals(that.getServiceIdentifier())
            && getTags().equals(that.getTags())
            && getCredentials().equals(that.getCredentials())
            && Objects.equals(properties, that.properties);
    }

    @Nonnull
    static Builder builder( @Nonnull final ServiceBinding delegate )
    {
        return new Builder(delegate);
    }

    static class Builder
    {
        @Nonnull
        private final ServiceBinding delegate;
        @Nullable
        private Optional<Map<String, Object>> properties;
        @Nullable
        private Optional<String> name;
        @Nullable
        private Optional<String> serviceName;
        @Nullable
        private Optional<ServiceIdentifier> serviceIdentifier;
        @Nullable
        private Optional<String> servicePlan;
        @Nullable
        private Optional<List<String>> tags;
        @Nullable
        private Optional<Map<String, Object>> credentials;

        Builder( @Nonnull final ServiceBinding delegate )
        {
            this.delegate = delegate;
        }

        @Nonnull
        Builder withProperties( @Nullable final Map<String, Object> properties )
        {
            @Nullable
            final Map<String, Object> copiedProperties;
            if( properties == null ) {
                copiedProperties = null;
            } else {
                copiedProperties =
                    DefaultServiceBindingBuilder
                        .copyMap(properties, Collections::unmodifiableMap, Collections::unmodifiableList);
            }

            this.properties = Optional.ofNullable(copiedProperties);
            return this;
        }

        @Nonnull
        Builder withName( @Nullable final String name )
        {
            this.name = Optional.ofNullable(name);
            return this;
        }

        @Nonnull
        Builder withServiceName( @Nullable final String serviceName )
        {
            this.serviceName = Optional.ofNullable(serviceName);
            return this;
        }

        @Nonnull
        Builder withServiceIdentifier( @Nullable final ServiceIdentifier serviceIdentifier )
        {
            this.serviceIdentifier = Optional.ofNullable(serviceIdentifier);
            return this;
        }

        @Nonnull
        Builder withServicePlan( @Nullable final String servicePlan )
        {
            this.servicePlan = Optional.ofNullable(servicePlan);
            return this;
        }

        @Nonnull
        Builder withTags( @Nullable final List<String> tags )
        {
            @Nullable
            final List<String> copiedTags;
            if( tags == null ) {
                copiedTags = null;
            } else {
                copiedTags = new ArrayList<>(tags);
            }

            this.tags = Optional.ofNullable(copiedTags);
            return this;
        }

        @Nonnull
        Builder withCredentials( @Nullable final Map<String, Object> credentials )
        {
            @Nullable
            final Map<String, Object> copiedCredentials;
            if( credentials == null ) {
                copiedCredentials = null;
            } else {
                copiedCredentials =
                    DefaultServiceBindingBuilder
                        .copyMap(credentials, Collections::unmodifiableMap, Collections::unmodifiableList);
            }

            this.credentials = Optional.ofNullable(copiedCredentials);
            return this;
        }

        @Nonnull
        ServiceBinding build()
        {
            return new DelegatingServiceBinding(
                delegate,
                properties,
                name,
                serviceName,
                serviceIdentifier,
                servicePlan,
                tags,
                credentials);
        }
    }
}
