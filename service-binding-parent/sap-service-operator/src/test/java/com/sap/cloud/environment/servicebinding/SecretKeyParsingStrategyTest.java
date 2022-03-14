/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.sap.cloud.environment.api.ServiceBinding;

import static org.assertj.core.api.Assertions.assertThat;

class SecretKeyParsingStrategyTest
{
    @Test
    @SuppressWarnings( "unchecked" )
    void parseValidBinding() throws IOException
    {
        final Path path = TestResource.get(SecretKeyParsingStrategyTest.class, "ValidBinding");

        final SecretKeyParsingStrategy sut = SecretKeyParsingStrategy.newDefault();

        final ServiceBinding serviceBinding = sut.parse("XSUAA", "my-xsuaa-binding", path);

        assertThat(serviceBinding).isNotNull();
        assertThat(serviceBinding.getName().orElse("")).isEqualTo("my-xsuaa-binding");
        assertThat(serviceBinding.getServiceName().orElse("")).isEqualTo("XSUAA");
        assertThat(serviceBinding.getServicePlan().orElse("")).isEqualTo("my-plan");
        assertThat(serviceBinding.getTags()).isEmpty();

        assertThat(serviceBinding.getCredentials()).containsOnlyKeys("domain",
                                                                     "domains",
                                                                     "clientid",
                                                                     "clientsecret",
                                                                     "url",
                                                                     "zone_uuid");
        assertThat(serviceBinding.getCredentials()).containsEntry("domain", "my-trusted-domain");
        assertThat(serviceBinding.getCredentials().get("domains")).isInstanceOf(List.class);
        assertThat((List<Object>) serviceBinding.getCredentials().get("domains")).containsExactly("my-trusted-domain");
        assertThat(serviceBinding.getCredentials()).containsEntry("clientid", "my-client-id");
        assertThat(serviceBinding.getCredentials()).containsEntry("clientsecret", "my-client-secret");
        assertThat(serviceBinding.getCredentials()).containsEntry("url", "https://my-trusted-domain.com");
        assertThat(serviceBinding.getCredentials()).containsEntry("zone_uuid", "my-zone-uuid");
    }

    @Test
    void parsingTwoJsonFilesLeadsToNull() throws IOException
    {
        final Path path = TestResource.get(SecretKeyParsingStrategyTest.class, "TwoJsonFiles");

        final SecretKeyParsingStrategy sut = SecretKeyParsingStrategy.newDefault();

        final ServiceBinding serviceBinding = sut.parse("XSUAA", "my-xsuaa-binding", path);

        assertThat(serviceBinding).isNull();
    }

    @Test
    void parsingNoJsonFileLeadsToNull() throws IOException
    {
        final Path path = TestResource.get(SecretKeyParsingStrategyTest.class, "NoJsonFile");

        final SecretKeyParsingStrategy sut = SecretKeyParsingStrategy.newDefault();

        final ServiceBinding serviceBinding = sut.parse("XSUAA", "my-xsuaa-binding", path);

        assertThat(serviceBinding).isNull();
    }
}