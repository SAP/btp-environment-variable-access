/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

/**
 * Represents a method object to parse a {@link ServiceBinding} from a specific layered file structure (see
 * {@link SapServiceOperatorLayeredServiceBindingAccessor}).
 */
@FunctionalInterface
public interface LayeredParsingStrategy
{
    /**
     * Tries to initialize a new {@link ServiceBinding} instance from the files contained in the given
     * {@code bindingPath}.
     *
     * @param serviceName
     *            The name of the {@link ServiceBinding} (see {@link ServiceBinding#getName()}).
     * @param bindingName
     *            The name of the bound service of the {@link ServiceBinding} (see
     *            {@link ServiceBinding#getServiceName()}).
     * @param bindingPath
     *            The {@link Path} that contains the property file(s).
     * @return An {@link Optional} that might contain a new {@link ServiceBinding} instance if the files contained in
     *         the given {@code bindingPath} match the expected structure.
     * @throws IOException
     *             Thrown if reading property files failed.
     */
    @Nonnull
    Optional<ServiceBinding>
        parse( @Nonnull final String serviceName, @Nonnull final String bindingName, @Nonnull final Path bindingPath )
            throws IOException;
}
