/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class DefaultServiceBindingAccessor
{
    @Nonnull
    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceBindingAccessor.class);

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
            logger.debug("Setting instance to {}.", accessor.getClass().getName());
            instance = accessor;
        } else {
            logger.debug("Resetting instance.");
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

        if (logger.isDebugEnabled()) {
            final String classNames = accessors.stream()
                                               .map(Object::getClass)
                                               .map(Class::getName)
                                               .collect(Collectors.joining(", "));
            logger.debug("Following implementations of {} were found: {}.",
                         ServiceBindingAccessor.class.getSimpleName(),
                         classNames);
        }

        final ServiceBindingMerger bindingMerger = new ServiceBindingMerger(accessors,
                                                                            ServiceBindingMerger.KEEP_EVERYTHING);

        return new SimpleServiceBindingCache(bindingMerger);
    }
}
