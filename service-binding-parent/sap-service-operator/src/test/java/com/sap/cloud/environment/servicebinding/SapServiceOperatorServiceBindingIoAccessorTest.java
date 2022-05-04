/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;


import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sap.cloud.environment.api.ServiceBinding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SapServiceOperatorServiceBindingIoAccessorTest
{
    @Test
    void defaultConstructorExists()
    {
        assertThat(new SapServiceOperatorServiceBindingIoAccessor()).isNotNull();
    }

    @SuppressWarnings( "unchecked" )
    @Test
    void getServiceBindingsReadsEnvironmentVariable()
    {
        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(any())).thenReturn(null);

        final SapServiceOperatorServiceBindingIoAccessor sut = new SapServiceOperatorServiceBindingIoAccessor(reader,
                                                                                                              SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET);

        sut.getServiceBindings();

        verify(reader, times(1)).apply(eq("SERVICE_BINDING_ROOT"));
    }

    @SuppressWarnings( "unchecked" )
    @Test
    void parseMixedServiceBindings()
    {
        final Path rootDirectory = TestResource.get(SapServiceOperatorServiceBindingIoAccessorTest.class);

        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(eq("SERVICE_BINDING_ROOT"))).thenReturn(rootDirectory.toString());

        final SapServiceOperatorServiceBindingIoAccessor sut = new SapServiceOperatorServiceBindingIoAccessor(reader,
                                                                                                              SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings.size()).isEqualTo(2);

        // assert data binding has been parsed
        final List<ServiceBinding> dataBinding = serviceBindings.stream()
                                                                .filter(binding -> "data-xsuaa-binding".equals(binding.getName()
                                                                                                                      .orElse(null)))
                                                                .collect(Collectors.toList());
        assertThat(dataBinding.size()).isEqualTo(1);

        final ServiceBinding dataXsuaaBinding = dataBinding.get(0);
        assertThat(dataXsuaaBinding.getName().orElse(null)).isEqualTo("data-xsuaa-binding");
        assertThat(dataXsuaaBinding.getServiceName().orElse(null)).isEqualTo("xsuaa");
        assertThat(dataXsuaaBinding.getServicePlan().orElse(null)).isEqualTo("application");
        assertThat(dataXsuaaBinding.getTags()).containsExactlyInAnyOrder("data-xsuaa-tag-1", "data-xsuaa-tag-2");
        assertThat(dataXsuaaBinding.get("instance_guid").orElse(null)).isEqualTo("data-xsuaa-instance-guid");
        assertThat(dataXsuaaBinding.get("instance_name").orElse(null)).isEqualTo("data-xsuaa-instance-name");
        assertThat(dataXsuaaBinding.getCredentials()).isNotEmpty();
        assertThat(dataXsuaaBinding.getCredentials().get("clientid")).isEqualTo("data-xsuaa-clientid");
        assertThat(dataXsuaaBinding.getCredentials().get("clientsecret")).isEqualTo("data-xsuaa-clientsecret");
        assertThat(dataXsuaaBinding.getCredentials().get("domains")).asList()
                                                                    .containsExactlyInAnyOrder("data-xsuaa-domain-1",
                                                                                               "data-xsuaa-domain-2");

        // assert secret key binding has been parsed
        final List<ServiceBinding> secretKeyXsuaaBindings = serviceBindings.stream()
                                                                           .filter(binding -> "secret-key-xsuaa-binding".equals(
                                                                                   binding.getName().orElse(null)))
                                                                           .collect(Collectors.toList());

        assertThat(secretKeyXsuaaBindings.size()).isEqualTo(1);

        final ServiceBinding secretKeyBinding = secretKeyXsuaaBindings.get(0);
        assertThat(secretKeyBinding.getName().orElse(null)).isEqualTo("secret-key-xsuaa-binding");
        assertThat(secretKeyBinding.getServiceName().orElse(null)).isEqualTo("xsuaa");
        assertThat(secretKeyBinding.getServicePlan().orElse(null)).isEqualTo("lite");
        assertThat(secretKeyBinding.getTags()).containsExactlyInAnyOrder("secret-key-xsuaa-tag-1",
                                                                         "secret-key-xsuaa-tag-2");
        assertThat(secretKeyBinding.get("instance_guid").orElse(null)).isEqualTo("secret-key-xsuaa-instance-guid");
        assertThat(secretKeyBinding.get("instance_name").orElse(null)).isEqualTo("secret-key-xsuaa-instance-name");
        assertThat(secretKeyBinding.getCredentials()).isNotEmpty();
        assertThat(secretKeyBinding.getCredentials().get("clientid")).isEqualTo("secret-key-xsuaa-clientid");
        assertThat(secretKeyBinding.getCredentials().get("clientsecret")).isEqualTo("secret-key-xsuaa-clientsecret");
        assertThat(secretKeyBinding.getCredentials().get("url")).isEqualTo("https://secret-key-xsuaa-domain-1.com");
        assertThat(secretKeyBinding.getCredentials().get("zone_uuid")).isEqualTo("secret-key-xsuaa-zone-uuid");
        assertThat(secretKeyBinding.getCredentials().get("domain")).isEqualTo("secret-key-xsuaa-domain-1");
        assertThat(secretKeyBinding.getCredentials().get("domains")).asList()
                                                                    .containsExactlyInAnyOrder(
                                                                            "secret-key-xsuaa-domain-1");
    }
}