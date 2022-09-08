/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

class FileSystemWatcherCache implements DirectoryBasedCache
{
    @Nonnull
    private static final Collection<WatchEvent.Kind<?>> MODIFICATION_EVENTS =
        Arrays.asList(ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

    @Nonnull
    private final Function<Path, ServiceBinding> serviceBindingLoader;
    @Nonnull
    // package-private for simplified testing
    final Map<Path, ServiceBinding> cachedServiceBindings = new HashMap<>();
    @Nonnull
    // package-private for simplified testing
    final Map<Path, WatchKey> directoryWatchKeys = new HashMap<>();
    @Nonnull
    private final WatchService watchService;

    public FileSystemWatcherCache( @Nonnull final Function<Path, ServiceBinding> serviceBindingLoader )
    {
        this.serviceBindingLoader = serviceBindingLoader;

        try {
            watchService = FileSystems.getDefault().newWatchService();
        }
        catch( final IOException e ) {
            throw new IllegalStateException(
                String.format("Unable to create new instance of '%s'.", WatchService.class.getSimpleName()),
                e);
        }
    }

    @Nonnull
    @Override
    public synchronized List<ServiceBinding> getServiceBindings( @Nonnull final Collection<Path> directories )
    {
        removeOutdatedWatchKeys(directories);
        removeOutdatedServiceBindings(directories);
        return directories
            .stream()
            .peek(this::renewCachedServiceBindingIfNeeded)
            .map(cachedServiceBindings::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
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

    private void renewCachedServiceBindingIfNeeded( @Nonnull final Path directory )
    {
        @Nullable
        final WatchKey watchKey = directoryWatchKeys.get(directory);
        if( watchKey == null ) {
            watchAndCacheServiceBinding(directory);
            return;
        }

        if( !watchKey.isValid() ) {
            throw new IllegalStateException(
                String.format("%s for directory '%s' is invalid.", WatchKey.class.getSimpleName(), directory));
        }

        if( hasBeenModified(watchKey) ) {
            cacheServiceBinding(directory);
        }
    }

    private void watchAndCacheServiceBinding( @Nonnull final Path directory )
    {
        startWatching(directory);
        cacheServiceBinding(directory);
    }

    private void cacheServiceBinding( @Nonnull final Path directory )
    {
        cachedServiceBindings.remove(directory);

        @Nullable
        final ServiceBinding serviceBinding = serviceBindingLoader.apply(directory);
        if( serviceBinding == null ) {
            return;
        }

        cachedServiceBindings.put(directory, serviceBinding);
    }

    private void startWatching( @Nonnull final Path directory )
    {
        try {
            final WatchKey watchKey = directory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            directoryWatchKeys.put(directory, watchKey);
        }
        catch( final IOException e ) {
            throw new IllegalStateException(String.format("Unable to watch directory '%s'.", directory), e);
        }
    }

    private boolean hasBeenModified( @Nonnull final WatchKey watchKey )
    {
        final List<WatchEvent<?>> events = watchKey.pollEvents();
        watchKey.reset();

        return events.stream().map(WatchEvent::kind).anyMatch(MODIFICATION_EVENTS::contains);
    }
}
