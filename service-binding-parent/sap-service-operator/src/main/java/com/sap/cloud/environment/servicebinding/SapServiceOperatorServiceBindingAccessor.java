package com.sap.cloud.environment.servicebinding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sap.cloud.environment.api.ServiceBinding;
import com.sap.cloud.environment.api.ServiceBindingAccessor;
import com.sap.cloud.environment.api.exception.ServiceBindingAccessException;

public class SapServiceOperatorServiceBindingAccessor implements ServiceBindingAccessor {
    @Nonnull
    public static final Path DEFAULT_ROOT_PATH = Paths.get("/etc/secrets/sapcp");
    @Nonnull
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    @Nonnull
    private final Supplier<Path> rootPathSupplier;
    @Nonnull
    private final List<ParsingStrategy> parsingStrategies;

//    public SapServiceOperatorServiceBindingAccessor() {
//        this(() -> DEFAULT_ROOT_PATH);
//    }

    public SapServiceOperatorServiceBindingAccessor(@Nonnull final Supplier<Path> rootPathSupplier,
                                                    @Nonnull final List<ParsingStrategy> parsingStrategies) {
        this.rootPathSupplier = rootPathSupplier;
        this.parsingStrategies = parsingStrategies;
    }

    @Nonnull
    @Override
    public Iterable<ServiceBinding> getServiceBindings() {
        try {
            final Path rootPath = rootPathSupplier.get();
            return Files.list(rootPath).filter(Files::isDirectory).flatMap(this::parseServiceBindings).collect(Collectors.toList());

        } catch (final SecurityException | IOException e) {
            // TODO: propagate exception
            throw new ServiceBindingAccessException();
        }
    }

    @Nonnull
    private Stream<ServiceBinding> parseServiceBindings(@Nonnull final Path servicePath) {
        try {
            return Files.list(servicePath)
                    .filter(Files::isDirectory)
                    .map(bindingPath ->
                            parsingStrategies.stream()
                                    .map(strategy -> applyStrategy(strategy, servicePath, bindingPath))
                                    .filter(Objects::nonNull)
                                    .findFirst())
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        } catch (final IOException e) {
            // TODO: propagate exception
            throw new ServiceBindingAccessException();
        }
    }

    @Nullable
    private ServiceBinding applyStrategy(@Nonnull final ParsingStrategy strategy, @Nonnull final Path servicePath, @Nonnull final Path bindingPath) {
        try {
            return strategy.parse(servicePath.getFileName().toString(), bindingPath.getFileName().toString(), bindingPath);
        } catch (final IOException e) {
            return null;
        }
    }
}
