/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleServiceBindingCacheTest
{
    @Nonnull
    private static final Duration ONE_SECOND = Duration.ofSeconds(1L);

    @Nonnull
    private static final Duration TWO_SECONDS = Duration.ofSeconds(2L);

    @Nonnull
    private static final ServiceBinding FIRST_SERVICE_BINDING =
        DefaultServiceBinding.builder().copy(Collections.singletonMap("id", 1)).build();

    @Nonnull
    private static final ServiceBinding SECOND_SERVICE_BINDING =
        DefaultServiceBinding.builder().copy(Collections.singletonMap("id", 2)).build();

    @Test
    void cacheIsFilledUponFirstInvocation()
    {
        final CountingServiceBindingAccessor accessor =
            new CountingServiceBindingAccessor(FIRST_SERVICE_BINDING, SECOND_SERVICE_BINDING);
        final ManualTimeSupplier timeSupplier = new ManualTimeSupplier();

        final SimpleServiceBindingCache sut = new SimpleServiceBindingCache(accessor, ONE_SECOND, timeSupplier);

        assertThat(sut.getServiceBindings()).containsExactly(FIRST_SERVICE_BINDING);
        assertThat(accessor.getNumberOfAccesses()).isEqualTo(1);

        assertThat(sut.getServiceBindings()).containsExactly(FIRST_SERVICE_BINDING);
        assertThat(accessor.getNumberOfAccesses()).isEqualTo(1);
    }

    @Test
    void cacheIsInvalidatedAutomatically()
    {
        final CountingServiceBindingAccessor accessor =
            new CountingServiceBindingAccessor(FIRST_SERVICE_BINDING, SECOND_SERVICE_BINDING);
        final ManualTimeSupplier timeSupplier = new ManualTimeSupplier();

        final SimpleServiceBindingCache sut = new SimpleServiceBindingCache(accessor, ONE_SECOND, timeSupplier);

        assertThat(sut.getServiceBindings()).containsExactly(FIRST_SERVICE_BINDING);
        assertThat(accessor.getNumberOfAccesses()).isEqualTo(1);

        timeSupplier.advance(TWO_SECONDS); // cache should be invalidated by now

        assertThat(sut.getServiceBindings()).containsExactly(SECOND_SERVICE_BINDING);
        assertThat(accessor.getNumberOfAccesses()).isEqualTo(2);
    }

    @Test
    void cacheCanBeInvalidatedManually()
    {
        final CountingServiceBindingAccessor accessor =
            new CountingServiceBindingAccessor(FIRST_SERVICE_BINDING, SECOND_SERVICE_BINDING);
        final ManualTimeSupplier timeSupplier = new ManualTimeSupplier();

        final SimpleServiceBindingCache sut = new SimpleServiceBindingCache(accessor, ONE_SECOND, timeSupplier);

        assertThat(sut.getServiceBindings()).containsExactly(FIRST_SERVICE_BINDING);
        assertThat(accessor.getNumberOfAccesses()).isEqualTo(1);

        sut.invalidate();

        assertThat(sut.getServiceBindings()).containsExactly(SECOND_SERVICE_BINDING);
        assertThat(accessor.getNumberOfAccesses()).isEqualTo(2);
    }

    @Test
    void cacheIsThreadSafe()
        throws ExecutionException,
            InterruptedException
    {
        final CountingServiceBindingAccessor accessor =
            new CountingServiceBindingAccessor(FIRST_SERVICE_BINDING, SECOND_SERVICE_BINDING);
        final ManualTimeSupplier timeSupplier = new ManualTimeSupplier();

        final SimpleServiceBindingCache sut = new SimpleServiceBindingCache(accessor, ONE_SECOND, timeSupplier);

        final ExecutorService executorService = Executors.newFixedThreadPool(8);
        try {
            final Collection<Future<List<ServiceBinding>>> firstFutures = new ArrayList<>(8);
            for( int i = 0; i < 8; ++i ) {
                firstFutures.add(executorService.submit(sut::getServiceBindings));
            }

            for( final Future<List<ServiceBinding>> future : firstFutures ) {
                assertThat(future.get()).containsExactly(FIRST_SERVICE_BINDING);
            }

            assertThat(accessor.getNumberOfAccesses()).isEqualTo(1);

            timeSupplier.advance(TWO_SECONDS); // cache is automatically invalidated

            final Collection<Future<List<ServiceBinding>>> secondFutures = new ArrayList<>(8);
            for( int i = 0; i < 8; ++i ) {
                secondFutures.add(executorService.submit(sut::getServiceBindings));
            }

            for( final Future<List<ServiceBinding>> future : secondFutures ) {
                assertThat(future.get()).containsExactly(SECOND_SERVICE_BINDING);
            }

            assertThat(accessor.getNumberOfAccesses()).isEqualTo(2);
        }
        finally {
            executorService.shutdown();
        }
    }

    private static class CountingServiceBindingAccessor implements ServiceBindingAccessor
    {
        @Nonnull
        private final List<ServiceBinding> serviceBindings;

        private int numberOfAccesses = 0;

        public CountingServiceBindingAccessor( @Nonnull final ServiceBinding... serviceBindings )
        {
            this(Arrays.asList(serviceBindings));
        }

        public CountingServiceBindingAccessor( @Nonnull final List<ServiceBinding> serviceBindings )
        {
            this.serviceBindings = serviceBindings;
        }

        public int getNumberOfAccesses()
        {
            return numberOfAccesses;
        }

        @Nonnull
        @Override
        public List<ServiceBinding> getServiceBindings()
            throws ServiceBindingAccessException
        {
            if( serviceBindings.isEmpty() ) {
                return Collections.emptyList();
            }

            numberOfAccesses += 1;
            return Collections
                .singletonList(serviceBindings.get(Math.min(numberOfAccesses - 1, serviceBindings.size() - 1)));
        }
    }

    private static class ManualTimeSupplier implements Supplier<LocalDateTime>
    {
        @Nonnull
        private LocalDateTime currentValue = LocalDateTime.now();

        public void advance( @Nonnull final TemporalAmount duration )
        {
            currentValue = currentValue.plus(duration);
        }

        public void setCurrentValue( @Nonnull final LocalDateTime value )
        {
            currentValue = value;
        }

        @Override
        public LocalDateTime get()
        {
            return currentValue;
        }
    }
}
