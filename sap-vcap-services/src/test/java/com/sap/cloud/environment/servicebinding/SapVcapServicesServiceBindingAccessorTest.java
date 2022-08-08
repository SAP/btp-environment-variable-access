/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

import static org.assertj.core.api.Assertions.assertThat;

class SapVcapServicesServiceBindingAccessorTest
{
    @Test
    void parseFullVcapServices()
    {
        final String vcapServices =
            TestResource.read(SapVcapServicesServiceBindingAccessorTest.class, "FullVcapServices.json");

        final SapVcapServicesServiceBindingAccessor sut =
            new SapVcapServicesServiceBindingAccessor(any -> vcapServices);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings.size()).isEqualTo(3);

        assertContainsXsuaaBinding1(serviceBindings);
        assertContainsXsuaaBinding2(serviceBindings);
        assertContainsDestinationBinding1(serviceBindings);
    }

    private void assertContainsXsuaaBinding1( @Nonnull final Collection<ServiceBinding> serviceBindings )
    {
        final List<ServiceBinding> bindings =
            serviceBindings
                .stream()
                .filter(binding -> "xsuaa-binding-1".equalsIgnoreCase(binding.getName().orElse("")))
                .collect(Collectors.toList());

        assertThat(bindings.size()).isEqualTo(1);

        final ServiceBinding binding = bindings.get(0);

        assertThat(binding.getKeys()).containsExactlyInAnyOrder("name", "plan", "tags", "credentials");

        assertThat(binding.getName().orElse("")).isEqualTo("xsuaa-binding-1");
        assertThat(binding.getServicePlan().orElse("")).isEqualTo("lite");
        assertThat(binding.getTags()).containsExactly("xsuaa-binding-1-tag-1", "xsuaa-binding-1-tag-2");
        assertThat(binding.getCredentials()).containsOnlyKeys("uri", "clientid", "clientsecret");
        assertThat(binding.getCredentials().get("uri")).isEqualTo("https://xsuaa-1.domain.com");
        assertThat(binding.getCredentials().get("clientid")).isEqualTo("xsuaa-clientid-1");
        assertThat(binding.getCredentials().get("clientsecret")).isEqualTo("xsuaa-clientsecret-1");
    }

    private void assertContainsXsuaaBinding2( @Nonnull final Collection<ServiceBinding> serviceBindings )
    {
        final List<ServiceBinding> bindings =
            serviceBindings
                .stream()
                .filter(binding -> "xsuaa-binding-2".equalsIgnoreCase(binding.getName().orElse("")))
                .collect(Collectors.toList());

        assertThat(bindings.size()).isEqualTo(1);

        final ServiceBinding binding = bindings.get(0);

        assertThat(binding.getKeys()).containsExactlyInAnyOrder("name", "plan", "tags", "credentials");

        assertThat(binding.getName().orElse("")).isEqualTo("xsuaa-binding-2");
        assertThat(binding.getServicePlan().orElse("")).isEqualTo("application");
        assertThat(binding.getTags()).containsExactly("xsuaa-binding-2-tag-1", "xsuaa-binding-2-tag-2");
        assertThat(binding.getCredentials()).containsOnlyKeys("uri", "clientid", "clientsecret");
        assertThat(binding.getCredentials().get("uri")).isEqualTo("https://xsuaa-2.domain.com");
        assertThat(binding.getCredentials().get("clientid")).isEqualTo("xsuaa-clientid-2");
        assertThat(binding.getCredentials().get("clientsecret")).isEqualTo("xsuaa-clientsecret-2");
    }

    private void assertContainsDestinationBinding1( @Nonnull final Collection<ServiceBinding> serviceBindings )
    {
        final List<ServiceBinding> bindings =
            serviceBindings
                .stream()
                .filter(binding -> "destination-binding-1".equalsIgnoreCase(binding.getName().orElse("")))
                .collect(Collectors.toList());

        assertThat(bindings.size()).isEqualTo(1);

        final ServiceBinding binding = bindings.get(0);

        assertThat(binding.getKeys()).containsExactlyInAnyOrder("name", "plan", "tags", "credentials");

        assertThat(binding.getName().orElse("")).isEqualTo("destination-binding-1");
        assertThat(binding.getServicePlan().orElse("")).isEqualTo("broker");
        assertThat(binding.getTags()).containsExactly("destination-binding-1-tag-1", "destination-binding-1-tag-2");
        assertThat(binding.getCredentials()).containsOnlyKeys("uri", "clientid", "clientsecret");
        assertThat(binding.getCredentials().get("uri")).isEqualTo("https://destination-1.domain.com");
        assertThat(binding.getCredentials().get("clientid")).isEqualTo("destination-clientid-1");
        assertThat(binding.getCredentials().get("clientsecret")).isEqualTo("destination-clientsecret-1");
    }

    @Test
    void nullEnvironmentVariableLeadsToEmptyResult()
    {
        final SapVcapServicesServiceBindingAccessor sut = new SapVcapServicesServiceBindingAccessor(any -> null);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings.size()).isEqualTo(0);
    }

    @Test
    void brokenBindingIsIgnored()
    {
        final String vcapServices =
            TestResource.read(SapVcapServicesServiceBindingAccessorTest.class, "VcapServicesWithBrokenBinding.json");

        final SapVcapServicesServiceBindingAccessor sut =
            new SapVcapServicesServiceBindingAccessor(any -> vcapServices);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings.size()).isEqualTo(1);

        assertContainsXsuaaBinding1(serviceBindings);
    }

    @Test
    void resultIsNotCached()
    {
        final String vcapServices =
            TestResource.read(SapVcapServicesServiceBindingAccessorTest.class, "FullVcapServices.json");
        final CountingEnvironmentVariableReader environmentVariableReader =
            new CountingEnvironmentVariableReader("VCAP_SERVICES", vcapServices);

        final SapVcapServicesServiceBindingAccessor sut =
            new SapVcapServicesServiceBindingAccessor(environmentVariableReader);

        // first invocation
        assertThat(sut.getServiceBindings().size()).isEqualTo(3);
        assertThat(environmentVariableReader.getInvocations()).isEqualTo(1);

        // second invocation
        assertThat(sut.getServiceBindings().size()).isEqualTo(3);
        assertThat(environmentVariableReader.getInvocations()).isEqualTo(2);
    }

    private static class CountingEnvironmentVariableReader implements Function<String, String>
    {
        @Nonnull
        private final String expectedKey;

        @Nonnull
        private final String value;

        private int invocations;

        public CountingEnvironmentVariableReader( @Nonnull final String expectedKey, @Nonnull final String value )
        {
            this.expectedKey = expectedKey;
            this.value = value;
        }

        @Override
        public String apply( final String s )
        {
            assertThat(s).isEqualTo(expectedKey);
            invocations++;
            return value;
        }

        public int getInvocations()
        {
            return invocations;
        }
    }
}
