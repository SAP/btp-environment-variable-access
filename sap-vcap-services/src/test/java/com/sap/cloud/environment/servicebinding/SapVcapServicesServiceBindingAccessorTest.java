package com.sap.cloud.environment.servicebinding;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Map;
import java.util.HashMap;
import java.nio.file.Paths;
import java.nio.file.Path;

class SapVcapServicesServiceBindingAccessorTest
{
    @Test
    void parseFullVcapServices()
    {
        final String vcapServices =
            TestResource.read(SapVcapServicesServiceBindingAccessorTest.class, "FullVcapServices.json");

        final SapVcapServicesServiceBindingAccessor sut =
            new SapVcapServicesServiceBindingAccessor(key -> "VCAP_SERVICES".equals(key) ? vcapServices : null);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings.size()).isEqualTo(3);

        assertContainsXsuaaBinding1(serviceBindings);
        assertContainsXsuaaBinding2(serviceBindings);
        assertContainsDestinationBinding1(serviceBindings);
    }

    @Test
    void parseFullVcapServicesFileBased()
    {
        final String vcapServicesFilePath =
            TestResource.getPathAsString(SapVcapServicesServiceBindingAccessorTest.class, "FullVcapServices.json");

        final SapVcapServicesServiceBindingAccessor sut =
            new SapVcapServicesServiceBindingAccessor(
                key -> "VCAP_SERVICES_FILE_PATH".equals(key) ? vcapServicesFilePath : null);

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

        assertThat(binding.getName()).hasValue("xsuaa-binding-1");
        assertThat(binding.getServicePlan()).hasValue("lite");
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

        assertThat(binding.getName()).hasValue("xsuaa-binding-2");
        assertThat(binding.getServicePlan()).hasValue("application");
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

        assertThat(binding.getName()).hasValue("destination-binding-1");
        assertThat(binding.getServicePlan()).hasValue("broker");
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
            new SapVcapServicesServiceBindingAccessor(key -> "VCAP_SERVICES".equals(key) ? vcapServices : null);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings.size()).isEqualTo(1);

        assertContainsXsuaaBinding1(serviceBindings);
    }

    @Test
    void brokenBindingIsIgnoredWhenFileBased()
    {
        final String vcapServicesFilePath =
            TestResource
                .getPathAsString(SapVcapServicesServiceBindingAccessorTest.class, "VcapServicesWithBrokenBinding.json");

        final SapVcapServicesServiceBindingAccessor sut =
            new SapVcapServicesServiceBindingAccessor(
                key -> "VCAP_SERVICES_FILE_PATH".equals(key) ? vcapServicesFilePath : null);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings.size()).isEqualTo(1);

        assertContainsXsuaaBinding1(serviceBindings);
    }

    @Test
    void resultIsNotCached()
    {
        final String vcapServices =
            TestResource.read(SapVcapServicesServiceBindingAccessorTest.class, "FullVcapServices.json");
        final CountingEnvironmentVariableReader environmentVariableReader = new CountingEnvironmentVariableReader();
        environmentVariableReader.addExpectedKeyAndValue("VCAP_SERVICES", vcapServices);
        environmentVariableReader.addExpectedKeyAndValue("VCAP_SERVICES_FILE_PATH", null);

        final SapVcapServicesServiceBindingAccessor sut =
            new SapVcapServicesServiceBindingAccessor(environmentVariableReader);

        // first invocation
        assertThat(sut.getServiceBindings().size()).isEqualTo(3);
        assertThat(environmentVariableReader.getInvocations("VCAP_SERVICES")).isEqualTo(1);
        assertThat(environmentVariableReader.getInvocations("VCAP_SERVICES_FILE_PATH")).isEqualTo(1);

        // second invocation
        assertThat(sut.getServiceBindings().size()).isEqualTo(3);
        assertThat(environmentVariableReader.getInvocations("VCAP_SERVICES")).isEqualTo(2);
        assertThat(environmentVariableReader.getInvocations("VCAP_SERVICES_FILE_PATH")).isEqualTo(2);
    }

    @Test
    void resultIsNotCachedWhenFileBased()
    {
        final String vcapServicesFile =
            TestResource.getPathAsString(SapVcapServicesServiceBindingAccessorTest.class, "FullVcapServices.json");
        final Path vcapServicesFilePath =
            TestResource.get(SapVcapServicesServiceBindingAccessorTest.class, "FullVcapServices.json");
        final String vcapServices =
            TestResource.read(SapVcapServicesServiceBindingAccessorTest.class, "FullVcapServices.json");
        final CountingEnvironmentVariableReader environmentVariableReader = new CountingEnvironmentVariableReader();
        environmentVariableReader.addExpectedKeyAndValue("VCAP_SERVICES", null);
        environmentVariableReader.addExpectedKeyAndValue("VCAP_SERVICES_FILE_PATH", vcapServicesFile);
        final CountingFileReader fileReader = new CountingFileReader(vcapServicesFilePath, vcapServices);

        final SapVcapServicesServiceBindingAccessor sut =
            new SapVcapServicesServiceBindingAccessor(environmentVariableReader, fileReader);

        // first invocation
        assertThat(sut.getServiceBindings().size()).isEqualTo(3);
        assertThat(fileReader.getInvocations()).isEqualTo(1);

        // second invocation
        assertThat(sut.getServiceBindings().size()).isEqualTo(3);
        assertThat(fileReader.getInvocations()).isEqualTo(2);
    }

    @Test
    void ioExceptionIsHandledGracefully()
    {
        final String vcapServicesFilePath =
            TestResource.getPathAsString(SapVcapServicesServiceBindingAccessorTest.class, "non-existent-file.json");

        final SapVcapServicesServiceBindingAccessor sut =
            new SapVcapServicesServiceBindingAccessor(
                key -> "VCAP_SERVICES_FILE_PATH".equals(key) ? vcapServicesFilePath : null);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings).isEmpty();
    }

    private static class CountingEnvironmentVariableReader implements Function<String, String>
    {
        @Nonnull
        private final Map<String, String> expectedKeyToValueMap;

        @Nonnull
        private final Map<String, Integer> expectedKeyInvocations;

        public CountingEnvironmentVariableReader()
        {
            this.expectedKeyToValueMap = new HashMap<>();
            this.expectedKeyInvocations = new HashMap<>();
        }

        public void addExpectedKeyAndValue( @Nonnull final String expectedKey, @Nonnull final String value )
        {
            expectedKeyToValueMap.put(expectedKey, value);
            expectedKeyInvocations.put(expectedKey, 0);
        }

        @Override
        public String apply( final String s )
        {
            assertThat(expectedKeyToValueMap.containsKey(s)).isTrue();
            int invocations = expectedKeyInvocations.get(s) + 1;
            expectedKeyInvocations.put(s, invocations);
            return expectedKeyToValueMap.get(s);
        }

        public int getInvocations( @Nonnull final String expectedKey )
        {
            return expectedKeyInvocations.get(expectedKey);
        }
    }

    private static class CountingFileReader implements Function<Path, String>
    {
        @Nonnull
        private final Path expectedPath;

        @Nonnull
        private final String value;

        private int invocations;

        public CountingFileReader( @Nonnull final Path expectedPath, @Nonnull final String value )
        {
            this.expectedPath = expectedPath;
            this.value = value;
        }

        @Override
        public String apply( final Path path )
        {
            assertThat(path).isEqualTo(expectedPath);
            invocations++;
            return value;
        }

        public int getInvocations()
        {
            return invocations;
        }
    }
}
