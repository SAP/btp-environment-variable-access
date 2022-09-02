/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

@FunctionalInterface
interface DirectoryBasedCache
{
    @Nonnull
    List<ServiceBinding> getServiceBindings( @Nonnull final Collection<Path> directories );

    @Nonnull
    default List<ServiceBinding> getServiceBindings( @Nonnull final Stream<Path> directories )
    {
        return getServiceBindings(directories.filter(Files::isDirectory).collect(Collectors.toList()));
    }
}
