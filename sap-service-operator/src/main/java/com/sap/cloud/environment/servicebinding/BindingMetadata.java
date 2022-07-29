/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

class BindingMetadata
{
    @Nonnull
    private final List<BindingProperty> metadataProperties;

    @Nonnull
    private final List<BindingProperty> credentialProperties;

    BindingMetadata(
        @Nonnull final Collection<BindingProperty> metadataProperties,
        @Nonnull final Collection<BindingProperty> credentialProperties )
    {
        this.metadataProperties = Collections.unmodifiableList(new ArrayList<>(metadataProperties));
        this.credentialProperties = Collections.unmodifiableList(new ArrayList<>(credentialProperties));
    }

    @Nonnull
    public List<BindingProperty> getMetadataProperties()
    {
        return metadataProperties;
    }

    @Nonnull
    public List<BindingProperty> getCredentialProperties()
    {
        return credentialProperties;
    }

}
