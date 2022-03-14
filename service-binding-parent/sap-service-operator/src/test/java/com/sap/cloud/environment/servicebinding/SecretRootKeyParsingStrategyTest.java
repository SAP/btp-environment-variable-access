/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import com.sap.cloud.environment.api.ServiceBinding;

import static org.assertj.core.api.Assertions.assertThat;

class SecretRootKeyParsingStrategyTest
{
    @Test
    void multipleFilesLeadToNull() throws IOException
    {
        final Path path = TestResource.get(SecretRootKeyParsingStrategyTest.class, "MultipleFiles");

        final SecretRootKeyParsingStrategy sut = SecretRootKeyParsingStrategy.newDefault();

        final ServiceBinding serviceBinding = sut.parse("service", "binding", path);

        assertThat(serviceBinding).isNull();
    }

    @Test
    void fileWithoutJsonLeadsToNull() throws IOException
    {
        final Path path = TestResource.get(SecretRootKeyParsingStrategyTest.class, "NotAJsonFile");

        final SecretRootKeyParsingStrategy sut = SecretRootKeyParsingStrategy.newDefault();

        final ServiceBinding serviceBinding = sut.parse("service", "binding", path);

        assertThat(serviceBinding).isNull();
    }

    @Test
    void parseValidBinding() throws IOException
    {
        final Path path = TestResource.get(SecretRootKeyParsingStrategyTest.class, "ValidBinding");

        final SecretRootKeyParsingStrategy sut = SecretRootKeyParsingStrategy.newDefault();

        final ServiceBinding serviceBinding = sut.parse("XSUAA", "my-xsuaa-binding", path);

        assertThat(serviceBinding).isNotNull();
        assertThat(serviceBinding.getName().orElse("")).isEqualTo("my-xsuaa-binding");
        assertThat(serviceBinding.getServiceName().orElse("")).isEqualTo("XSUAA");
        assertThat(serviceBinding.getServicePlan().orElse("")).isEqualTo("lite");
        assertThat(serviceBinding.getTags()).containsExactly("tag1", "tag2");
        assertThat(serviceBinding.getCredentials()).containsKeys("clientid", "clientsecret");
    }
}