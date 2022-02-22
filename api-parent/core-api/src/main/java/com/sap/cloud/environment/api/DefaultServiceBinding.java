package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class DefaultServiceBinding implements ServiceBinding
{
    @Nonnull
    private final Map<String, Object> map;
    @Nonnull
    private final Function<ServiceBinding, String> nameResolver;
    @Nonnull
    private final Function<ServiceBinding, String> serviceNameResolver;
    @Nonnull
    private final Function<ServiceBinding, String> servicePlanResolver;
    @Nonnull
    private final Function<ServiceBinding, List<String>> tagsResolver;
    @Nonnull
    private final Function<ServiceBinding, Map<String, Object>> credentialsResolver;

    DefaultServiceBinding( @Nonnull final Map<String, Object> map,
                           @Nonnull final Function<ServiceBinding, String> nameResolver,
                           @Nonnull final Function<ServiceBinding, String> serviceNameResolver,
                           @Nonnull final Function<ServiceBinding, String> servicePlanResolver,
                           @Nonnull final Function<ServiceBinding, List<String>> tagsResolver,
                           @Nonnull final Function<ServiceBinding, Map<String, Object>> credentialsResolver )
    {
        this.map = map;
        this.nameResolver = nameResolver;
        this.serviceNameResolver = serviceNameResolver;
        this.servicePlanResolver = servicePlanResolver;
        this.tagsResolver = tagsResolver;
        this.credentialsResolver = credentialsResolver;
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
        return Optional.ofNullable(nameResolver.apply(this));
    }

    @Nonnull
    @Override
    public Optional<String> getServiceName()
    {
        return Optional.ofNullable(serviceNameResolver.apply(this));
    }

    @Nonnull
    @Override
    public Optional<String> getServicePlan()
    {
        return Optional.ofNullable(servicePlanResolver.apply(this));
    }

    @Nonnull
    @Override
    public List<String> getTags()
    {
        @Nullable final List<String> maybeTags = tagsResolver.apply(this);

        if (maybeTags == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(maybeTags);
    }

    @Nonnull
    @Override
    public Map<String, Object> getCredentials()
    {
        @Nullable final Map<String, Object> maybeCredentials = credentialsResolver.apply(this);

        if (maybeCredentials == null) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(maybeCredentials);
    }

    @Nonnull
    @Override
    public Map<String, Object> copyToMap()
    {
        return DefaultServiceBindingBuilder.copyMap(map, Function.identity(), Function.identity());
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

        return getName().equals(that.getName())
                && getServiceName().equals(that.getServiceName())
                && getServicePlan().equals(that.getServicePlan())
                && getTags().equals(that.getTags())
                && getCredentials().equals(that.getCredentials())
                && map.equals(that.map);
    }

    public interface MapSelectionBuilder
    {
        @Nonnull
        TerminalBuilder copy( @Nonnull final Map<String, Object> properties );
    }

    public interface TerminalBuilder
    {
        @Nonnull
        TerminalBuilder withNameKey( @Nonnull final String key );

        @Nonnull
        TerminalBuilder withNameResolver( @Nonnull final Function<ServiceBinding, String> resolver );

        @Nonnull
        TerminalBuilder withServiceNameKey( @Nonnull final String key );

        @Nonnull
        TerminalBuilder withServiceNameResolver( @Nonnull final Function<ServiceBinding, String> resolver );

        @Nonnull
        TerminalBuilder withServicePlanKey( @Nonnull final String key );

        @Nonnull
        TerminalBuilder withServicePlanResolver( @Nonnull final Function<ServiceBinding, String> resolver );

        @Nonnull
        TerminalBuilder withTagsKey( @Nonnull final String key );

        @Nonnull
        TerminalBuilder withTagsResolver( @Nonnull final Function<ServiceBinding, List<String>> resolver );

        @Nonnull
        TerminalBuilder withCredentialsKey( @Nonnull final String key );

        @Nonnull
        TerminalBuilder withCredentialsResolver( @Nonnull final Function<ServiceBinding, Map<String, Object>> resolver );

        @Nonnull
        DefaultServiceBinding build();
    }
}
