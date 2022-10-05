/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;

/**
 * Represents a source for {@link ServiceBinding}s.
 */
@FunctionalInterface
public interface ServiceBindingAccessor
{
    /**
     * Returns {@link ServiceBindingAccessor} instances for implementations that are exposed via the
     * <a href="https://xperti.io/blogs/understanding-java-service-loader/">Service Loader Pattern</a>.
     * <p>
     * These instances are useful when the behavior of one (or more) specific {@link ServiceBindingAccessor}s should be
     * overwritten while leaving others in their default state. <br>
     * <b>Example:</b>
     * 
     * <pre>
     * {@code
     *     final List<ServiceBindingAccessor> defaultInstances = ServiceBindingAccessor.getInstancesViaServiceLoader();
     *     if( defaultInstances.removeIf(SapVcapServicesServiceBindingAccessor.class::isInstance) ) {
     *         defaultInstances.add(new SapVcapServicesServiceBindingAccessor(customEnvironmentVariableReader));
     *     }
     *
     *     final ServiceBindingMerger merger =
     *         new ServiceBindingMerger(defaultInstances, ServiceBindingMerger.KEEP_UNIQUE);
     *     final SimpleServiceBindingCache cache = new SimpleServiceBindingCache(merger);
     *
     *     DefaultServiceBindingAccessor.setInstance(cache);
     * }
     * </pre>
     *
     * @return A {@link List} of {@link ServiceBindingAccessor} instances created from implementations that are exposed
     *         via the <i>Service Locator Pattern</i> (see above).
     */
    static List<ServiceBindingAccessor> getInstancesViaServiceLoader()
    {
        final ServiceLoader<ServiceBindingAccessor> serviceLoader =
            ServiceLoader.load(ServiceBindingAccessor.class, ServiceBindingAccessor.class.getClassLoader());
        return StreamSupport.stream(serviceLoader.spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Retrieves all {@link ServiceBinding}s that are accessible for this {@link ServiceBindingAccessor}.
     *
     * @return All accessible {@link ServiceBinding}s.
     * @throws ServiceBindingAccessException
     *             Thrown if anything went wrong while loading the {@link ServiceBinding}s.
     */
    @Nonnull
    List<ServiceBinding> getServiceBindings()
        throws ServiceBindingAccessException;
}
