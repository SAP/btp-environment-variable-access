/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DefaultServiceBinding implements ServiceBinding
{
    @Nonnull
    private final Map<String, Object> map;
    @Nullable
    private final String name;
    @Nullable
    private final String serviceName;
    @Nullable
    private final String servicePlan;
    @Nonnull
    private final List<String> tags;
    @Nonnull
    private final Map<String, Object> credentials;

    DefaultServiceBinding( @Nonnull final Map<String, Object> map,
                           @Nullable final String name,
                           @Nullable final String serviceName,
                           @Nullable final String servicePlan,
                           @Nonnull final List<String> tags,
                           @Nonnull final Map<String, Object> credentials )
    {
        this.map = map;
        this.name = name;
        this.serviceName = serviceName;
        this.servicePlan = servicePlan;
        this.tags = tags;
        this.credentials = credentials;
    }

    @Nonnull
    public static MapSelectionBuilder builder()
    {
        return new DefaultServiceBindingBuilder();
    }

    @Nonnull
    @Override
    public List<String> getKeys()
    {
        return new ArrayList<>(map.keySet());
    }

    @Override
    public boolean containsKey( @Nonnull final String key )
    {
        return map.containsKey(key);
    }

    @Nonnull
    @Override
    public Optional<Object> get( @Nonnull final String key )
    {
        return Optional.ofNullable(map.get(key));
    }

    @Nonnull
    @Override
    public Optional<String> getName()
    {
        return Optional.ofNullable(name);
    }

    @Nonnull
    @Override
    public Optional<String> getServiceName()
    {
        return Optional.ofNullable(serviceName);
    }

    @Nonnull
    @Override
    public Optional<String> getServicePlan()
    {
        return Optional.ofNullable(servicePlan);
    }

    @Nonnull
    @Override
    public List<String> getTags()
    {
        return Collections.unmodifiableList(tags);
    }

    @Nonnull
    @Override
    public Map<String, Object> getCredentials()
    {
        return Collections.unmodifiableMap(credentials);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getName(), getServiceName(), getServicePlan(), getTags(), getCredentials(), map);
    }

    @Override
    public boolean equals( final Object o )
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DefaultServiceBinding that = (DefaultServiceBinding) o;

        return getName().equals(that.getName()) && getServiceName().equals(that.getServiceName()) && getServicePlan().equals(
                that.getServicePlan()) && getTags().equals(that.getTags()) && getCredentials().equals(that.getCredentials()) && map.equals(
                that.map);
    }

    public interface MapSelectionBuilder
    {
        @Nonnull
        TerminalBuilder copy( @Nonnull final Map<String, Object> properties );
    }

    public interface TerminalBuilder
    {
        @Nonnull
        TerminalBuilder withName( @Nonnull final String name );

        @Nonnull
        TerminalBuilder withNameKey( @Nonnull final String key );

        @Nonnull
        TerminalBuilder withServiceName( @Nonnull final String serviceName );

        @Nonnull
        TerminalBuilder withServiceNameKey( @Nonnull final String key );

        @Nonnull
        TerminalBuilder withServicePlan( @Nonnull final String servicePlan );

        @Nonnull
        TerminalBuilder withServicePlanKey( @Nonnull final String key );

        @Nonnull
        TerminalBuilder withTags( @Nonnull final Iterable<String> tags );

        @Nonnull
        TerminalBuilder withTagsKey( @Nonnull final String key );

        @Nonnull
        TerminalBuilder withCredentials( @Nonnull final Map<String, Object> credentials );

        @Nonnull
        TerminalBuilder withCredentialsKey( @Nonnull final String key );

        @Nonnull
        DefaultServiceBinding build();
    }
}
