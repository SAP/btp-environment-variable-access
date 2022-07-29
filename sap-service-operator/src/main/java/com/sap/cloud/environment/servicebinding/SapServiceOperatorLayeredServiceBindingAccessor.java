/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBindingAccessor;
import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link ServiceBindingAccessor} that is able to load <b>layered</b> {@link ServiceBinding}s from the file system.
 * <br>
 * The <b>layered</b> structure is assumed to look as follows:
 *
 * <pre>
 *     {SERVICE-BINDING-ROOT}
 *     ├-- {SERVICE-NAME#1}
 *     |   ├-- {SERVICE-BINDING-NAME#1}
 *     |   |   └-- {SERVICE-BINDING-CONTENT#1}
 *     |   └-- {SERVICE-BINDING-NAME#2}
 *     |       └-- {SERVICE-BINDING-CONTENT#2}
 *     └-- {SERVICE-NAME#2}
 *         └-- {SERVICE-BINDING-NAME#3}
 *             └- {SERVICE-BINDING-CONTENT#3}
 * </pre>
 * <p>
 * By default, {@code /etc/secrets/sapbtp} is used as the {@code SERVICE-BINDING-ROOT}. <br>
 * The {@code {SERVICE-BINDING-CONTENT}} itself can also have different structures, which are supported through
 * different {@link LayeredParsingStrategy}s. By default, following strategies are applied:
 * <ol>
 * <li>{@link LayeredSecretRootKeyParsingStrategy}</li>
 * <li>{@link LayeredSecretKeyParsingStrategy}</li>
 * <li>{@link LayeredDataParsingStrategy}</li>
 * </ol>
 * The <b>order</b> of the applied strategies <b>is important</b> as only the first parsed value for each service
 * binding will be considered.
 */
public class SapServiceOperatorLayeredServiceBindingAccessor implements ServiceBindingAccessor
{
    @Nonnull
    private static final Logger logger = LoggerFactory.getLogger(SapServiceOperatorLayeredServiceBindingAccessor.class);

    /**
     * The default service binding root {@link Path}.
     */
    @Nonnull
    public static final Path DEFAULT_ROOT_PATH = Paths.get("/etc/secrets/sapbtp");

    /**
     * The default {@link LayeredParsingStrategy}s.
     */
    @Nonnull
    public static final Collection<LayeredParsingStrategy> DEFAULT_PARSING_STRATEGIES =
        Collections
            .unmodifiableCollection(
                Arrays
                    .asList(
                        LayeredSecretRootKeyParsingStrategy.newDefault(),
                        LayeredSecretKeyParsingStrategy.newDefault(),
                        LayeredDataParsingStrategy.newDefault()));

    /**
     * The default {@link Charset} that should be used to read property files.
     */
    @Nonnull
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    @Nonnull
    private final Path rootPath;

    @Nonnull
    private final Collection<LayeredParsingStrategy> parsingStrategies;

    /**
     * Initializes a new {@link SapServiceOperatorLayeredServiceBindingAccessor} instance that uses the
     * {@link #DEFAULT_ROOT_PATH} and the {@link #DEFAULT_PARSING_STRATEGIES}.
     */
    public SapServiceOperatorLayeredServiceBindingAccessor()
    {
        this(DEFAULT_ROOT_PATH, DEFAULT_PARSING_STRATEGIES);
    }

    /**
     * Initializes a new {@link SapServiceOperatorLayeredServiceBindingAccessor} that uses the given {@code rootPath}
     * and {@code parsingStrategies}.
     * 
     * @param rootPath
     *            The service binding root {@link Path} that should be used.
     * @param parsingStrategies
     *            The {@link LayeredParsingStrategy}s that should be used.
     */
    public SapServiceOperatorLayeredServiceBindingAccessor(
        @Nonnull final Path rootPath,
        @Nonnull final Collection<LayeredParsingStrategy> parsingStrategies )
    {
        this.rootPath = rootPath;
        this.parsingStrategies = parsingStrategies;
    }

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings()
    {
        logger.debug("Trying to read service bindings from '{}'.", rootPath);

        if( !Files.exists(rootPath) || !Files.isDirectory(rootPath) ) {
            logger.debug("Skipping '{}': Directory does not exist.", rootPath);
            return Collections.emptyList();
        }

        try( final Stream<Path> files = Files.list(rootPath) ) {
            return files.filter(Files::isDirectory).flatMap(this::parseServiceBindings).collect(Collectors.toList());
        }
        catch( final SecurityException | IOException e ) {
            throw new ServiceBindingAccessException("Unable to access service binding files.", e);
        }
    }

    @Nonnull
    private Stream<ServiceBinding> parseServiceBindings( @Nonnull final Path servicePath )
    {
        try {
            return Files
                .list(servicePath)
                .filter(Files::isDirectory)
                .map(
                    bindingPath -> parsingStrategies
                        .stream()
                        .map(strategy -> applyStrategy(strategy, servicePath, bindingPath))
                        .filter(Optional::isPresent)
                        .findFirst()
                        .orElse(Optional.empty()))
                .filter(Optional::isPresent)
                .map(Optional::get);
        }
        catch( final IOException e ) {
            throw new ServiceBindingAccessException(
                String.format("Unable to access service binding files in '%s'.", servicePath),
                e);
        }
    }

    @Nonnull
    private Optional<ServiceBinding> applyStrategy(
        @Nonnull final LayeredParsingStrategy strategy,
        @Nonnull final Path servicePath,
        @Nonnull final Path bindingPath )
    {
        try {
            return strategy
                .parse(servicePath.getFileName().toString(), bindingPath.getFileName().toString(), bindingPath);
        }
        catch( final IOException e ) {
            return Optional.empty();
        }
    }
}
