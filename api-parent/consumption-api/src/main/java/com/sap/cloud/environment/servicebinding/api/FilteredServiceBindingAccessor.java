package com.sap.cloud.environment.servicebinding.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;

public class FilteredServiceBindingAccessor implements ServiceBindingAccessor
{

    @Nonnull
    private final ServiceBindingAccessor accessor;
    @Nonnull
    private final ServiceBindingFilter filter;

    private FilteredServiceBindingAccessor(
        @Nonnull final ServiceBindingAccessor accessor,
        @Nonnull final ServiceBindingFilter filter )
    {
        this.accessor = accessor;
        this.filter = filter;
    }

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings()
        throws ServiceBindingAccessException
    {
        return accessor.getServiceBindings().stream().filter(filter::matches).collect(Collectors.toList());
    }

    public static Builder from( @Nonnull final ServiceBindingAccessor accessor )
    {
        return new BuilderImpl(accessor);
    }

    interface Builder
    {
        @Nonnull
        Builder where( @Nonnull final ServiceBindingFilter filter );

        @Nonnull
        FilteredServiceBindingAccessor build();
    }

    private static class BuilderImpl implements Builder
    {
        @Nonnull
        private final ServiceBindingAccessor accessor;
        @Nullable
        private ServiceBindingFilter filter;

        private BuilderImpl( @Nonnull final ServiceBindingAccessor accessor )
        {
            this.accessor = accessor;
        }

        @Nonnull
        @Override
        public Builder where( @Nonnull final ServiceBindingFilter filter )
        {
            this.filter = this.filter == null ? filter : this.filter.and(filter);
            return this;
        }

        @Nonnull
        @Override
        public FilteredServiceBindingAccessor build()
        {
            return new FilteredServiceBindingAccessor(accessor, filter == null ? ServiceBindingFilter.TRUE : filter);
        }
    }
}
