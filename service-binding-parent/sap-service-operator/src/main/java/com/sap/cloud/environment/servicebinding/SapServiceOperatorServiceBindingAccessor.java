/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sap.cloud.environment.api.ServiceBinding;
import com.sap.cloud.environment.api.ServiceBindingAccessor;
import com.sap.cloud.environment.api.ServiceBindingAccessorOptions;
import com.sap.cloud.environment.api.exception.ServiceBindingAccessException;

public class SapServiceOperatorServiceBindingAccessor implements ServiceBindingAccessor
{
    @Nonnull
    public static final Path DEFAULT_ROOT_PATH = Paths.get("/etc/secrets/sapbtp");
    @Nonnull
    public static final Collection<ParsingStrategy> DEFAULT_PARSING_STRATEGIES = Collections.unmodifiableCollection(
            Arrays.asList(SecretRootKeyParsingStrategy.newDefault(),
                          SecretKeyParsingStrategy.newDefault(),
                          DataParsingStrategy.newDefault()));
    @Nonnull
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    @Nonnull
    private final Path rootPath;
    @Nonnull
    private final Collection<ParsingStrategy> parsingStrategies;

    public SapServiceOperatorServiceBindingAccessor()
    {
        this(DEFAULT_ROOT_PATH, DEFAULT_PARSING_STRATEGIES);
    }

    public SapServiceOperatorServiceBindingAccessor( @Nonnull final Path rootPath,
                                                     @Nonnull final Collection<ParsingStrategy> parsingStrategies )
    {
        this.rootPath = rootPath;
        this.parsingStrategies = parsingStrategies;
    }

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings( @Nonnull final ServiceBindingAccessorOptions options )
    {
        try {
            return Files.list(rootPath)
                        .filter(Files::isDirectory)
                        .flatMap(servicePath -> parseServiceBindings(servicePath, options))
                        .collect(Collectors.toList());
        } catch (final SecurityException | IOException e) {
            throw new ServiceBindingAccessException("Unable to access service binding files.", e);
        }
    }

    @Nonnull
<<<<<<< HEAD
    private Stream<ServiceBinding> parseServiceBindings( @Nonnull final Path servicePath,
                                                         @Nonnull final ServiceBindingAccessorOptions options )
=======
    private Stream<ServiceBinding> parseServiceBindings( @Nonnull final Path servicePath )
>>>>>>> 054bb54 (Docu and some API enhancements (#7))
    {
        try {
            return Files.list(servicePath)
                        .filter(Files::isDirectory)
                        .map(bindingPath -> parsingStrategies.stream()
                                                             .map(strategy -> applyStrategy(strategy,
                                                                                            servicePath,
                                                                                            bindingPath,
                                                                                            options))
                                                             .filter(Objects::nonNull)
                                                             .findFirst())
                        .filter(Optional::isPresent)
                        .map(Optional::get);
        } catch (final IOException e) {
            throw new ServiceBindingAccessException(String.format("Unable to access service binding files in '%s'.",
                                                                  servicePath), e);
        }
    }

    @Nullable
    private ServiceBinding applyStrategy( @Nonnull final ParsingStrategy strategy,
                                          @Nonnull final Path servicePath,
<<<<<<< HEAD
                                          @Nonnull final Path bindingPath,
                                          @Nonnull final ServiceBindingAccessorOptions options )
=======
                                          @Nonnull final Path bindingPath )
>>>>>>> 054bb54 (Docu and some API enhancements (#7))
    {
        try {
            return strategy.parse(servicePath.getFileName().toString(),
                                  bindingPath.getFileName().toString(),
                                  bindingPath,
                                  options);
        } catch (final IOException e) {
            return null;
        }
    }
}
