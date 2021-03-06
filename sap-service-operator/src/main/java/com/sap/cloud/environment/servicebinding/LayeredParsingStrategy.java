/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

@FunctionalInterface
public interface LayeredParsingStrategy
{
    @Nonnull
    Optional<ServiceBinding>
        parse( @Nonnull final String serviceName, @Nonnull final String bindingName, @Nonnull final Path bindingPath )
            throws IOException;
}
