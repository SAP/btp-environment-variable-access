/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

@FunctionalInterface
interface DirectoryBasedCache
{
    @Nonnull
    List<ServiceBinding> getServiceBindings( @Nonnull final Collection<Path> directories );
}
