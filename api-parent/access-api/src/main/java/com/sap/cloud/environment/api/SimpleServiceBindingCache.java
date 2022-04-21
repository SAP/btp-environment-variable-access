/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import com.sap.cloud.environment.api.exception.ServiceBindingAccessException;

public class SimpleServiceBindingCache implements ServiceBindingAccessor
{
    @Nonnull
    public static final Duration DEFAULT_CACHE_DURATION = Duration.ofMinutes(5L);
    @Nonnull
    static final Supplier<LocalDateTime> DEFAULT_LOCAL_DATE_TIME_SUPPLIER = LocalDateTime::now;

    @Nonnull
    private final ReadWriteLock accessLock = new ReentrantReadWriteLock();
    @Nonnull
    private final ServiceBindingAccessor delegateAccessor;
    @Nonnull
    private final Duration cacheDuration;
    @Nonnull
    private final Supplier<LocalDateTime> localDateTimeSupplier;
    @Nullable
    private List<ServiceBinding> cachedServiceBindings = null;
    @Nullable
    private LocalDateTime lastCacheRenewal = null;

    public SimpleServiceBindingCache( @Nonnull final ServiceBindingAccessor delegateAccessor )
    {
        this(delegateAccessor, DEFAULT_CACHE_DURATION, DEFAULT_LOCAL_DATE_TIME_SUPPLIER);
    }

    SimpleServiceBindingCache( @Nonnull final ServiceBindingAccessor delegateAccessor,
                               @Nonnull final Duration cacheDuration,
                               @Nonnull final Supplier<LocalDateTime> localDateTimeSupplier )
    {
        this.delegateAccessor = delegateAccessor;
        this.cacheDuration = cacheDuration;
        this.localDateTimeSupplier = localDateTimeSupplier;
    }

    public SimpleServiceBindingCache( @Nonnull final ServiceBindingAccessor delegateAccessor,
                                      @Nonnull final Duration cacheDuration )
    {
        this(delegateAccessor, cacheDuration, DEFAULT_LOCAL_DATE_TIME_SUPPLIER);
    }

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings() throws ServiceBindingAccessException
    {
        final LocalDateTime now = localDateTimeSupplier.get();
        if (now == null) {
            throw new IllegalStateException(String.format("Unable to determine the current %s.",
                                                          LocalDateTime.class.getSimpleName()));
        }

        return Optional.ofNullable(tryReadCache(now)).orElse(readOrUpdateCache(now));
    }

    @Nullable
    private List<ServiceBinding> tryReadCache( @Nonnull final Temporal now )
    {
        accessLock.readLock().lock();
        try {
            if (isExpired(now)) {
                return null;
            }

            return cachedServiceBindings;
        } finally {
            accessLock.readLock().unlock();
        }
    }

    @Nonnull
    private List<ServiceBinding> readOrUpdateCache( @Nonnull final LocalDateTime now )
    {
        accessLock.writeLock().lock();
        try {
            if (!isExpired(now)) {
                return Objects.requireNonNull(cachedServiceBindings, "Cached Service Bindings must not be null.");
            }

            cachedServiceBindings = delegateAccessor.getServiceBindings();
            lastCacheRenewal = now;
            return cachedServiceBindings;
        } finally {
            accessLock.writeLock().unlock();
        }
    }

    private boolean isExpired( @Nonnull final Temporal now )
    {
        if (lastCacheRenewal == null || cachedServiceBindings == null) {
            return true;
        }

        final Duration durationSinceLastCacheRenewal = Duration.between(lastCacheRenewal, now);
        return cacheDuration.minus(durationSinceLastCacheRenewal).isNegative();
    }

    public void invalidate()
    {
        accessLock.writeLock().lock();
        try {
            cachedServiceBindings = null;
            lastCacheRenewal = null;
        } finally {
            accessLock.writeLock().unlock();
        }
    }
}
