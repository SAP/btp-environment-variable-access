/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import com.sap.cloud.environment.api.ServiceBinding;

@FunctionalInterface
public interface ParsingStrategy
{
    @Nonnull
    Optional<ServiceBinding> parse( @Nonnull final String serviceName,
                                    @Nonnull final String bindingName,
                                    @Nonnull final Path bindingPath ) throws IOException;
}
