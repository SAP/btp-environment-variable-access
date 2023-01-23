package com.sap.cloud.environment.servicebinding.api;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface ServiceBindingFilter
{
    ServiceBindingFilter TRUE = any -> true;
    ServiceBindingFilter FALSE = any -> false;

    final class And implements ServiceBindingFilter
    {
        @Nonnull
        private final ServiceBindingFilter first;
        @Nonnull
        private final ServiceBindingFilter second;

        private And( @Nonnull final ServiceBindingFilter first, @Nonnull final ServiceBindingFilter second )
        {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean matches( @Nonnull final ServiceBinding serviceBinding )
        {
            return first.matches(serviceBinding) && second.matches(serviceBinding);
        }
    }

    final class Or implements ServiceBindingFilter
    {
        @Nonnull
        private final ServiceBindingFilter first;
        @Nonnull
        private final ServiceBindingFilter second;

        private Or( @Nonnull final ServiceBindingFilter first, @Nonnull final ServiceBindingFilter second )
        {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean matches( @Nonnull final ServiceBinding serviceBinding )
        {
            return first.matches(serviceBinding) || second.matches(serviceBinding);
        }
    }

    final class Not implements ServiceBindingFilter
    {
        @Nonnull
        private final ServiceBindingFilter filter;

        private Not( @Nonnull final ServiceBindingFilter filter )
        {
            this.filter = filter;
        }

        @Override
        public boolean matches( @Nonnull final ServiceBinding serviceBinding )
        {
            return !filter.matches(serviceBinding);
        }
    }

    boolean matches( @Nonnull final ServiceBinding serviceBinding );

    default ServiceBindingFilter and( @Nonnull final ServiceBindingFilter other )
    {
        return new And(this, other);
    }

    default ServiceBindingFilter or( @Nonnull final ServiceBindingFilter other )
    {
        return new Or(this, other);
    }

    default ServiceBindingFilter not()
    {
        return new Not(this);
    }
}
