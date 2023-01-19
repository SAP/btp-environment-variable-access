/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.sap.cloud.environment.servicebinding.api.exception.UnsupportedPropertyTypeException;

/**
 * A {@link ServiceBinding} that treats keys case insensitively.
 */
public class DefaultServiceBinding implements ServiceBinding
{
    @Nonnull
    private final Map<String, Object> properties;

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

    DefaultServiceBinding(
        @Nonnull final Map<String, Object> properties,
        @Nullable final String name,
        @Nullable final String serviceName,
        @Nullable final String servicePlan,
        @Nonnull final List<String> tags,
        @Nonnull final Map<String, Object> credentials )
    {
        this.properties = properties;
        this.name = name;
        this.serviceName = serviceName;
        this.servicePlan = servicePlan;
        this.tags = tags;
        this.credentials = credentials;
    }

    /**
     * Initializes a new {@link MapSelectionBuilder} instance.
     *
     * @return A new {@link MapSelectionBuilder} instance.
     */
    @Nonnull
    public static MapSelectionBuilder builder()
    {
        return new DefaultServiceBindingBuilder();
    }

    @Nonnull
    @Override
    public Set<String> getKeys()
    {
        return Collections.unmodifiableSet(properties.keySet());
    }

    @Override
    public boolean containsKey( @Nonnull final String key )
    {
        return properties.containsKey(key);
    }

    @Nonnull
    @Override
    public Optional<Object> get( @Nonnull final String key )
    {
        return Optional.ofNullable(properties.get(key));
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
        return Objects.hash(getName(), getServiceName(), getServicePlan(), getTags(), getCredentials(), properties);
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

        final DefaultServiceBinding that = (DefaultServiceBinding) o;

        return getName().equals(that.getName())
            && getServiceName().equals(that.getServiceName())
            && getServicePlan().equals(that.getServicePlan())
            && getTags().equals(that.getTags())
            && getCredentials().equals(that.getCredentials())
            && properties.equals(that.properties);
    }

    /**
     * Represents the first step that is required for building a {@link DefaultServiceBinding}.
     */
    public interface MapSelectionBuilder
    {
        /**
         * Creates a deep copy of the given {@code properties}, which is then used as the data source of the to-be-built
         * {@link DefaultServiceBinding}.
         *
         * @param properties
         *            The properties of the to-be-built {@link DefaultServiceBinding}.
         * @return A {@link TerminalBuilder} to finalize the building process.
         * @throws UnsupportedPropertyTypeException
         *             Thrown if the given {@code properties} contain an unsupported property type. Supported types are:
         *             <ul>
         *             <li>{@link Boolean}</li>
         *             <li>{@link Number}</li>
         *             <li>{@link String}</li>
         *             <li>{@link Map}</li>
         *             <li>{@link Iterable}</li>
         *             </ul>
         */
        @Nonnull
        TerminalBuilder copy( @Nonnull final Map<String, Object> properties )
            throws UnsupportedPropertyTypeException;
    }

    /**
     * Represents the last step that is required for building a {@link DefaultServiceBinding}.
     */
    public interface TerminalBuilder
    {
        /**
         * Defines the name of the to-be-built {@link DefaultServiceBinding}.
         *
         * @param name
         *            The name that should be returned by {@link DefaultServiceBinding#getName()}.
         * @return This {@link TerminalBuilder} instance.
         */
        @Nonnull
        TerminalBuilder withName( @Nonnull final String name );

        /**
         * Extracts the name of the to-be-built {@link DefaultServiceBinding} from the initially passed properties (see
         * {@link MapSelectionBuilder#copy(Map)}) using the given {@code key}.
         *
         * @param key
         *            The key that should be used to extract the value that should be returned by
         *            {@link DefaultServiceBinding#getName()} from the initially passed properties (see
         *            {@link MapSelectionBuilder#copy(Map)}).
         * @return This {@link TerminalBuilder} instance.
         */
        @Nonnull
        TerminalBuilder withNameKey( @Nonnull final String key );

        /**
         * Defines the name of the bound service of the to-be-built {@link DefaultServiceBinding}.
         *
         * @param serviceName
         *            The name of the bound service that should be returned by
         *            {@link DefaultServiceBinding#getServiceName()}.
         * @return This {@link TerminalBuilder} instance.
         */
        @Nonnull
        TerminalBuilder withServiceName( @Nonnull final String serviceName );

        /**
         * Extracts the name of the bound service of the to-be-built {@link DefaultServiceBinding} from the initially
         * passed properties (see {@link MapSelectionBuilder#copy(Map)}) using the given {@code key}.
         *
         * @param key
         *            The key that should be used to extract the value that should be returned by
         *            {@link DefaultServiceBinding#getServiceName()} from the initially passed properties (see
         *            {@link MapSelectionBuilder#copy(Map)}).
         * @return This {@link TerminalBuilder} instance.
         */
        @Nonnull
        TerminalBuilder withServiceNameKey( @Nonnull final String key );

        /**
         * Defines the plan of the bound service of the to-be-built {@link DefaultServiceBinding}.
         *
         * @param servicePlan
         *            The plan of the bound service that should be returned by
         *            {@link DefaultServiceBinding#getServicePlan()}.
         * @return This {@link TerminalBuilder} instance.
         */
        @Nonnull
        TerminalBuilder withServicePlan( @Nonnull final String servicePlan );

        /**
         * Extracts the plan of the bound service of the to-be-built {@link DefaultServiceBinding} from the initially
         * passed properties (see {@link MapSelectionBuilder#copy(Map)}) using the given {@code key}.
         *
         * @param key
         *            The key that should be used to extract the value that should be returned by
         *            {@link DefaultServiceBinding#getServicePlan()} from the initially passed properties (see
         *            {@link MapSelectionBuilder#copy(Map)}).
         * @return This {@link TerminalBuilder} instance.
         */
        @Nonnull
        TerminalBuilder withServicePlanKey( @Nonnull final String key );

        /**
         * Defines the tags of the to-be-built {@link DefaultServiceBinding}.
         *
         * @param tags
         *            The tags that should be returned by {@link DefaultServiceBinding#getTags()}.
         * @return This {@link TerminalBuilder} instance.
         */
        @Nonnull
        TerminalBuilder withTags( @Nonnull final Iterable<String> tags );

        /**
         * Extracts the tags of the to-be-built {@link DefaultServiceBinding} from the initially passed properties (see
         * {@link MapSelectionBuilder#copy(Map)}) using the given {@code key}.
         *
         * @param key
         *            The key that should be used to extract the values that should be returned by
         *            {@link DefaultServiceBinding#getTags()} from the initially passed properties (see
         *            {@link MapSelectionBuilder#copy(Map)}).
         * @return This {@link TerminalBuilder} instance.
         */
        @Nonnull
        TerminalBuilder withTagsKey( @Nonnull final String key );

        /**
         * Defines the credentials of the to-be-built {@link DefaultServiceBinding}.
         *
         * @param credentials
         *            The credentials, which will be deep-copied that should be returned by
         *            {@link DefaultServiceBinding#getCredentials()}.
         * @return This {@link TerminalBuilder} instance.
         */
        @Nonnull
        TerminalBuilder withCredentials( @Nonnull final Map<String, Object> credentials );

        /**
         * Extracts the credentials of the to-be-built {@link DefaultServiceBinding} from the initially passed
         * properties (see {@link MapSelectionBuilder#copy(Map)}) using the given {@code key}.
         *
         * @param key
         *            The key that should be used to extract the values that should be returned by
         *            {@link DefaultServiceBinding#getCredentials()} from the initially passed properties (see
         *            {@link MapSelectionBuilder#copy(Map)}).
         * @return This {@link TerminalBuilder} instance.
         */
        @Nonnull
        TerminalBuilder withCredentialsKey( @Nonnull final String key );

        /**
         * Initializes a new {@link DefaultServiceBinding} instance.
         *
         * @return A new {@link DefaultServiceBinding} instance.
         */
        @Nonnull
        DefaultServiceBinding build();
    }
}
