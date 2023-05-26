/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

class FileSystemWatcherCache implements DirectoryBasedCache
{
    @Nonnull
    private static final Logger log = LoggerFactory.getLogger(FileSystemWatcherCache.class);

    @Nonnull
    private static final Supplier<FileSystem> DEFAULT_FILE_SYSTEM_SUPPLIER = FileSystems::getDefault;
    private static final int DEFAULT_MAX_RECONCILIATION_ATTEMPTS = 3;
    @Nonnull
    private static final Collection<WatchEvent.Kind<?>> MODIFICATION_EVENTS =
        Arrays.asList(ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

    @Nonnull
    private static final Lock cachedWatchServicesLock = new ReentrantLock();
    @Nonnull
    private static final Map<FileSystem, AutoClosingWatchService> cachedWatchServices = new HashMap<>();

    @Nullable
    private static AutoClosingWatchService tryGetOrCreateWatchService( @Nonnull final FileSystem fileSystem )
    {
        if( !fileSystem.isOpen() ) {
            log
                .warn(
                    "Trying to retrieve a {} for a closed {}.",
                    WatchService.class.getName(),
                    fileSystem.getClass().getName());
            return null;
        }

        cachedWatchServicesLock.lock();
        try {
            cachedWatchServices.entrySet().removeIf(entry -> !entry.getKey().isOpen() || entry.getValue().isClosed());

            final AutoClosingWatchService result = cachedWatchServices.computeIfAbsent(fileSystem, key -> {
                try {
                    final WatchService watchService = fileSystem.newWatchService();
                    final AutoClosingWatchService autoClosingWatchService = new AutoClosingWatchService(watchService);
                    if( autoClosingWatchService.isClosed() ) {
                        return null;
                    }

                    return autoClosingWatchService;
                }
                catch( final IOException e ) {
                    log
                        .warn(
                            "Unable to create new {} from the provided {}.",
                            WatchService.class.getName(),
                            fileSystem.getClass().getName(),
                            e);
                    return null;
                }
            });

            if( result == null ) {
                return null;
            }

            return result.increaseReferenceCountAndGet();
        }
        finally {
            cachedWatchServicesLock.unlock();
        }
    }

    @Nonnull
    private final Function<Path, ServiceBinding> serviceBindingFromDirectory;
    @Nonnull
    private final Supplier<FileSystem> fileSystemSupplier;
    private final int maxReconciliationAttempts;
    int reconciliationAttempts = -1;
    // we are starting at -1 because the very first "reconciliation" attempt is just the lazy initialization, so we don't count that towards the granted limit
    @Nonnull
    final Map<Path, ServiceBinding> cachedServiceBindings = new HashMap<>();
    @Nonnull
    final Map<Path, WatchKey> directoryWatchKeys = new HashMap<>();
    @Nullable
    FileSystem fileSystem;
    @Nullable
    AutoClosingWatchService watchService;

    public FileSystemWatcherCache( @Nonnull final Function<Path, ServiceBinding> serviceBindingFromDirectory )
    {
        this(serviceBindingFromDirectory, DEFAULT_FILE_SYSTEM_SUPPLIER, DEFAULT_MAX_RECONCILIATION_ATTEMPTS);
    }

    public FileSystemWatcherCache(
        @Nonnull final Function<Path, ServiceBinding> serviceBindingFromDirectory,
        @Nonnull final Supplier<FileSystem> fileSystemSupplier,
        @Nonnegative final int maxReconciliationAttempts )
    {
        this.serviceBindingFromDirectory = serviceBindingFromDirectory;
        this.fileSystemSupplier = fileSystemSupplier;
        this.maxReconciliationAttempts = maxReconciliationAttempts;
    }

    @Nonnull
    @Override
    public synchronized List<ServiceBinding> getServiceBindings( @Nonnull final Collection<Path> directories )
    {
        if( reconciliationIsNeeded() ) {
            tryApplyReconciliation();
        } else {
            if( reconciliationAttempts > 0 ) {
                reconciliationAttempts--;
                log
                    .debug(
                        "Reconciliation is not needed. Granting back one reconciliation attempt. New current reconciliation attempt: {}.",
                        reconciliationAttempts);
            }
        }

        if( reconciliationIsNeeded() ) {
            // reconciliation didn't work. so just return load the service bindings from the file system and (potentially) re-try reconciliation in the next invocation.
            releaseResources();
            return directories
                .stream()
                .map(serviceBindingFromDirectory)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }

        removeOutdatedWatchKeys(directories);
        removeOutdatedServiceBindings(directories);

        boolean watchKeyInvalidExceptionCaught = false;
        final List<ServiceBinding> result = new ArrayList<>();
        for( final Path directory : directories ) {
            @Nullable
            ServiceBinding maybeCachedServiceBinding;
            try {
                maybeCachedServiceBinding = renewCachedServiceBindingIfNeeded(directory);
            }
            catch( final WatchKeyInvalidException | UnableToWatchDirectoryException e ) {
                watchKeyInvalidExceptionCaught = true;
                maybeCachedServiceBinding = serviceBindingFromDirectory.apply(directory);
            }

            if( maybeCachedServiceBinding != null ) {
                result.add(maybeCachedServiceBinding);
            }
        }

        if( watchKeyInvalidExceptionCaught ) {
            releaseResources();
        }

        return result;
    }

    private boolean reconciliationIsNeeded()
    {
        return fileSystem == null || !fileSystem.isOpen() || watchService == null || watchService.isClosed();
    }

    private void tryApplyReconciliation()
    {
        if( reconciliationAttempts >= maxReconciliationAttempts ) {
            return;
        }

        reconciliationAttempts++;
        if( reconciliationAttempts > 0 ) {
            // we only log for the 2nd attempt onwards because the very first attempt is actually just the initial setup as this class is lazy.
            log.debug("Trying to apply reconciliation strategy. This is attempt number {}.", reconciliationAttempts);
        }

        try {
            fileSystem = fileSystemSupplier.get();
        }
        catch( final Exception e ) {
            log.debug("Unable to retrieve {}.", FileSystem.class.getName(), e);
            fileSystem = null;
        }

        if( fileSystem == null || !fileSystem.isOpen() ) {
            tryCloseWatchService(watchService);
            watchService = null;
            return;
        }

        final WatchService oldWatchService = watchService;
        watchService = tryGetOrCreateWatchService(fileSystem);

        if( oldWatchService != watchService ) {
            tryCloseWatchService(oldWatchService);
        }
    }

    private void removeOutdatedWatchKeys( @Nonnull final Collection<Path> directoriesOfInterest )
    {
        directoryWatchKeys.entrySet().removeIf(entry -> {
            if( directoriesOfInterest.contains(entry.getKey()) ) {
                return false;
            }

            entry.getValue().cancel();
            return true;
        });
    }

    private void removeOutdatedServiceBindings( @Nonnull final Collection<Path> directoriesOfInterest )
    {
        cachedServiceBindings.keySet().removeIf(key -> !directoriesOfInterest.contains(key));
    }

    @Nullable
    private ServiceBinding renewCachedServiceBindingIfNeeded( @Nonnull final Path directory )
        throws WatchKeyInvalidException,
            UnableToWatchDirectoryException
    {
        @Nullable
        final WatchKey watchKey = directoryWatchKeys.get(directory);
        if( watchKey == null ) {
            return watchAndCacheServiceBinding(directory);
        }

        if( !watchKey.isValid() ) {
            log
                .error(
                    "{} for directory '{}' is invalid. As a consequence, this service binding will be reloaded from the disk and reconciliation will be trigger for the next attempt to get all service bindings.",
                    watchKey.getClass().getName(),
                    directory);
            throw new WatchKeyInvalidException();
        }

        if( hasBeenModified(watchKey) ) {
            log.debug("Directory '{}' has been modified. Reloading the service binding.", directory);
            return cacheServiceBinding(directory);
        }

        log.debug("Directory '{}' has not been modified. Serving the service binding from the cache.", directory);
        return cachedServiceBindings.get(directory);
    }

    @Nullable
    private ServiceBinding watchAndCacheServiceBinding( @Nonnull final Path directory )
        throws UnableToWatchDirectoryException
    {
        startWatching(directory);
        return cacheServiceBinding(directory);
    }

    private void startWatching( @Nonnull final Path directory )
        throws UnableToWatchDirectoryException
    {
        if( watchService == null ) {
            throw new UnableToWatchDirectoryException();
        }

        final WatchKey watchKey = watchService.registerWatchKey(directory);
        directoryWatchKeys.put(directory, watchKey);
    }

    @Nullable
    private ServiceBinding cacheServiceBinding( @Nonnull final Path directory )
    {
        cachedServiceBindings.remove(directory);

        @Nullable
        final ServiceBinding serviceBinding = serviceBindingFromDirectory.apply(directory);
        if( serviceBinding == null ) {
            return null;
        }

        cachedServiceBindings.put(directory, serviceBinding);
        return serviceBinding;
    }

    private boolean hasBeenModified( @Nonnull final WatchKey watchKey )
    {
        final List<WatchEvent<?>> events = watchKey.pollEvents();
        watchKey.reset();

        return events.stream().map(WatchEvent::kind).anyMatch(MODIFICATION_EVENTS::contains);
    }

    @Override
    protected void finalize()
        throws Throwable
    {
        super.finalize();
        releaseResources();
    }

    synchronized void releaseResources()
    {
        directoryWatchKeys.values().forEach(WatchKey::cancel);
        directoryWatchKeys.clear();
        cachedServiceBindings.clear();

        tryCloseWatchService(watchService);
        fileSystem = null;
        watchService = null;
    }

    private void tryCloseWatchService( @Nullable final WatchService watchService )
    {
        if( watchService == null ) {
            return;
        }

        try {
            watchService.close();
        }
        catch( final IOException e ) {
            // ignored
        }
    }

    static class AutoClosingWatchService implements WatchService
    {
        @Nullable
        private WatchService delegate;
        @Nonnull
        private final AtomicInteger referenceCount = new AtomicInteger();

        public AutoClosingWatchService( @Nonnull final WatchService delegate )
        {
            this.delegate = delegate;
        }

        @Nullable
        public synchronized WatchKey registerWatchKey( @Nonnull final Path path )
            throws UnableToWatchDirectoryException
        {
            if( delegate == null ) {
                log.warn("Trying to access a closed {}.", getClass().getName());
                return null;
            }

            try {
                return path.register(delegate, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            }
            catch( final IOException e ) {
                log.error("Unable to watch directory '{}'.", path, e);
                throw new UnableToWatchDirectoryException();
            }
        }

        @Nonnull
        private synchronized AutoClosingWatchService increaseReferenceCountAndGet()
        {
            if( delegate == null ) {
                log.warn("Trying to access a closed {}.", getClass().getName());
                return this;
            }

            final int newReferenceCount = referenceCount.incrementAndGet();
            log
                .debug(
                    "Increased reference count to this {}. There are now {} reference(s) to this instance.",
                    getClass().getName(),
                    newReferenceCount);
            return this;
        }

        private synchronized boolean isClosed()
        {
            if( delegate == null ) {
                return true;
            }

            try {
                delegate.poll();
                return false;
            }
            catch( final ClosedWatchServiceException e ) {
                return true;
            }
        }

        @Override
        public synchronized void close()
        {
            if( delegate == null ) {
                return;
            }

            final int newReferenceCount = referenceCount.decrementAndGet();
            log
                .debug(
                    "Decreased reference count to this {}. There are now {} reference(s) to this instance.",
                    getClass().getName(),
                    newReferenceCount);

            if( newReferenceCount > 0 ) {
                return;
            }

            log.debug("Closing {}.", delegate.getClass().getName());
            try {
                delegate.close();
            }
            catch( final IOException e ) {
                log.debug("Exception while trying to close a {}.", delegate.getClass().getName(), e);
            }

            delegate = null;
        }

        @Override
        public synchronized WatchKey poll()
        {
            if( delegate == null ) {
                log.warn("Trying to access a closed {}.", getClass().getName());
                return null;
            }

            return delegate.poll();
        }

        @Override
        public WatchKey poll( final long timeout, final TimeUnit unit )
            throws InterruptedException
        {
            if( delegate == null ) {
                log.warn("Trying to access a closed {}.", getClass().getName());
                return null;
            }

            return delegate.poll(timeout, unit);
        }

        @Override
        public synchronized WatchKey take()
            throws InterruptedException
        {
            if( delegate == null ) {
                log.warn("Trying to access a closed {}.", getClass().getName());
                return null;
            }

            return delegate.take();
        }
    }

    private static class WatchKeyInvalidException extends Exception
    {
        private static final long serialVersionUID = 9070754310573522025L;
    }

    private static class UnableToWatchDirectoryException extends Exception
    {
        private static final long serialVersionUID = -433620100403474102L;
    }
}
