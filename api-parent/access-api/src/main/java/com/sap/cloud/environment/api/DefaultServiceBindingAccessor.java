/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class DefaultServiceBindingAccessor
{
    @Nonnull
    private static ServiceBindingAccessor instance = newDefaultInstance();

    private DefaultServiceBindingAccessor()
    {
        throw new IllegalStateException("This utility class must not be instantiated.");
    }

    @Nonnull
    public static ServiceBindingAccessor getInstance()
    {
        return instance;
    }

    public static void setInstance( @Nullable final ServiceBindingAccessor accessor )
    {
        if (accessor != null) {
            instance = accessor;
        } else {
            instance = newDefaultInstance();
        }
    }

    @Nonnull
    private static ServiceBindingAccessor newDefaultInstance()
    {
        final ClassLoader classLoader = DefaultServiceBindingAccessor.class.getClassLoader();
        final ServiceLoader<ServiceBindingAccessor> serviceLoader = ServiceLoader.load(ServiceBindingAccessor.class,
                                                                                       classLoader);
        final Collection<ServiceBindingAccessor> accessors = StreamSupport.stream(serviceLoader.spliterator(), false)
                                                                          .collect(Collectors.toList());
        final ServiceBindingMerger bindingMerger = new ServiceBindingMerger(accessors,
                                                                            ServiceBindingMerger.KEEP_EVERYTHING);

        return new SimpleServiceBindingCache(bindingMerger);
    }
}