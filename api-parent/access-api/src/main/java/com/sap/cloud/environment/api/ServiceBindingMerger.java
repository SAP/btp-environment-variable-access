package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class ServiceBindingMerger implements ServiceBindingAccessor
{
    @Nonnull
    public static final KeySelector KEEP_EVERYTHING = binding -> binding;

    @Nonnull
    private final Collection<ServiceBindingAccessor> accessors;
    @Nonnull
    private final KeySelector keySelector;

    public ServiceBindingMerger( @Nonnull final Collection<ServiceBindingAccessor> accessors,
                                 @Nonnull final KeySelector keySelector )
    {
        this.accessors = new ArrayList<>(accessors);
        this.keySelector = keySelector;
    }

    @Nonnull
    public static InitialBuilder builder()
    {
        return new Builder();
    }

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings()
    {
        return new ArrayList<>(mergeServiceBindings().values());
    }

    private Map<Object, ServiceBinding> mergeServiceBindings()
    {
        final Map<Object, ServiceBinding> mergedServiceBindings = new HashMap<>();
        accessors.stream().flatMap(accessor -> getKeyedServiceBindings(accessor).entrySet().stream()).forEachOrdered(entry -> mergedServiceBindings.putIfAbsent(entry.getKey(), entry.getValue()));

        return mergedServiceBindings;
    }

    private Map<Object, ServiceBinding> getKeyedServiceBindings( @Nonnull final ServiceBindingAccessor accessor )
    {
        final Map<Object, ServiceBinding> keyedServiceBindings = new HashMap<>();
        accessor.getServiceBindings().stream().forEachOrdered(serviceBinding -> keyedServiceBindings.putIfAbsent(keySelector.selectKey(serviceBinding), serviceBinding));

        return keyedServiceBindings;
    }

    @FunctionalInterface
    public interface KeySelector
    {
        @Nonnull
        Object selectKey( @Nonnull final ServiceBinding serviceBinding );
    }

    public interface InitialBuilder
    {
        @Nonnull
        AccessorBuilder withAccessor( @Nonnull final ServiceBindingAccessor accessor );
    }

    public interface AccessorBuilder
    {
        @Nonnull
        AccessorBuilder withAccessor( @Nonnull final ServiceBindingAccessor accessor );

        @Nonnull
        TerminalBuilder andKeySelector( @Nonnull final KeySelector keySelector );
    }

    public interface TerminalBuilder
    {
        @Nonnull
        ServiceBindingMerger build();
    }

    private static class Builder implements InitialBuilder, AccessorBuilder, TerminalBuilder
    {
        @Nonnull
        private final List<ServiceBindingAccessor> accessors = new ArrayList<>();
        @Nullable
        private KeySelector keySelector;

        @Nonnull
        @Override
        public AccessorBuilder withAccessor( @Nonnull final ServiceBindingAccessor accessor )
        {
            accessors.add(accessor);
            return this;
        }

        @Nonnull
        @Override
        public TerminalBuilder andKeySelector( @Nonnull final KeySelector keySelector )
        {
            this.keySelector = keySelector;
            return this;
        }

        @Nonnull
        @Override
        public ServiceBindingMerger build()
        {
            return new ServiceBindingMerger(accessors, Objects.requireNonNull(keySelector, "The KeySelector must not be null!"));
        }
    }
}
