package com.sap.cloud.environment.servicebinding.api;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A static access point for the default {@link ServiceBindingAccessor}. The statically stored instance inside this
 * class can both be retrieved ({@link DefaultServiceBindingAccessor#getInstance()}) <b>and</b> manipulated
 * ({@link DefaultServiceBindingAccessor#setInstance(ServiceBindingAccessor)}). Applications might want to overwrite the
 * default instance during startup to tweak the default behavior of libraries that are relying on it. <br>
 * <b>Please note:</b> It is considered best practice to offer APIs that accept a dedicated
 * {@link ServiceBindingAccessor} instance instead of using the globally available instance stored inside this class.
 * For example, libraries that are using {@link ServiceBindingAccessor}s should offer APIs such as the following:
 *
 * <pre>
 * public ReturnType doSomethingWithServiceBindings( @Nonnull final ServiceBindingAccessor accessor );
 * </pre>
 * <p>
 * If that is, for some reason, not feasible, only then should this static default instance be used.
 */
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

    /**
     * Returns the statically stored {@link ServiceBindingAccessor} instance. This instance can be changed at any time
     * by using {@link DefaultServiceBindingAccessor#setInstance(ServiceBindingAccessor)}. <br>
     * By default, the returned {@link ServiceBindingAccessor} will be assembled in the following way:
     * <ol>
     * <li>Use {@link ServiceBindingAccessor#getInstancesViaServiceLoader()} to retrieve a list of
     * {@link ServiceBindingAccessor} instances.</li>
     * <li>Combine instances of the found implementations using the {@link ServiceBindingMerger} (the merging strategy
     * used is {@link ServiceBindingMerger#KEEP_EVERYTHING}).</li>
     * <li>Wrap the resulting instance of {@link ServiceBindingMerger} into a {@link SimpleServiceBindingCache}.</li>
     * </ol>
     *
     * @return The statically stored {@link ServiceBindingAccessor} instance.
     */
    @Nonnull
    public static ServiceBindingAccessor getInstance()
    {
        return instance;
    }

    /**
     * Overwrites the statically stored {@link ServiceBindingAccessor} instance.
     *
     * @param accessor
     *            The {@link ServiceBindingAccessor} instance that should be returned by
     *            {@link DefaultServiceBindingAccessor#getInstance()}. If {@code accessor} is {@code null}, the default
     *            instance will be used (see {@link DefaultServiceBindingAccessor#getInstance()} for more details).
     */
    public static void setInstance( @Nullable final ServiceBindingAccessor accessor )
    {
        if( accessor != null ) {
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
        final List<ServiceBindingAccessor> defaultAccessors = ServiceBindingAccessor.getInstancesViaServiceLoader();

        if( logger.isDebugEnabled() ) {
            final String classNames =
                defaultAccessors.stream().map(Object::getClass).map(Class::getName).collect(Collectors.joining(", "));
            logger
                .debug(
                    "Following implementations of {} will be used for the {}: {}.",
                    ServiceBindingAccessor.class.getSimpleName(),
                    DefaultServiceBindingAccessor.class.getSimpleName(),
                    classNames);
        }

        final ServiceBindingMerger bindingMerger =
            new ServiceBindingMerger(defaultAccessors, ServiceBindingMerger.KEEP_EVERYTHING);

        return new SimpleServiceBindingCache(bindingMerger);
    }
}
