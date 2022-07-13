/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

import static org.assertj.core.api.Assertions.assertThat;

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

        final SapServiceOperatorLayeredServiceBindingAccessor sut =
            new SapServiceOperatorLayeredServiceBindingAccessor(
                path,
                SapServiceOperatorLayeredServiceBindingAccessor.DEFAULT_PARSING_STRATEGIES);

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

        assertThat(secretRootKeyBinding.getName().orElse("")).isEqualTo("secret-root-key-binding");
        assertThat(secretRootKeyBinding.getServiceName().orElse("")).isEqualTo("xsuaa");
        assertThat(secretRootKeyBinding.getServicePlan().orElse("")).isEqualTo("secret-root-key-xsuaa-plan");
        assertThat(secretRootKeyBinding.getTags())
            .containsExactly("secret-root-key-xsuaa-tag-1", "secret-root-key-xsuaa-tag-2");
        assertThat(secretRootKeyBinding.getCredentials()).containsKeys("clientid", "clientsecret");
    }

    @Test
    void parseIgnoresInvalidBindings()
    {
        final Path path = TestResource.get(SapServiceOperatorLayeredServiceBindingAccessorTest.class, "InvalidBinding");

        final SapServiceOperatorLayeredServiceBindingAccessor sut =
            new SapServiceOperatorLayeredServiceBindingAccessor(
                path,
                SapServiceOperatorLayeredServiceBindingAccessor.DEFAULT_PARSING_STRATEGIES);

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

        final SapServiceOperatorLayeredServiceBindingAccessor sut =
            new SapServiceOperatorLayeredServiceBindingAccessor(
                path,
                SapServiceOperatorLayeredServiceBindingAccessor.DEFAULT_PARSING_STRATEGIES);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings).isNotNull();
        assertThat(serviceBindings).isEmpty();
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

        assertThat(secretKeyBinding.getName().orElse("")).isEqualTo("secret-key-binding");
        assertThat(secretKeyBinding.getServiceName().orElse("")).isEqualTo("xsuaa");
        assertThat(secretKeyBinding.getServicePlan().orElse("")).isEqualTo("secret-key-xsuaa-plan");
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

        assertThat(dataBinding.getName().orElse("")).isEqualTo("data-binding");
        assertThat(dataBinding.getServiceName().orElse("")).isEqualTo("xsuaa");
        assertThat(dataBinding.getServicePlan().orElse("")).isEqualTo("data-xsuaa-plan");
        assertThat(dataBinding.getTags()).containsExactly("data-xsuaa-tag-1", "data-xsuaa-tag-2");
        assertThat(dataBinding.getCredentials()).containsOnlyKeys("domains", "clientid", "clientsecret");
    }
}
