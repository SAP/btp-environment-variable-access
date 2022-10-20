/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBindingAccessor;

import static com.sap.cloud.environment.servicebinding.SapServiceOperatorLayeredServiceBindingAccessor.DEFAULT_PARSING_STRATEGIES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SapServiceOperatorLayeredServiceBindingAccessorTest
{
    @Test
    void defaultConstructorExists()
    {
        assertThat(new SapServiceOperatorLayeredServiceBindingAccessor()).isNotNull();
    }

    @Test
    void parseMixedBindings()
    {
        final Path path = TestResource.get(SapServiceOperatorLayeredServiceBindingAccessorTest.class, "MixedBindings");

        final ServiceBindingAccessor sut =
            new SapServiceOperatorLayeredServiceBindingAccessor(path, DEFAULT_PARSING_STRATEGIES);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings).hasSize(3);

        assertContainsSecretRootKeyBinding(serviceBindings);
        assertContainsSecretKeyBinding(serviceBindings);
        assertContainsDataBinding(serviceBindings);
    }

    private static void assertContainsSecretRootKeyBinding( @Nonnull final List<ServiceBinding> serviceBindings )
    {
        final ServiceBinding secretRootKeyBinding =
            serviceBindings
                .stream()
                .filter(binding -> binding.getName().orElse("").equals("secret-root-key-binding"))
                .findFirst()
                .orElse(null);
        assertThat(secretRootKeyBinding).isNotNull();

        assertThat(secretRootKeyBinding.getKeys()).containsExactlyInAnyOrder("tags", "plan", "credentials");

        assertThat(secretRootKeyBinding.getName()).hasValue("secret-root-key-binding");
        assertThat(secretRootKeyBinding.getServiceName()).hasValue("xsuaa");
        assertThat(secretRootKeyBinding.getServicePlan()).hasValue("secret-root-key-xsuaa-plan");
        assertThat(secretRootKeyBinding.getTags())
            .containsExactly("secret-root-key-xsuaa-tag-1", "secret-root-key-xsuaa-tag-2");
        assertThat(secretRootKeyBinding.getCredentials()).containsKeys("clientid", "clientsecret");
    }

    @Test
    void parseIgnoresInvalidBindings()
    {
        final Path path = TestResource.get(SapServiceOperatorLayeredServiceBindingAccessorTest.class, "InvalidBinding");

        final ServiceBindingAccessor sut =
            new SapServiceOperatorLayeredServiceBindingAccessor(path, DEFAULT_PARSING_STRATEGIES);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings).hasSize(2);

        assertContainsSecretRootKeyBinding(serviceBindings);
        assertContainsSecretKeyBinding(serviceBindings);
    }

    @Test
    void parseIgnoresNonExistingRootDirectory()
    {
        final Path path = Paths.get("this-directory-does-not-exist");

        assertThat(path).doesNotExist();

        final ServiceBindingAccessor sut =
            new SapServiceOperatorLayeredServiceBindingAccessor(path, DEFAULT_PARSING_STRATEGIES);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings).isNotNull();
        assertThat(serviceBindings).isEmpty();
    }

    @Test
    void serviceBindingsAreServedFromCache( @Nonnull @TempDir final Path rootDirectory )
        throws IOException
    {
        Files.createDirectories(rootDirectory.resolve("service").resolve("binding"));

        final List<ServiceBinding> serviceBindings = Collections.singletonList(mock(ServiceBinding.class));
        final DirectoryBasedCache mockedCache = mock(DirectoryBasedCache.class);
        when(mockedCache.getServiceBindings(any())).thenReturn(serviceBindings);

        final ServiceBindingAccessor sut =
            new SapServiceOperatorLayeredServiceBindingAccessor(rootDirectory, DEFAULT_PARSING_STRATEGIES, mockedCache);

        assertThat(sut.getServiceBindings()).isSameAs(serviceBindings);
        verify(mockedCache, times(1)).getServiceBindings(any());

        assertThat(sut.getServiceBindings()).isSameAs(serviceBindings);
        verify(mockedCache, times(2)).getServiceBindings(any());
    }

    private static void assertContainsSecretKeyBinding( @Nonnull final List<ServiceBinding> serviceBindings )
    {
        final ServiceBinding secretKeyBinding =
            serviceBindings
                .stream()
                .filter(binding -> binding.getName().orElse("").equals("secret-key-binding"))
                .findFirst()
                .orElse(null);
        assertThat(secretKeyBinding).isNotNull();

        assertThat(secretKeyBinding.getKeys())
            .containsExactlyInAnyOrder("instance_guid", "instance_name", "label", "plan", "credentials");

        assertThat(secretKeyBinding.getName()).hasValue("secret-key-binding");
        assertThat(secretKeyBinding.getServiceName()).hasValue("xsuaa");
        assertThat(secretKeyBinding.getServicePlan()).hasValue("secret-key-xsuaa-plan");
        assertThat(secretKeyBinding.getTags()).isEmpty();
        assertThat(secretKeyBinding.getCredentials())
            .containsOnlyKeys("domain", "domains", "clientid", "clientsecret", "url", "zone_uuid");
    }

    private static void assertContainsDataBinding( @Nonnull final List<ServiceBinding> serviceBindings )
    {
        final ServiceBinding dataBinding =
            serviceBindings
                .stream()
                .filter(binding -> binding.getName().orElse("").equals("data-binding"))
                .findFirst()
                .orElse(null);
        assertThat(dataBinding).isNotNull();

        assertThat(dataBinding.getKeys())
            .containsExactlyInAnyOrder("instance_guid", "instance_name", "label", "plan", "tags", "credentials");

        assertThat(dataBinding.getName()).hasValue("data-binding");
        assertThat(dataBinding.getServiceName()).hasValue("xsuaa");
        assertThat(dataBinding.getServicePlan()).hasValue("data-xsuaa-plan");
        assertThat(dataBinding.getTags()).containsExactly("data-xsuaa-tag-1", "data-xsuaa-tag-2");
        assertThat(dataBinding.getCredentials()).containsOnlyKeys("domains", "clientid", "clientsecret");
    }
}
