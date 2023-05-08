/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileSystemWatcherCacheTest
{
    @Test
    void initialLoadWillHitTheFileSystem( @Nonnull @TempDir final Path rootDirectory )
        throws IOException
    {
        final Function<Path, ServiceBinding> mockedLoader = mockedServiceBindingLoader();
        final Path dir = Files.createDirectories(rootDirectory.resolve("dir"));

        final DirectoryBasedCache sut = new FileSystemWatcherCache(mockedLoader);
        final List<ServiceBinding> bindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(bindings.size()).isEqualTo(1);
        verify(mockedLoader, times(1)).apply(eq(dir));
    }

    @Test
    void subsequentLoadWillHitTheCache( @Nonnull @TempDir final Path rootDirectory )
        throws IOException
    {
        final Function<Path, ServiceBinding> mockedLoader = mockedServiceBindingLoader();
        final Path dir = Files.createDirectories(rootDirectory.resolve("dir"));

        final DirectoryBasedCache sut = new FileSystemWatcherCache(mockedLoader);
        final List<ServiceBinding> firstBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(firstBindings.size()).isEqualTo(1);
        verify(mockedLoader, times(1)).apply(eq(dir));

        final List<ServiceBinding> secondBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(secondBindings.size()).isEqualTo(1);
        verify(mockedLoader, times(1)).apply(eq(dir));
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void loadWillHitFileSystemWhenFileIsCreated( @Nonnull @TempDir final Path rootDirectory )
        throws IOException
    {
        final Function<Path, ServiceBinding> mockedLoader = mockedServiceBindingLoader();
        final Path dir = Files.createDirectories(rootDirectory.resolve("dir"));

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(mockedLoader);

        // manually create cache entries
        sut.cachedServiceBindings.put(dir, mock(ServiceBinding.class));

        final WatchKey mockedWatchKey = mock(WatchKey.class);
        when(mockedWatchKey.isValid()).thenReturn(true);
        // file system has not been changed
        when(mockedWatchKey.pollEvents()).thenReturn(Collections.emptyList());

        sut.directoryWatchKeys.put(dir, mockedWatchKey);

        final List<ServiceBinding> firstBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(firstBindings.size()).isEqualTo(1);
        // service binding is not reloaded
        verify(mockedLoader, times(0)).apply(eq(dir));
        verify(mockedWatchKey, times(1)).pollEvents();

        // mark the file system as changed
        final WatchEvent<Path> mockedWatchEvent = (WatchEvent<Path>) mock(WatchEvent.class);
        when(mockedWatchEvent.kind()).thenReturn(ENTRY_CREATE);
        when(mockedWatchKey.pollEvents()).thenReturn(Collections.singletonList(mockedWatchEvent));

        // load again
        final List<ServiceBinding> secondBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(secondBindings.size()).isEqualTo(1);
        // service binding is reloaded
        verify(mockedLoader, times(1)).apply(eq(dir));
        verify(mockedWatchKey, times(2)).pollEvents();
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void loadWillHitFileSystemWhenFileIsModified( @Nonnull @TempDir final Path rootDirectory )
        throws IOException
    {
        final Function<Path, ServiceBinding> mockedLoader = mockedServiceBindingLoader();
        final Path dir = Files.createDirectories(rootDirectory.resolve("dir"));

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(mockedLoader);

        // manually create cache entries
        sut.cachedServiceBindings.put(dir, mock(ServiceBinding.class));

        final WatchKey mockedWatchKey = mock(WatchKey.class);
        when(mockedWatchKey.isValid()).thenReturn(true);
        // file system has not been changed
        when(mockedWatchKey.pollEvents()).thenReturn(Collections.emptyList());

        sut.directoryWatchKeys.put(dir, mockedWatchKey);

        final List<ServiceBinding> firstBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(firstBindings.size()).isEqualTo(1);
        // service binding is not reloaded
        verify(mockedLoader, times(0)).apply(eq(dir));
        verify(mockedWatchKey, times(1)).pollEvents();

        // mark the file system as changed
        final WatchEvent<Path> mockedWatchEvent = (WatchEvent<Path>) mock(WatchEvent.class);
        when(mockedWatchEvent.kind()).thenReturn(ENTRY_MODIFY);
        when(mockedWatchKey.pollEvents()).thenReturn(Collections.singletonList(mockedWatchEvent));

        // load again
        final List<ServiceBinding> secondBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(secondBindings.size()).isEqualTo(1);
        // service binding is reloaded
        verify(mockedLoader, times(1)).apply(eq(dir));
        verify(mockedWatchKey, times(2)).pollEvents();
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void loadWillHitFileSystemWhenFileIsDeleted( @Nonnull @TempDir final Path rootDirectory )
        throws IOException
    {
        final Function<Path, ServiceBinding> mockedLoader = mockedServiceBindingLoader();
        final Path dir = Files.createDirectories(rootDirectory.resolve("dir"));

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(mockedLoader);

        // manually create cache entries
        sut.cachedServiceBindings.put(dir, mock(ServiceBinding.class));

        final WatchKey mockedWatchKey = mock(WatchKey.class);
        when(mockedWatchKey.isValid()).thenReturn(true);
        // file system has not been changed
        when(mockedWatchKey.pollEvents()).thenReturn(Collections.emptyList());

        sut.directoryWatchKeys.put(dir, mockedWatchKey);

        final List<ServiceBinding> firstBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(firstBindings.size()).isEqualTo(1);
        // service binding is not reloaded
        verify(mockedLoader, times(0)).apply(eq(dir));
        verify(mockedWatchKey, times(1)).pollEvents();

        // mark the file system as changed
        final WatchEvent<Path> mockedWatchEvent = (WatchEvent<Path>) mock(WatchEvent.class);
        when(mockedWatchEvent.kind()).thenReturn(ENTRY_DELETE);
        when(mockedWatchKey.pollEvents()).thenReturn(Collections.singletonList(mockedWatchEvent));

        // load again
        final List<ServiceBinding> secondBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(secondBindings.size()).isEqualTo(1);
        // service binding is reloaded
        verify(mockedLoader, times(1)).apply(eq(dir));
        verify(mockedWatchKey, times(2)).pollEvents();
    }

    @Test
    void loadWillRemoveCacheEntry( @Nonnull @TempDir final Path rootDirectory )
        throws IOException
    {
        final Function<Path, ServiceBinding> mockedLoader = mockedServiceBindingLoader();
        final Path dir1 = Files.createDirectories(rootDirectory.resolve("dir1"));
        final Path dir2 = Files.createDirectories(rootDirectory.resolve("dir2"));

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(mockedLoader);

        // manually create cache entries
        sut.cachedServiceBindings.put(dir1, mock(ServiceBinding.class));
        sut.cachedServiceBindings.put(dir2, mock(ServiceBinding.class));

        final WatchKey mockedWatchKey1 = mock(WatchKey.class);
        final WatchKey mockedWatchKey2 = mock(WatchKey.class);
        sut.directoryWatchKeys.put(dir1, mockedWatchKey1);
        sut.directoryWatchKeys.put(dir2, mockedWatchKey2);

        sut.getServiceBindings(Collections.emptyList());

        assertThat(sut.directoryWatchKeys).isEmpty();
        assertThat(sut.cachedServiceBindings).isEmpty();

        verify(mockedWatchKey1, times(1)).cancel();
        verify(mockedWatchKey2, times(1)).cancel();
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void loadWillIgnoreBindingWhenLoaderReturnsNull( @Nonnull @TempDir final Path rootDirectory )
        throws IOException
    {
        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        when(loader.apply(any())).thenReturn(null);

        final Path dir = Files.createDirectories(rootDirectory.resolve("dir"));

        final DirectoryBasedCache sut = new FileSystemWatcherCache(loader);

        final List<ServiceBinding> bindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(bindings.size()).isEqualTo(0);
        verify(loader, times(1)).apply(eq(dir));
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void watchServiceIsCached()
        throws Exception
    {
        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        final FileSystem fileSystemA = mock(FileSystem.class);
        final FileSystem fileSystemB = mock(FileSystem.class);
        final WatchService watchService = mock(WatchService.class);

        when(fileSystemA.newWatchService()).thenReturn(watchService);
        when(fileSystemB.newWatchService()).thenReturn(watchService);

        new FileSystemWatcherCache(loader, fileSystemA);
        verify(fileSystemA, times(1)).newWatchService();

        new FileSystemWatcherCache(loader, fileSystemA);
        verify(fileSystemA, times(1)).newWatchService();

        // using a different FileSystem will create a new WatchService
        new FileSystemWatcherCache(loader, fileSystemB);
        verify(fileSystemB, times(1)).newWatchService();
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void serviceBindingsAreLoadedIfWatchKeyIsInvalid() throws Exception
    {
        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        final FileSystem fileSystem = mock(FileSystem.class);
        final WatchService watchService = mock(WatchService.class);
        final Path directory = mock(Path.class);
        final WatchKey watchKey = mock(WatchKey.class);

        when(loader.apply(any())).thenReturn(null);

        when(fileSystem.newWatchService()).thenReturn(watchService);
        when(directory.register(eq(watchService), any())).thenReturn(watchKey);
        when(watchKey.isValid()).thenReturn(false); // the watch key is always invalid

        final DirectoryBasedCache sut = new FileSystemWatcherCache(loader, fileSystem);

        assertThat(sut.getServiceBindings(Collections.singletonList(directory))).isEmpty();
        verify(loader, times(1)).apply(any());

        assertThat(sut.getServiceBindings(Collections.singletonList(directory))).isEmpty(); // requesting the service bindings a second time will re-load them again
        verify(loader, times(2)).apply(any());
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void finalizeCancelsWatchKeys()
    {
        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        when(loader.apply(any())).thenReturn(null);

        final WatchKey watchKey = mock(WatchKey.class);

        final FileSystemWatcherCache sut = new FileSystemWatcherCache(loader);

        sut.directoryWatchKeys.put(Paths.get("some", "imaginary", "path"), watchKey);

        sut.finalize();

        verify(watchKey, times(1)).cancel();
        assertThat(sut.directoryWatchKeys).isEmpty();
    }

    @Disabled( "This test doesn't work reliably in the CI/CD pipeline. It can still be used for manual testing." )
    @Test
    @SuppressWarnings( "unchecked" )
    void watchKeysAreCancelledOnGcRun()
    {
        final Function<Path, ServiceBinding> loader = (Function<Path, ServiceBinding>) mock(Function.class);
        when(loader.apply(any())).thenReturn(null);

        final WatchKey watchKey = mock(WatchKey.class);

        FileSystemWatcherCache sut = new FileSystemWatcherCache(loader);

        sut.directoryWatchKeys.put(Paths.get("some", "imaginary", "path"), watchKey);

        sut = null;

        System.gc();
        verify(watchKey, times(1)).cancel();
    }

    @Nonnull
    @SuppressWarnings( "unchecked" )
    private static Function<Path, ServiceBinding> mockedServiceBindingLoader()
    {
        final Function<Path, ServiceBinding> mock = (Function<Path, ServiceBinding>) mock(Function.class);
        when(mock.apply(any())).thenReturn(mock(ServiceBinding.class));

        return mock;
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
