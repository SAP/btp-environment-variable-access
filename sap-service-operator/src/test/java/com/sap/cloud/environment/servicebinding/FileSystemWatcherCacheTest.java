/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileSystemWatcherCacheTest
{
    @Test
    @SuppressWarnings( "unchecked" )
    void getServiceBindingsWillHitFileSystemForTheFirstInvocationOnly( @Nonnull @TempDir final Path root )
        throws IOException
    {
        final Path bindingRoot = Files.createDirectories(root.resolve("binding"));
        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        doReturn(mock(ServiceBinding.class)).when(loader).apply(eq(bindingRoot));

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(loader);

        final List<ServiceBinding> firstBindings = sut.getServiceBindings(getAllDirectories(root));
        assertThat(firstBindings).hasSize(1);
        verify(loader, times(1)).apply(eq(bindingRoot));

        final List<ServiceBinding> secondBindings = sut.getServiceBindings(getAllDirectories(root));
        assertThat(secondBindings).hasSize(1);
        assertThat(secondBindings.get(0)).isSameAs(firstBindings.get(0));
        verify(loader, times(1)).apply(eq(bindingRoot)); // no further invocation
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void getServiceBindingsTriggersReconciliationIfFileSystemIsClosed( @Nonnull @TempDir final Path root )
        throws IOException
    {
        final Path bindingRoot = Files.createDirectories(root.resolve("binding"));
        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        doReturn(mock(ServiceBinding.class)).when(loader).apply(eq(bindingRoot));

        final FileSystem closedFileSystem = mock(FileSystem.class);
        when(closedFileSystem.isOpen()).thenReturn(false);
        final Supplier<FileSystem> fileSystemSupplier = (Supplier<FileSystem>) mock(Supplier.class);
        doReturn(closedFileSystem).when(fileSystemSupplier).get();

        final int expectedInvocations = 4;
        final int maxReconciliationAttempts = expectedInvocations - 1;
        final FileSystemWatcherCache sut =
            new FileSystemWatcherCache(loader, fileSystemSupplier, maxReconciliationAttempts);

        for( int i = 0; i < expectedInvocations; ++i ) {
            final List<ServiceBinding> bindings = sut.getServiceBindings(getAllDirectories(root));
            assertThat(bindings).hasSize(1);

            verify(loader, times(i + 1)).apply(eq(bindingRoot));
            verify(fileSystemSupplier, times(i + 1)).get();
            verify(closedFileSystem, times(0)).newWatchService();
        }

        // invoking the method once again will not attempt another reconciliation
        final List<ServiceBinding> bindings = sut.getServiceBindings(getAllDirectories(root));
        assertThat(bindings).hasSize(1);

        verify(loader, times(expectedInvocations + 1)).apply(eq(bindingRoot));
        verify(fileSystemSupplier, times(expectedInvocations)).get();
        verify(closedFileSystem, times(0)).newWatchService();
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void getServiceBindingsTriggersReconciliationIfWatchServiceIsClosed( @Nonnull @TempDir final Path root )
        throws IOException
    {
        final Path bindingRoot = Files.createDirectories(root.resolve("binding"));
        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        doReturn(mock(ServiceBinding.class)).when(loader).apply(eq(bindingRoot));

        final WatchService closedWatchService = mock(WatchService.class);
        doThrow(ClosedWatchServiceException.class).when(closedWatchService).poll();
        final FileSystem fileSystem = mock(FileSystem.class);
        doReturn(true).when(fileSystem).isOpen();
        doReturn(closedWatchService).when(fileSystem).newWatchService();
        final Supplier<FileSystem> fileSystemSupplier = (Supplier<FileSystem>) mock(Supplier.class);
        doReturn(fileSystem).when(fileSystemSupplier).get();

        final int expectedInvocations = 4;
        final int maxReconciliationAttempts = expectedInvocations - 1;
        final FileSystemWatcherCache sut =
            new FileSystemWatcherCache(loader, fileSystemSupplier, maxReconciliationAttempts);

        for( int i = 0; i < expectedInvocations; ++i ) {
            final List<ServiceBinding> bindings = sut.getServiceBindings(getAllDirectories(root));
            assertThat(bindings).hasSize(1);

            verify(loader, times(i + 1)).apply(eq(bindingRoot));
            verify(fileSystemSupplier, times(i + 1)).get();
            verify(fileSystem, times(i + 1)).newWatchService();
            verify(closedWatchService, times(i + 1)).poll();
        }

        // invoking the method once again will not attempt another reconciliation
        final List<ServiceBinding> bindings = sut.getServiceBindings(getAllDirectories(root));
        assertThat(bindings).hasSize(1);

        verify(loader, times(expectedInvocations + 1)).apply(eq(bindingRoot));
        verify(fileSystemSupplier, times(expectedInvocations)).get();
        verify(fileSystem, times(expectedInvocations)).newWatchService();
        verify(closedWatchService, times(expectedInvocations)).poll();
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void getServiceBindingsTriggersReconciliationIfWatchServiceCreationFails( @Nonnull @TempDir final Path root )
        throws IOException
    {
        final Path bindingRoot = Files.createDirectories(root.resolve("binding"));
        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        doReturn(mock(ServiceBinding.class)).when(loader).apply(eq(bindingRoot));

        final FileSystem fileSystem = mock(FileSystem.class);
        doReturn(true).when(fileSystem).isOpen();
        doThrow(IOException.class).when(fileSystem).newWatchService();
        final Supplier<FileSystem> fileSystemSupplier = (Supplier<FileSystem>) mock(Supplier.class);
        doReturn(fileSystem).when(fileSystemSupplier).get();

        final int expectedInvocations = 4;
        final int maxReconciliationAttempts = expectedInvocations - 1;
        final FileSystemWatcherCache sut =
            new FileSystemWatcherCache(loader, fileSystemSupplier, maxReconciliationAttempts);

        for( int i = 0; i < expectedInvocations; ++i ) {
            final List<ServiceBinding> bindings = sut.getServiceBindings(getAllDirectories(root));
            assertThat(bindings).hasSize(1);

            verify(loader, times(i + 1)).apply(eq(bindingRoot));
            verify(fileSystemSupplier, times(i + 1)).get();
            verify(fileSystem, times(i + 1)).newWatchService();
        }

        // invoking the method once again will not attempt another reconciliation
        final List<ServiceBinding> bindings = sut.getServiceBindings(getAllDirectories(root));
        assertThat(bindings).hasSize(1);

        verify(loader, times(expectedInvocations + 1)).apply(eq(bindingRoot));
        verify(fileSystemSupplier, times(expectedInvocations)).get();
        verify(fileSystem, times(expectedInvocations)).newWatchService();
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void getServiceBindingsHitsFileSystemIfFileSystemSupplierThrowsException()
    {
        final Path bindingRoot = mock(Path.class);
        final ServiceBinding binding = mock(ServiceBinding.class);
        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        doReturn(binding).when(loader).apply(eq(bindingRoot));

        final Supplier<FileSystem> fileSystemSupplier = (Supplier<FileSystem>) mock(Supplier.class);
        doThrow(IllegalStateException.class).when(fileSystemSupplier).get();

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(loader, fileSystemSupplier, 3);
        final List<ServiceBinding> bindings = sut.getServiceBindings(Collections.singletonList(bindingRoot));
        assertThat(bindings).containsExactly(binding);

        verify(loader, times(1)).apply(eq(bindingRoot));
        verify(fileSystemSupplier, times(1)).get();
        assertThat(sut.fileSystem).isNull();
        assertThat(sut.watchService).isNull();
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void getServiceBindingsTriggerReconciliationOnFollowingInvocationIfWatchKeyIsInvalid()
        throws IOException
    {
        final WatchKey invalidWatchKey = mockWatchKey(false);
        final WatchService watchService = mockWatchService(true);
        final Path bindingRoot = mockPath(invalidWatchKey);
        final FileSystem fileSystem = mockFileSystem(watchService);

        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        doReturn(mock(ServiceBinding.class)).when(loader).apply(eq(bindingRoot));
        final Supplier<FileSystem> fileSystemSupplier = (Supplier<FileSystem>) mock(Supplier.class);
        doReturn(fileSystem).when(fileSystemSupplier).get();

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(loader, fileSystemSupplier, 1);

        // first invocation: do lazy initialization
        {
            final List<ServiceBinding> bindings = sut.getServiceBindings(Collections.singletonList(bindingRoot));
            assertThat(bindings).hasSize(1);

            // lazily initialize the file system ...
            verify(fileSystemSupplier, times(1)).get();
            verify(fileSystem, times(3)).isOpen();
            // ... and the watch service.
            verify(fileSystem, times(1)).newWatchService();
            verify(watchService, times(2)).poll();
            // cache the service binding as it is the first invocation ...
            verify(loader, times(1)).apply(eq(bindingRoot));
            // ... but don't check the watch key (yet)
            verify(invalidWatchKey, times(0)).isValid();
            // also don't release any resources
            verify(invalidWatchKey, times(0)).cancel();
            verify(watchService, times(0)).close();
        }

        // second invocation: check if watch key is valid
        {
            final List<ServiceBinding> bindings = sut.getServiceBindings(Collections.singletonList(bindingRoot));
            assertThat(bindings).hasSize(1);

            // check whether reconciliation is needed
            verify(fileSystem, times(5)).isOpen();
            verify(watchService, times(4)).poll();

            // watch key became invalid...
            verify(invalidWatchKey, times(1)).isValid();
            // ... therefore, we will reload the service binding from the file system ...
            verify(loader, times(2)).apply(eq(bindingRoot));
            // ... and release all resources afterward
            verify(invalidWatchKey, times(1)).cancel();
            verify(watchService, times(1)).close();
        }

        // third invocation: trigger reconciliation as the previous attempt released all resources
        {
            final List<ServiceBinding> bindings = sut.getServiceBindings(Collections.singletonList(bindingRoot));
            assertThat(bindings).hasSize(1);

            // re-create the file system ...
            verify(fileSystemSupplier, times(2)).get();
            verify(fileSystem, times(9)).isOpen();
            // ... and the watch service.
            verify(fileSystem, times(2)).newWatchService();
            verify(watchService, times(6)).poll();

            // cache the service binding as it has been removed from the cache ...
            verify(loader, times(3)).apply(eq(bindingRoot));
            // ... but don't check the watch key again
            verify(invalidWatchKey, times(1)).isValid();
            // also don't release any resources
            verify(invalidWatchKey, times(1)).cancel();
            verify(watchService, times(1)).close();
        }
    }

    @ParameterizedTest
    @MethodSource( "watchEventTypes" )
    @SuppressWarnings( "unchecked" )
    void getServiceBindingsReloadsIndividualBindingIfDirectoryHasBeenModified(
        @Nonnull final WatchEvent.Kind<Path> eventKind )
        throws IOException
    {
        final WatchKey firstWatchKey = mockWatchKey(true);
        final WatchKey secondWatchKey = mockWatchKey(true);

        final Path firstBindingRoot = mockPath(firstWatchKey);
        final Path secondBindingRoot = mockPath(secondWatchKey);

        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        doReturn(mock(ServiceBinding.class)).when(loader).apply(any());

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(loader);
        final ServiceBinding initialFirstBinding = mock(ServiceBinding.class);
        final ServiceBinding initialSecondBinding = mock(ServiceBinding.class);
        sut.cachedServiceBindings.put(firstBindingRoot, initialFirstBinding);
        sut.directoryWatchKeys.put(firstBindingRoot, firstWatchKey);
        sut.cachedServiceBindings.put(secondBindingRoot, initialSecondBinding);
        sut.directoryWatchKeys.put(secondBindingRoot, secondWatchKey);

        // first invocation: all bindings are served from the cache
        {
            final List<ServiceBinding> bindings =
                sut.getServiceBindings(Arrays.asList(firstBindingRoot, secondBindingRoot));
            assertThat(bindings).hasSize(2);
            assertThat(bindings).containsExactlyInAnyOrder(initialFirstBinding, initialSecondBinding);

            verifyIsPolled(firstWatchKey, 1);
            verifyIsPolled(secondWatchKey, 1);

            verify(loader, times(0)).apply(any());
        }

        // second invocation: one watch key reports new events
        {
            final WatchEvent<Path> modificationEvent = (WatchEvent<Path>) mock(WatchEvent.class);
            doReturn(eventKind).when(modificationEvent).kind();
            doReturn(Collections.singletonList(modificationEvent)).when(firstWatchKey).pollEvents();

            final List<ServiceBinding> bindings =
                sut.getServiceBindings(Arrays.asList(firstBindingRoot, secondBindingRoot));
            assertThat(bindings).hasSize(2);
            assertThat(bindings).contains(initialSecondBinding).doesNotContain(initialFirstBinding);

            verifyIsPolled(firstWatchKey, 2);
            verifyIsPolled(secondWatchKey, 2);

            verify(loader, times(1)).apply(eq(firstBindingRoot));
            verify(loader, times(0)).apply(eq(secondBindingRoot));
        }
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void getServiceBindingsGraduallyRecoversReconciliationAttempts()
        throws IOException
    {
        final WatchKey watchKey = mockWatchKey(true);
        final Path bindingRoot = mockPath(watchKey);
        final WatchService watchService = mockWatchService(true);
        final FileSystem fileSystem = mockFileSystem(watchService);
        final ServiceBinding binding = mock(ServiceBinding.class);

        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        doReturn(null).when(loader).apply(eq(bindingRoot));

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(loader, () -> fileSystem, 3);

        // put the cache in a state where everything is fine, but it already applied reconciliation more than once
        sut.fileSystem = fileSystem;
        sut.watchService = new FileSystemWatcherCache.AutoClosingWatchService(watchService);
        sut.directoryWatchKeys.put(bindingRoot, watchKey);
        sut.cachedServiceBindings.put(bindingRoot, binding);
        sut.reconciliationAttempts = 2;

        final List<ServiceBinding> bindings = sut.getServiceBindings(Collections.singletonList(bindingRoot));
        assertThat(bindings).containsExactly(binding); // binding is served from cache

        verify(loader, times(0)).apply(any());
        assertThat(sut.reconciliationAttempts).isEqualTo(1);

        sut.getServiceBindings(Collections.singletonList(bindingRoot));
        assertThat(sut.reconciliationAttempts).isEqualTo(0);

        sut.getServiceBindings(Collections.singletonList(bindingRoot));
        assertThat(sut.reconciliationAttempts).isEqualTo(0); // getting the bindings again won't decrease the reconciliation attempts below 0
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void getServiceBindingsIgnoresNullResults( @Nonnull @TempDir final Path root )
        throws IOException
    {
        final Path bindingRoot = Files.createDirectories(root.resolve("binding"));
        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        doReturn(null).when(loader).apply(eq(bindingRoot));

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(loader);
        final List<ServiceBinding> bindings = sut.getServiceBindings(getAllDirectories(root));
        assertThat(bindings).isEmpty();

        verify(loader, times(1)).apply(eq(bindingRoot));
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void getServiceBindingsRemovesOutdatedBinding()
        throws IOException
    {
        final WatchKey watchKey = mockWatchKey(true);
        final Path bindingPath = mockPath(watchKey);
        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        doReturn(null).when(loader).apply(any());

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(loader);
        sut.cachedServiceBindings.put(bindingPath, mock(ServiceBinding.class));
        sut.directoryWatchKeys.put(bindingPath, watchKey);

        final List<ServiceBinding> bindings = sut.getServiceBindings(Collections.emptyList());
        assertThat(bindings).isEmpty();
        assertThat(sut.cachedServiceBindings).isEmpty();
        assertThat(sut.directoryWatchKeys).isEmpty();

        verify(watchKey, times(1)).cancel();
        verify(loader, times(0)).apply(any());
    }

    @Test
    void finalizeCallsReleaseResources()
        throws Throwable
    {
        final FileSystemWatcherCache sut = spy(new FileSystemWatcherCache(any -> null));

        sut.finalize();

        verify(sut, times(1)).releaseResources();
    }

    @Test
    void releaseResourcesClosesAllResources()
        throws IOException
    {
        final WatchKey watchKey = mockWatchKey(false);
        final Path bindingPath = mockPath(watchKey);
        final WatchService watchService = mockWatchService(true);
        final FileSystem fileSystem = mockFileSystem(watchService);

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(any -> null, () -> fileSystem, 3);
        sut.fileSystem = fileSystem;
        sut.watchService = new FileSystemWatcherCache.AutoClosingWatchService(watchService);
        sut.cachedServiceBindings.put(bindingPath, mock(ServiceBinding.class));
        sut.directoryWatchKeys.put(bindingPath, watchKey);

        sut.releaseResources();

        assertThat(sut.cachedServiceBindings).isEmpty();
        assertThat(sut.directoryWatchKeys).isEmpty();
        assertThat(sut.fileSystem).isNull();
        assertThat(sut.watchService).isNull();

        verify(watchKey, times(1)).cancel();
        verify(watchService, times(1)).close();
    }

    @Test
    void testReleaseResourcesDoesNotReleaseWatchServiceIfAnotherReferenceExists()
        throws IOException
    {
        final WatchKey watchKey = mockWatchKey(true);
        final Path bindingPath = mockPath(watchKey);
        final WatchService watchService = mockWatchService(true);
        final FileSystem fileSystem = mockFileSystem(watchService);

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(any -> null, () -> fileSystem, 3);
        sut.getServiceBindings(Collections.singletonList(bindingPath)); // make sure everything has been initialized

        final FileSystemWatcherCache anotherCacheInstance =
            new FileSystemWatcherCache(any -> null, () -> fileSystem, 3);
        anotherCacheInstance.getServiceBindings(Collections.singletonList(bindingPath)); // make sure everything has been initialized

        assertThat(sut.watchService).isSameAs(anotherCacheInstance.watchService).isNotNull();

        sut.releaseResources();

        assertThat(sut.fileSystem).isNull();
        assertThat(sut.watchService).isNull();
        verify(watchService, times(0)).close(); // watch service has not yet been closed

        anotherCacheInstance.releaseResources();

        assertThat(anotherCacheInstance.fileSystem).isNull();
        assertThat(anotherCacheInstance.watchService).isNull();
        verify(watchService, times(1)).close();
    }

    @Nonnull
    private static Stream<WatchEvent.Kind<Path>> watchEventTypes()
    {
        return Stream.of(ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
    }

    @Nonnull
    private static WatchKey mockWatchKey( final boolean isValid )
    {
        final WatchKey watchKey = mock(WatchKey.class);
        doReturn(isValid).when(watchKey).isValid();
        doReturn(Collections.emptyList()).when(watchKey).pollEvents();
        doReturn(true).when(watchKey).reset();
        doNothing().when(watchKey).cancel();

        return watchKey;
    }

    @Nonnull
    private static Path mockPath( @Nonnull final WatchKey watchKey )
        throws IOException
    {
        final Path path = mock(Path.class);
        doReturn(watchKey).when(path).register(any(), any());

        return path;
    }

    @Nonnull
    private static WatchService mockWatchService( final boolean isOpen )
        throws IOException
    {
        final WatchService watchService = mock(WatchService.class);
        doNothing().when(watchService).close();

        if( isOpen ) {
            doReturn(null).when(watchService).poll();
        } else {
            doThrow(ClosedWatchServiceException.class).when(watchService).poll();
        }

        return watchService;
    }

    @Nonnull
    private static FileSystem mockFileSystem( @Nonnull final WatchService watchService )
        throws IOException
    {
        final FileSystem fileSystem = mock(FileSystem.class);
        doReturn(true).when(fileSystem).isOpen();
        doReturn(watchService).when(fileSystem).newWatchService();

        return fileSystem;
    }

    private static void verifyIsPolled( @Nonnull final WatchKey watchKey, int expectedTimes )
    {
        verify(watchKey, times(expectedTimes)).reset();
        verify(watchKey, times(expectedTimes)).pollEvents();
    }

    @Nonnull
    private static Collection<Path> getAllDirectories( @Nonnull final Path rootDirectory )
    {
        try( final Stream<Path> dirs = Files.list(rootDirectory).filter(Files::isDirectory) ) {
            return dirs.collect(Collectors.toList());
        }
        catch( final IOException e ) {
            throw new AssertionError(String.format("Unable to get all directories in '%s'.", rootDirectory), e);
        }
    }
}
