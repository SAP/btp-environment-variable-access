/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileSystemWatcherCacheTest
{
    private Function<Path, ServiceBinding> mockedLoader;

    @BeforeEach
    @SuppressWarnings( "unchecked" )
    void setupMockedLoader()
    {
        mockedLoader = (Function<Path, ServiceBinding>) mock(Function.class);
        when(mockedLoader.apply(any())).thenReturn(mock(ServiceBinding.class));
    }

    @Test
    void initialLoadWillHitTheFileSystem( @Nonnull @TempDir final Path rootDirectory )
    {
        final Path dir1 = rootDirectory.resolve("dir1");
        final Path dir2 = rootDirectory.resolve("dir2");
        write(dir1.resolve("file1"), "foo");
        write(dir2.resolve("file2"), "bar");

        final DirectoryBasedCache sut = new FileSystemWatcherCache(mockedLoader);
        final List<ServiceBinding> bindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(bindings.size()).isEqualTo(2);
        verify(mockedLoader, times(1)).apply(eq(dir1));
        verify(mockedLoader, times(1)).apply(eq(dir2));
    }

    @Test
    void subsequentLoadWillHitTheCache( @Nonnull @TempDir final Path rootDirectory )
    {
        final Path dir1 = rootDirectory.resolve("dir1");
        final Path dir2 = rootDirectory.resolve("dir2");
        write(dir1.resolve("file1"), "foo");
        write(dir2.resolve("file2"), "bar");

        final DirectoryBasedCache sut = new FileSystemWatcherCache(mockedLoader);
        final List<ServiceBinding> firstBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(firstBindings.size()).isEqualTo(2);
        verify(mockedLoader, times(1)).apply(eq(dir1));
        verify(mockedLoader, times(1)).apply(eq(dir2));

        final List<ServiceBinding> secondBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(secondBindings.size()).isEqualTo(2);
        verify(mockedLoader, times(1)).apply(eq(dir1));
        verify(mockedLoader, times(1)).apply(eq(dir2));
    }

    @Test
    void loadWillHitFileSystemWhenFileIsCreated( @Nonnull @TempDir final Path rootDirectory )
    {
        final Path dir = rootDirectory.resolve("dir1");
        write(dir.resolve("file1"), "foo");

        final DirectoryBasedCache sut = new FileSystemWatcherCache(mockedLoader);
        final List<ServiceBinding> firstBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(firstBindings.size()).isEqualTo(1);
        verify(mockedLoader, times(1)).apply(eq(dir));

        // create a new file
        write(dir.resolve("file2"), "bar");
        final List<ServiceBinding> secondBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(secondBindings.size()).isEqualTo(1);
        verify(mockedLoader, times(2)).apply(eq(dir));
    }

    @Test
    void loadWillHitFileSystemWhenFileIsModified( @Nonnull @TempDir final Path rootDirectory )
    {
        final Path dir = rootDirectory.resolve("dir1");
        final Path file = write(dir.resolve("file1"), "foo");

        final DirectoryBasedCache sut = new FileSystemWatcherCache(mockedLoader);
        final List<ServiceBinding> firstBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(firstBindings.size()).isEqualTo(1);
        verify(mockedLoader, times(1)).apply(eq(dir));

        // modify existing file
        write(file, "bar");
        final List<ServiceBinding> secondBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(secondBindings.size()).isEqualTo(1);
        verify(mockedLoader, times(2)).apply(eq(dir));
    }

    @Test
    void loadWillHitFileSystemWhenFileIsDeleted( @Nonnull @TempDir final Path rootDirectory )
        throws IOException
    {
        final Path dir = rootDirectory.resolve("dir1");
        final Path file = write(dir.resolve("file1"), "foo");

        final DirectoryBasedCache sut = new FileSystemWatcherCache(mockedLoader);
        final List<ServiceBinding> firstBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(firstBindings.size()).isEqualTo(1);
        verify(mockedLoader, times(1)).apply(eq(dir));

        // delete existing file
        Files.delete(file);
        final List<ServiceBinding> secondBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(secondBindings.size()).isEqualTo(1);
        verify(mockedLoader, times(2)).apply(eq(dir));
    }

    @Test
    void loadWillRemoveCacheEntry( @Nonnull @TempDir final Path rootDirectory )
    {
        final Path dir = rootDirectory.resolve("dir1");
        write(dir.resolve("file1"), "foo");

        final DirectoryBasedCache sut = new FileSystemWatcherCache(mockedLoader);
        final List<ServiceBinding> firstBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(firstBindings.size()).isEqualTo(1);
        verify(mockedLoader, times(1)).apply(eq(dir));

        // load without the existing directory will remove the cache entry
        final List<ServiceBinding> secondBindings = sut.getServiceBindings(Collections.emptyList());

        assertThat(secondBindings.size()).isEqualTo(0);
        verify(mockedLoader, times(1)).apply(eq(dir));

        // load with the existing directory will hit the file system,
        // even though the file system has not been changed in the meanwhile
        final List<ServiceBinding> thirdBindings = sut.getServiceBindings(getAllDirectories(rootDirectory));

        assertThat(thirdBindings.size()).isEqualTo(1);
        verify(mockedLoader, times(2)).apply(eq(dir));
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

    @Nonnull
    private static Path write( @Nonnull final Path filePath, @Nonnull final String content )
    {
        try {
            if( !Files.exists(filePath.getParent()) ) {
                Files.createDirectories(filePath.getParent());
            }

            Files.write(filePath, Collections.singletonList(content), StandardCharsets.UTF_8);
            return filePath;
        }
        catch( final IOException e ) {
            fail("Failed to write test file content.", e);
            throw new AssertionError("Should not be reached.");
        }
    }

    @Nonnull
    @SuppressWarnings( "resource" )
    private static Stream<Path> getAllDirectories( @Nonnull final Path rootDirectory )
    {
        try {
            return Files.list(rootDirectory).filter(Files::isDirectory);
        }
        catch( final IOException e ) {
            throw new AssertionError(String.format("Unable to get all directories in '%s'.", rootDirectory), e);
        }
    }
}
