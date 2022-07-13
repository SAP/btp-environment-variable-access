/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        final Path path = get(testClass, fileName);
        try {
            return String.join("\n", Files.readAllLines(path, StandardCharsets.UTF_8));
        }
        catch( final IOException e ) {
            throw new AssertionError(
                String.format("Unable to read test resource from '%s/%s'", testClass.getSimpleName(), fileName),
                e);
        }
    }

    @Nonnull
    public static Path get( @Nonnull final Class<?> testClass, @Nonnull final String fileName )
    {
        return Paths.get(get(testClass).toString(), fileName);
    }

    @Nonnull
    public static Path get( @Nonnull final Class<?> testClass )
    {
        final URL url = testClass.getClassLoader().getResource(testClass.getSimpleName());
        if( url == null ) {
            throw new AssertionError(
                String.format("Unable to load test resource directory '%s'", testClass.getSimpleName()));
        }

        try {
            return Paths.get(url.toURI());
        }
        catch( final URISyntaxException e ) {
            throw new AssertionError(
                String.format("Unable to load test resource directory '%s'", testClass.getSimpleName()),
                e);
        }
    }
}
