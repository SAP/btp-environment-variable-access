/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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

        final SapServiceOperatorServiceBindingIoAccessor sut =
            new SapServiceOperatorServiceBindingIoAccessor(
                reader,
                SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET);

        sut.getServiceBindings();

        verify(reader, times(1)).apply(eq("SERVICE_BINDING_ROOT"));
    }

    @SuppressWarnings( "unchecked" )
    @Test
    void parseMixedServiceBindings()
    {
        final Path rootDirectory =
            TestResource.get(SapServiceOperatorServiceBindingIoAccessorTest.class, "ValidMixedBindings");

        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(eq("SERVICE_BINDING_ROOT"))).thenReturn(rootDirectory.toString());

        final SapServiceOperatorServiceBindingIoAccessor sut =
            new SapServiceOperatorServiceBindingIoAccessor(
                reader,
                SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings.size()).isEqualTo(2);

        assertContainsDataXsuaaBinding(serviceBindings);
        assertContainsSecretKeyXsuaaBinding(serviceBindings);
    }

    @SuppressWarnings( "unchecked" )
    @Test
    void brokenBindingsAreIgnored()
    {
        final Path rootDirectory =
            TestResource.get(SapServiceOperatorServiceBindingIoAccessorTest.class, "PartiallyValidMixedBindings");

        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(eq("SERVICE_BINDING_ROOT"))).thenReturn(rootDirectory.toString());

        final SapServiceOperatorServiceBindingIoAccessor sut =
            new SapServiceOperatorServiceBindingIoAccessor(
                reader,
                SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings.size()).isEqualTo(2);

        assertContainsDataXsuaaBinding(serviceBindings);
        assertContainsSecretKeyXsuaaBinding(serviceBindings);
    }

    @SuppressWarnings( "unchecked" )
    @Test
    void emptyJsonPropertyIsIgnored( @Nonnull @TempDir final Path rootDirectory )
    {
        // setup file system
        final Path bindingRoot = rootDirectory.resolve("binding");
        write(
            bindingRoot.resolve(".metadata"),
            "{\n"
                + "    \"metaDataProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"type\",\n"
                + "            \"format\": \"text\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"empty_text\",\n"
                + "            \"format\": \"text\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"empty_json\",\n"
                + "            \"format\": \"json\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"empty_container\",\n"
                + "            \"format\": \"json\",\n"
                + "            \"container\": true\n"
                + "        }\n"
                + "    ],\n"
                + "    \"credentialProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"token\",\n"
                + "            \"format\": \"text\"\n"
                + "        }\n"
                + "    ]\n"
                + "}");
        write(bindingRoot.resolve("type"), "xsuaa");
        write(bindingRoot.resolve("empty_text"), "");
        write(bindingRoot.resolve("empty_json"), "");
        write(bindingRoot.resolve("empty_container"), "");
        write(bindingRoot.resolve("token"), "auth-token");

        // setup environment variable reader
        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(eq("SERVICE_BINDING_ROOT"))).thenReturn(rootDirectory.toString());

        // setup subject under test
        final SapServiceOperatorServiceBindingIoAccessor sut =
            new SapServiceOperatorServiceBindingIoAccessor(
                reader,
                SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        // assert
        assertThat(serviceBindings.size()).isEqualTo(1);

        final ServiceBinding serviceBinding = serviceBindings.get(0);
        assertThat(serviceBinding.getKeys()).containsExactlyInAnyOrder("type", "empty_text", "credentials");

        assertThat(serviceBinding.getServiceName()).hasValue("xsuaa");
        assertThat(serviceBinding.getCredentials().get("token")).isEqualTo("auth-token");
        assertThat(serviceBinding.get("empty_text")).hasValue("");
        assertThat(serviceBinding.get("empty_json")).isEmpty();
        assertThat(serviceBinding.get("empty_container")).isEmpty();
    }

    @SuppressWarnings( "unchecked" )
    @Test
    void missingPropertyIsIgnored( @Nonnull @TempDir final Path rootDirectory )
    {
        // setup file system
        final Path bindingRoot = rootDirectory.resolve("binding");
        write(
            bindingRoot.resolve(".metadata"),
            "{\n"
                + "    \"metaDataProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"type\",\n"
                + "            \"format\": \"text\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"missing_text\",\n"
                + "            \"format\": \"text\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"missing_json\",\n"
                + "            \"format\": \"json\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"missing_container\",\n"
                + "            \"format\": \"json\",\n"
                + "            \"container\": true\n"
                + "        }\n"
                + "    ],\n"
                + "    \"credentialProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"token\",\n"
                + "            \"format\": \"text\"\n"
                + "        }\n"
                + "    ]\n"
                + "}");
        write(bindingRoot.resolve("type"), "xsuaa");
        write(bindingRoot.resolve("token"), "auth-token");

        assertThat(Files.exists(bindingRoot.resolve("missing_text"))).isFalse();
        assertThat(Files.exists(bindingRoot.resolve("missing_json"))).isFalse();
        assertThat(Files.exists(bindingRoot.resolve("missing_container"))).isFalse();

        // setup environment variable reader
        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(eq("SERVICE_BINDING_ROOT"))).thenReturn(rootDirectory.toString());

        // setup subject under test
        final SapServiceOperatorServiceBindingIoAccessor sut =
            new SapServiceOperatorServiceBindingIoAccessor(
                reader,
                SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        // assert
        assertThat(serviceBindings.size()).isEqualTo(1);

        final ServiceBinding serviceBinding = serviceBindings.get(0);
        assertThat(serviceBinding.getKeys()).containsExactlyInAnyOrder("type", "credentials");

        assertThat(serviceBinding.getServiceName()).hasValue("xsuaa");
        assertThat(serviceBinding.getCredentials().get("token")).isEqualTo("auth-token");
        assertThat(serviceBinding.get("empty_text")).isEmpty();
        assertThat(serviceBinding.get("empty_json")).isEmpty();
        assertThat(serviceBinding.get("empty_container")).isEmpty();
    }

    @SuppressWarnings( "unchecked" )
    @Test
    void invalidContainerIsIgnored( @Nonnull @TempDir final Path rootDirectory )
    {
        // setup file system
        final Path bindingRoot = rootDirectory.resolve("binding");
        write(
            bindingRoot.resolve(".metadata"),
            "{\n"
                + "    \"metaDataProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"type\",\n"
                + "            \"format\": \"text\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"list_container\",\n"
                + "            \"format\": \"json\",\n"
                + "            \"container\": true\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"int_container\",\n"
                + "            \"format\": \"json\",\n"
                + "            \"container\": true\n"
                + "        }\n"
                + "    ],\n"
                + "    \"credentialProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"token\",\n"
                + "            \"format\": \"text\"\n"
                + "        }\n"
                + "    ]\n"
                + "}");
        write(bindingRoot.resolve("type"), "xsuaa");
        write(bindingRoot.resolve("list_container"), "[\"element 1\", \"element 2\"]");
        write(bindingRoot.resolve("int_container"), "1337");
        write(bindingRoot.resolve("token"), "auth-token");

        // setup environment variable reader
        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(eq("SERVICE_BINDING_ROOT"))).thenReturn(rootDirectory.toString());

        // setup subject under test
        final SapServiceOperatorServiceBindingIoAccessor sut =
            new SapServiceOperatorServiceBindingIoAccessor(
                reader,
                SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        // assert
        assertThat(serviceBindings.size()).isEqualTo(1);

        final ServiceBinding serviceBinding = serviceBindings.get(0);
        assertThat(serviceBinding.getKeys()).containsExactlyInAnyOrder("type", "credentials");

        assertThat(serviceBinding.getServiceName()).hasValue("xsuaa");
        assertThat(serviceBinding.getCredentials().get("token")).isEqualTo("auth-token");
        assertThat(serviceBinding.get("list_container")).isEmpty();
        assertThat(serviceBinding.get("int_container")).isEmpty();
    }

    @SuppressWarnings( "unchecked" )
    @Test
    void propertyWithUnknownFormatIsIgnored( @Nonnull @TempDir final Path rootDirectory )
    {
        // setup file system
        final Path bindingRoot = rootDirectory.resolve("binding");
        write(
            bindingRoot.resolve(".metadata"),
            "{\n"
                + "    \"metaDataProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"type\",\n"
                + "            \"format\": \"text\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"unknown_property\",\n"
                + "            \"format\": \"unknown\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"credentialProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"token\",\n"
                + "            \"format\": \"text\"\n"
                + "        }\n"
                + "    ]\n"
                + "}");
        write(bindingRoot.resolve("type"), "xsuaa");
        write(bindingRoot.resolve("unknown_property"), "some value");
        write(bindingRoot.resolve("token"), "auth-token");

        // setup environment variable reader
        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(eq("SERVICE_BINDING_ROOT"))).thenReturn(rootDirectory.toString());

        // setup subject under test
        final SapServiceOperatorServiceBindingIoAccessor sut =
            new SapServiceOperatorServiceBindingIoAccessor(
                reader,
                SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        // assert
        assertThat(serviceBindings.size()).isEqualTo(1);

        final ServiceBinding serviceBinding = serviceBindings.get(0);
        assertThat(serviceBinding.getKeys()).containsExactlyInAnyOrder("type", "credentials");

        assertThat(serviceBinding.getServiceName()).hasValue("xsuaa");
        assertThat(serviceBinding.getCredentials().get("token")).isEqualTo("auth-token");
        assertThat(serviceBinding.get("unknown_property")).isEmpty();
    }

    @SuppressWarnings( "unchecked" )
    @Test
    void existingCredentialsPropertyIsNotOverwritten( @Nonnull @TempDir final Path rootDirectory )
    {
        // setup file system
        final Path bindingRoot = rootDirectory.resolve("binding");
        write(
            bindingRoot.resolve(".metadata"),
            "{\n"
                + "    \"metaDataProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"type\",\n"
                + "            \"format\": \"text\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"name\": \"credentials\",\n"
                + "            \"format\": \"json\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"credentialProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"token\",\n"
                + "            \"format\": \"text\"\n"
                + "        }\n"
                + "    ]\n"
                + "}");
        write(bindingRoot.resolve("type"), "xsuaa");
        write(bindingRoot.resolve("credentials"), "[\"foo\", \"bar\"]");
        write(bindingRoot.resolve("token"), "auth-token");

        // setup environment variable reader
        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(eq("SERVICE_BINDING_ROOT"))).thenReturn(rootDirectory.toString());

        // setup subject under test
        final SapServiceOperatorServiceBindingIoAccessor sut =
            new SapServiceOperatorServiceBindingIoAccessor(
                reader,
                SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        // assert
        assertThat(serviceBindings.size()).isEqualTo(1);

        final ServiceBinding serviceBinding = serviceBindings.get(0);
        assertThat(serviceBinding.getKeys()).hasSize(3);
        assertThat(serviceBinding.getKeys()).contains("type", "credentials"); // the last property key is a UUID that was randomly chosen to avoid a name clash with the existing 'credentials' property

        assertThat(serviceBinding.getServiceName()).hasValue("xsuaa");
        assertThat(serviceBinding.get("credentials")).isPresent().get().asList().containsExactly("foo", "bar");
        assertThat(serviceBinding.getCredentials().get("token")).isEqualTo("auth-token");
    }

    @SuppressWarnings( "unchecked" )
    @Test
    void emptyEnvironmentVariableLeadsToEmptyResult()
    {
        // setup environment variable reader
        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(any())).thenReturn(null);

        // setup subject under test
        final SapServiceOperatorServiceBindingIoAccessor sut =
            new SapServiceOperatorServiceBindingIoAccessor(
                reader,
                SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        // assert
        assertThat(serviceBindings).isNotNull();
        assertThat(serviceBindings).isEmpty();
    }

    @SuppressWarnings( "unchecked" )
    @Test
    void environmentVariablePointingToInvalidDirectoryLeadsToEmptyResult()
    {
        final Path path = Paths.get("this-directory-does-not-exist");
        assertThat(path).doesNotExist();

        // setup environment variable reader
        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(eq("SERVICE_BINDING_ROOT"))).thenReturn(path.toString());

        // setup subject under test
        final SapServiceOperatorServiceBindingIoAccessor sut =
            new SapServiceOperatorServiceBindingIoAccessor(
                reader,
                SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        // assert
        assertThat(serviceBindings).isNotNull();
        assertThat(serviceBindings).isEmpty();
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void fallbackRootPathIsUsedWhenRootPathIsNotDefined( @Nonnull @TempDir final Path rootDirectory )
    {
        // setup file system
        final Path bindingRoot = rootDirectory.resolve("binding");
        write(
            bindingRoot.resolve(".metadata"),
            "{\n"
                + "    \"metaDataProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"type\",\n"
                + "            \"format\": \"text\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"credentialProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"token\",\n"
                + "            \"format\": \"text\"\n"
                + "        }\n"
                + "    ]\n"
                + "}");
        write(bindingRoot.resolve("type"), "xsuaa");
        write(bindingRoot.resolve("token"), "auth-token");

        // setup environment variable reader
        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(eq("SERVICE_BINDING_ROOT"))).thenReturn(null);

        final SapServiceOperatorServiceBindingIoAccessor sut =
            new SapServiceOperatorServiceBindingIoAccessor(
                reader,
                SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET,
                rootDirectory);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        assertThat(serviceBindings.size()).isEqualTo(1);

        final ServiceBinding serviceBinding = serviceBindings.get(0);
        assertThat(serviceBinding.getKeys().size()).isEqualTo(2);
        assertThat(serviceBinding.getKeys()).contains("type");

        assertThat(serviceBinding.getServiceName()).hasValue("xsuaa");
        assertThat(serviceBinding.getCredentials().get("token")).isEqualTo("auth-token");

        verify(reader, times(1)).apply("SERVICE_BINDING_ROOT");
    }

    @Test
    @SuppressWarnings( "unchecked" )
    void fallbackRootPathIsNotUsedWhenRootPathIsDefinedButDoesNotExist( @Nonnull @TempDir final Path rootDirectory )
    {
        // setup file system
        final Path bindingRoot = rootDirectory.resolve("binding");
        write(
            bindingRoot.resolve(".metadata"),
            "{\n"
                + "    \"metaDataProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"type\",\n"
                + "            \"format\": \"text\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"credentialProperties\": [\n"
                + "        {\n"
                + "            \"name\": \"token\",\n"
                + "            \"format\": \"text\"\n"
                + "        }\n"
                + "    ]\n"
                + "}");
        write(bindingRoot.resolve("type"), "xsuaa");
        write(bindingRoot.resolve("token"), "auth-token");

        final Path nonExistingPath = rootDirectory.resolve("this-directory-does-not-exist");
        assertThat(nonExistingPath).doesNotExist();

        // setup environment variable reader
        final Function<String, String> reader = mock(Function.class);
        when(reader.apply(eq("SERVICE_BINDING_ROOT"))).thenReturn(nonExistingPath.toString());

        final SapServiceOperatorServiceBindingIoAccessor sut =
            new SapServiceOperatorServiceBindingIoAccessor(
                reader,
                SapServiceOperatorServiceBindingIoAccessor.DEFAULT_CHARSET,
                rootDirectory);

        final List<ServiceBinding> serviceBindings = sut.getServiceBindings();

        // assert that the fallback path is NOT used even though the SERVICE_BINDING_ROOT does not exist
        assertThat(serviceBindings).isEmpty();
        verify(reader, times(1)).apply("SERVICE_BINDING_ROOT");
    }

    private static void assertContainsDataXsuaaBinding( @Nonnull final List<ServiceBinding> serviceBindings )
    {
        final List<ServiceBinding> dataBinding =
            serviceBindings
                .stream()
                .filter(binding -> "data-xsuaa-binding".equals(binding.getName().orElse(null)))
                .collect(Collectors.toList());
        assertThat(dataBinding.size()).isEqualTo(1);

        final ServiceBinding dataXsuaaBinding = dataBinding.get(0);
        assertThat(dataXsuaaBinding.getName()).hasValue("data-xsuaa-binding");
        assertThat(dataXsuaaBinding.getServiceName()).hasValue("xsuaa");
        assertThat(dataXsuaaBinding.getServicePlan()).hasValue("application");
        assertThat(dataXsuaaBinding.getTags()).containsExactlyInAnyOrder("data-xsuaa-tag-1", "data-xsuaa-tag-2");
        assertThat(dataXsuaaBinding.get("instance_guid")).hasValue("data-xsuaa-instance-guid");
        assertThat(dataXsuaaBinding.get("instance_name")).hasValue("data-xsuaa-instance-name");
        assertThat(dataXsuaaBinding.getCredentials()).isNotEmpty();
        assertThat(dataXsuaaBinding.getCredentials().get("clientid")).isEqualTo("data-xsuaa-clientid");
        assertThat(dataXsuaaBinding.getCredentials().get("clientsecret")).isEqualTo("data-xsuaa-clientsecret");
        assertThat(dataXsuaaBinding.getCredentials().get("domains"))
            .asList()
            .containsExactlyInAnyOrder("data-xsuaa-domain-1", "data-xsuaa-domain-2");
        assertThat(dataXsuaaBinding.getCredentials().get("uaa")).isInstanceOf(Map.class);
    }

    private static void assertContainsSecretKeyXsuaaBinding( @Nonnull final List<ServiceBinding> serviceBindings )
    {
        final List<ServiceBinding> secretKeyXsuaaBindings =
            serviceBindings
                .stream()
                .filter(binding -> "secret-key-xsuaa-binding".equals(binding.getName().orElse(null)))
                .collect(Collectors.toList());

        assertThat(secretKeyXsuaaBindings.size()).isEqualTo(1);

        final ServiceBinding secretKeyBinding = secretKeyXsuaaBindings.get(0);
        assertThat(secretKeyBinding.getName()).hasValue("secret-key-xsuaa-binding");
        assertThat(secretKeyBinding.getServiceName()).hasValue("xsuaa");
        assertThat(secretKeyBinding.getServicePlan()).hasValue("lite");
        assertThat(secretKeyBinding.getTags())
            .containsExactlyInAnyOrder("secret-key-xsuaa-tag-1", "secret-key-xsuaa-tag-2");
        assertThat(secretKeyBinding.get("instance_guid")).hasValue("secret-key-xsuaa-instance-guid");
        assertThat(secretKeyBinding.get("instance_name")).hasValue("secret-key-xsuaa-instance-name");
        assertThat(secretKeyBinding.getCredentials()).isNotEmpty();
        assertThat(secretKeyBinding.getCredentials().get("clientid")).isEqualTo("secret-key-xsuaa-clientid");
        assertThat(secretKeyBinding.getCredentials().get("clientsecret")).isEqualTo("secret-key-xsuaa-clientsecret");
        assertThat(secretKeyBinding.getCredentials().get("url")).isEqualTo("https://secret-key-xsuaa-domain-1.com");
        assertThat(secretKeyBinding.getCredentials().get("zone_uuid")).isEqualTo("secret-key-xsuaa-zone-uuid");
        assertThat(secretKeyBinding.getCredentials().get("domain")).isEqualTo("secret-key-xsuaa-domain-1");
        assertThat(secretKeyBinding.getCredentials().get("domains"))
            .asList()
            .containsExactlyInAnyOrder("secret-key-xsuaa-domain-1");
    }

    private static void write( @Nonnull final Path filePath, @Nonnull final String content )
    {
        try {
            if( !Files.exists(filePath.getParent()) ) {
                Files.createDirectories(filePath.getParent());
            }

            Files.write(filePath, Collections.singletonList(content), StandardCharsets.UTF_8);
        }
        catch( final IOException e ) {
            fail("Failed to write test file content.", e);
        }
    }
}
