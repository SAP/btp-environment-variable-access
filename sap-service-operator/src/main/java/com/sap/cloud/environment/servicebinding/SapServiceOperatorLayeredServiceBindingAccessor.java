package com.sap.cloud.environment.servicebinding;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBindingAccessor;
import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link ServiceBindingAccessor} that is able to load <b>layered</b> {@link ServiceBinding}s from the file system.
 * <br>
 * The <b>layered</b> structure is assumed to look as follows:
 *
 * <pre>
 *     ${SERVICE-BINDING-ROOT}
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
 * <b>Note:</b> This class will attempt to read service bindings from {@code /etc/secrets/sapbtp} <b>if</b> the
 * {@code SERVICE_BINDING_ROOT} environment variable is not defined (i.e.
 * {@code System.getenv("SERVICE_BINDING_ROOT") == null}). The {@code {SERVICE-BINDING-CONTENT}} itself can also have
 * different structures, which are supported through different {@link LayeredParsingStrategy}s. By default, following
 * strategies are applied:
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
     * The default {@link Function} to read environment variables.
     */
    @Nonnull
    public static final Function<String, String> DEFAULT_ENVIRONMENT_VARIABLE_READER = System::getenv;

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
    private static final String ROOT_DIRECTORY_KEY = "SERVICE_BINDING_ROOT";

    @Nonnull
    private final Function<String, String> environmentVariableReader;
    @Nonnull
    private final Path fallbackRootPath;

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

    public SapServiceOperatorLayeredServiceBindingAccessor(
        @Nonnull final Function<String, String> environmentVariableReader,
        @Nonnull final Collection<LayeredParsingStrategy> parsingStrategies )
    {
        this(environmentVariableReader, DEFAULT_ROOT_PATH, parsingStrategies);
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
        this(any -> null, rootPath, parsingStrategies);
    }

    SapServiceOperatorLayeredServiceBindingAccessor(
        @Nonnull final Function<String, String> environmentVariableReader,
        @Nonnull final Path fallbackRootPath,
        @Nonnull final Collection<LayeredParsingStrategy> parsingStrategies )
    {
        this.environmentVariableReader = environmentVariableReader;
        this.fallbackRootPath = fallbackRootPath;
        this.parsingStrategies = parsingStrategies;
    }

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings()
    {
        final Path rootPath = getRootDirectory();
        if( rootPath == null ) {
            return Collections.emptyList();
        }

        logger.debug("Trying to read service bindings from '{}'.", rootPath);
        try( final Stream<Path> servicePaths = Files.list(rootPath).filter(Files::isDirectory) ) {
            return parseServiceBindings(servicePaths);
        }
        catch( final SecurityException | IOException e ) {
            throw new ServiceBindingAccessException("Unable to access service binding files.", e);
        }
    }

    @Nullable
    private Path getRootDirectory()
    {
        logger
            .debug(
                "Trying to determine service binding root directory using the '{}' environment variable.",
                ROOT_DIRECTORY_KEY);
        final String maybeRootDirectory = environmentVariableReader.apply(ROOT_DIRECTORY_KEY);
        if( maybeRootDirectory == null || maybeRootDirectory.isEmpty() ) {
            logger.debug("Environment variable '{}' is not defined.", ROOT_DIRECTORY_KEY);
            return getFallbackRootDirectory();
        }

        final Path rootDirectory = Paths.get(maybeRootDirectory);
        if( !Files.isDirectory(rootDirectory) ) {
            logger
                .debug(
                    "Environment variable '{}' ('{}') does not point to a valid directory.",
                    ROOT_DIRECTORY_KEY,
                    maybeRootDirectory);
            return null;
        }

        return rootDirectory;
    }

    @Nullable
    private Path getFallbackRootDirectory()
    {
        logger.debug("Trying to fall back to '{}'.", fallbackRootPath);
        if( !Files.isDirectory(fallbackRootPath) ) {
            logger.debug("Fallback '{}' ('{}') is not a valid directory.", ROOT_DIRECTORY_KEY, fallbackRootPath);
            return null;
        }

        return fallbackRootPath;
    }

    @Nonnull
    private List<ServiceBinding> parseServiceBindings( @Nonnull final Stream<Path> servicePaths )
    {
        return servicePaths.flatMap(servicePath -> {
            try {
                return Files.list(servicePath).filter(Files::isDirectory);
            }
            catch( final IOException e ) {
                throw new ServiceBindingAccessException(
                    String.format("Unable to access files in '%s'.", servicePath),
                    e);
            }
        }).map(this::parseServiceBinding).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Nullable
    private ServiceBinding parseServiceBinding( @Nonnull final Path bindingRoot )
    {
        final Path servicePath = bindingRoot.getParent();
        return parsingStrategies
            .stream()
            .map(strategy -> applyStrategy(strategy, servicePath, bindingRoot))
            .filter(Optional::isPresent)
            .findFirst()
            .orElse(Optional.empty())
            .orElse(null);
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
