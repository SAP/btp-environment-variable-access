/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;

import com.sap.cloud.environment.api.ServiceBinding;
import com.sap.cloud.environment.api.ServiceBindingAccessorOptions;

@FunctionalInterface
public interface ParsingStrategy
{
    @Nullable
    ServiceBinding parse( @Nonnull final String serviceName,
                          @Nonnull final String bindingName,
                          @Nonnull final Path bindingPath,
                          @Nonnull final ServiceBindingAccessorOptions options ) throws IOException;
}
