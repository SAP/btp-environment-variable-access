/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

import static org.assertj.core.api.Assertions.assertThat;

class LayeredSecretRootKeyParsingStrategyTest
{
    @Test
    void multipleFilesLeadToEmptyResult()
        throws IOException
    {
        final Path path = TestResource.get(LayeredSecretRootKeyParsingStrategyTest.class, "MultipleFiles");

        final LayeredSecretRootKeyParsingStrategy sut = LayeredSecretRootKeyParsingStrategy.newDefault();

        final Optional<ServiceBinding> serviceBinding = sut.parse("service", "binding", path);

        assertThat(serviceBinding.isPresent()).isFalse();
    }

    @Test
    void fileWithoutJsonLeadsToEmptyResult()
        throws IOException
    {
        final Path path = TestResource.get(LayeredSecretRootKeyParsingStrategyTest.class, "NotAJsonFile");

        final LayeredSecretRootKeyParsingStrategy sut = LayeredSecretRootKeyParsingStrategy.newDefault();

        final Optional<ServiceBinding> serviceBinding = sut.parse("service", "binding", path);

        assertThat(serviceBinding.isPresent()).isFalse();
    }

    @Test
    void parseValidBinding()
        throws IOException
    {
        final Path path = TestResource.get(LayeredSecretRootKeyParsingStrategyTest.class, "ValidBinding");

        final LayeredSecretRootKeyParsingStrategy sut = LayeredSecretRootKeyParsingStrategy.newDefault();

        final ServiceBinding serviceBinding = sut.parse("XSUAA", "my-xsuaa-binding", path).orElse(null);

        assertThat(serviceBinding).isNotNull();
        assertThat(serviceBinding.getName().orElse("")).isEqualTo("my-xsuaa-binding");
        assertThat(serviceBinding.getServiceName().orElse("")).isEqualTo("XSUAA");
        assertThat(serviceBinding.getServicePlan().orElse("")).isEqualTo("lite");
        assertThat(serviceBinding.getTags()).containsExactly("tag1", "tag2");
        assertThat(serviceBinding.getCredentials()).containsKeys("clientid", "clientsecret");
    }
}
