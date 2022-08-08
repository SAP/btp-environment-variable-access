/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

public final class TestResource
{
    private TestResource()
    {
        throw new IllegalStateException("This utility class must not be initialized.");
    }

    @Nonnull
    public static String read( @Nonnull final Class<?> testClass, @Nonnull final String fileName )
    {
        final Path resourcePath = get(testClass, fileName);
        try {
            return String.join("\n", Files.readAllLines(resourcePath));
        }
        catch( final IOException e ) {
            throw new AssertionError(
                String
                    .format("Unable to read test resource content from '%s/%s.'", testClass.getSimpleName(), fileName));
        }
    }

    @Nonnull
    public static Path get( @Nonnull final Class<?> testClass, @Nonnull final String fileName )
    {
        final URL url = testClass.getClassLoader().getResource(testClass.getSimpleName());
        if( url == null ) {
            throw new AssertionError(
                String.format("Unable to load test source from '%s/%s'", testClass.getSimpleName(), fileName));
        }

        final String rootFolder;
        try {
            rootFolder = Paths.get(url.toURI()).toString();
            return Paths.get(rootFolder, fileName);
        }
        catch( final URISyntaxException e ) {
            throw new AssertionError(
                String.format("Unable to load test source from '%s/%s'", testClass.getSimpleName(), fileName));
        }
    }
}
