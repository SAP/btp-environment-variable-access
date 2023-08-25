/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DelegatingServiceBindingTest
{
    @Test
    void testGetKeysDelegation()
    {
        final Map<String, Object> delegateProperties = new HashMap<>();
        delegateProperties.put("delegate-key1", "foo");
        delegateProperties.put("delegate-key2", "bar");
        final DefaultServiceBinding delegate = spy(DefaultServiceBinding.builder().copy(delegateProperties).build());

        final Map<String, Object> overwrittenProperties = new HashMap<>();
        overwrittenProperties.put("overwritten-key1", "baz");
        overwrittenProperties.put("overwritten-key2", "qux");

        final ServiceBinding sutWithoutProperties =
            DelegatingServiceBinding.builder(delegate).withProperties(null).build();
        final ServiceBinding sutWithProperties =
            DelegatingServiceBinding.builder(delegate).withProperties(overwrittenProperties).build();
        final ServiceBinding sutWithEmptyProperties =
            DelegatingServiceBinding.builder(delegate).withProperties(new HashMap<>()).build();
        final ServiceBinding sutThatDelegates = DelegatingServiceBinding.builder(delegate).build();

        assertThat(delegate.getKeys()).containsExactlyInAnyOrderElementsOf(delegateProperties.keySet());
        verify(delegate, times(1)).getKeys();

        assertThat(sutWithoutProperties.getKeys()).isEmpty();
        assertThat(sutWithProperties.getKeys()).containsExactlyInAnyOrderElementsOf(overwrittenProperties.keySet());
        assertThat(sutWithEmptyProperties.getKeys()).isEmpty();
        assertThat(sutThatDelegates.getKeys()).containsExactlyInAnyOrderElementsOf(delegateProperties.keySet());
        verify(delegate, times(2)).getKeys();
    }

    @Test
    void testWithPropertiesCreatesDeepCopy()
    {
        final Map<String, Object> passedProperties = new HashMap<>();
        passedProperties.put("string", "string");

        final List<Object> nestedList = new ArrayList<>();
        nestedList.add("item-1");
        nestedList.add(Arrays.asList("subitem-1", "subitem-2"));
        nestedList.add(Collections.singletonMap("key", "value"));
        passedProperties.put("list", nestedList);

        final Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("string", "string");
        nestedMap.put("list", Arrays.asList("item-1", "item-2"));
        nestedMap.put("map", Collections.singletonMap("key", "value"));
        passedProperties.put("map", nestedMap);

        final ServiceBinding sut =
            DelegatingServiceBinding.builder(mock(ServiceBinding.class)).withProperties(passedProperties).build();

        assertThat(sut.get("string")).contains("string");
        assertThat(sut.get("list"))
            .isNotEmpty()
            .get()
            .asList()
            .containsExactly(
                "item-1",
                Arrays.asList("subitem-1", "subitem-2"),
                Collections.singletonMap("key", "value"));
        assertThat(sut.get("map"))
            .isNotEmpty()
            .get()
            .asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
            .containsEntry("string", "string")
            .containsEntry("list", Arrays.asList("item-1", "item-2"))
            .containsEntry("map", Collections.singletonMap("key", "value"));

        // modify the original map
        passedProperties.put("string", "modified-string");
        passedProperties.remove("list");
        nestedMap.remove("map");

        assertThat(sut.get("string")).contains("string");
        assertThat(sut.get("list"))
            .isNotEmpty()
            .get()
            .asList()
            .containsExactly(
                "item-1",
                Arrays.asList("subitem-1", "subitem-2"),
                Collections.singletonMap("key", "value"));
        assertThat(sut.get("map"))
            .isNotEmpty()
            .get()
            .asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
            .containsEntry("string", "string")
            .containsEntry("list", Arrays.asList("item-1", "item-2"))
            .containsEntry("map", Collections.singletonMap("key", "value"));
    }

    @Test
    void testContainsKeyCallsGetKeys()
    {
        final ServiceBinding delegate = mock(ServiceBinding.class);
        final ServiceBinding sut = DelegatingServiceBinding.builder(delegate).build();

        assertThat(sut).isInstanceOf(DelegatingServiceBinding.class);

        final ServiceBinding spy = spy(sut);
        when(spy.getKeys()).thenReturn(Collections.emptySet());

        assertThat(spy.containsKey("foo")).isFalse();
        verify(spy, times(1)).getKeys();
    }

    @Test
    void testGetDelegation()
    {
        final Map<String, Object> delegateProperties = new HashMap<>();
        delegateProperties.put("delegate-key", "foo");
        final DefaultServiceBinding delegate = spy(DefaultServiceBinding.builder().copy(delegateProperties).build());

        final Map<String, Object> overwrittenProperties = new HashMap<>();
        overwrittenProperties.put("overwritten-key", "bar");

        final ServiceBinding sutWithoutProperties =
            DelegatingServiceBinding.builder(delegate).withProperties(null).build();
        final ServiceBinding sutWithProperties =
            DelegatingServiceBinding.builder(delegate).withProperties(overwrittenProperties).build();
        final ServiceBinding sutWithEmptyProperties =
            DelegatingServiceBinding.builder(delegate).withProperties(new HashMap<>()).build();
        final ServiceBinding sutThatDelegates = DelegatingServiceBinding.builder(delegate).build();

        assertThat(delegate.get("delegate-key")).contains("foo");
        assertThat(delegate.get("overwritten-key")).isEmpty();
        verify(delegate, times(1)).get(eq("delegate-key"));
        verify(delegate, times(1)).get(eq("overwritten-key"));

        assertThat(sutWithoutProperties.get("delegate-key")).isEmpty();
        assertThat(sutWithoutProperties.get("overwritten-key")).isEmpty();

        assertThat(sutWithProperties.get("delegate-key")).isEmpty();
        assertThat(sutWithProperties.get("overwritten-key")).contains("bar");

        assertThat(sutWithEmptyProperties.get("delegate-key")).isEmpty();
        assertThat(sutWithEmptyProperties.get("overwritten-key")).isEmpty();

        assertThat(sutThatDelegates.get("delegate-key")).contains("foo");
        assertThat(sutThatDelegates.get("overwritten-key")).isEmpty();
        verify(delegate, times(2)).get(eq("delegate-key"));
        verify(delegate, times(2)).get(eq("overwritten-key"));
    }

    @Test
    void testGetNameDelegation()
    {
        final ServiceBinding delegate = mock(ServiceBinding.class);
        when(delegate.getName()).thenReturn(Optional.of("delegate-name"));

        final ServiceBinding sutWithoutName = DelegatingServiceBinding.builder(delegate).withName(null).build();
        final ServiceBinding sutWithName =
            DelegatingServiceBinding.builder(delegate).withName("overwritten-name").build();
        final ServiceBinding sutThatDelegates = DelegatingServiceBinding.builder(delegate).build();

        assertThat(delegate.getName()).contains("delegate-name");
        verify(delegate, times(1)).getName();

        assertThat(sutWithoutName.getName()).isEmpty();
        assertThat(sutWithName.getName()).contains("overwritten-name");
        assertThat(sutThatDelegates.getName()).contains("delegate-name");
        verify(delegate, times(2)).getName();
    }

    @Test
    void testGetServiceNameDelegation()
    {
        final ServiceBinding delegate = mock(ServiceBinding.class);
        when(delegate.getServiceName()).thenReturn(Optional.of("delegate-service-name"));

        final ServiceBinding sutWithoutServiceName =
            DelegatingServiceBinding.builder(delegate).withServiceName(null).build();
        final ServiceBinding sutWithServiceName =
            DelegatingServiceBinding.builder(delegate).withServiceName("overwritten-service-name").build();
        final ServiceBinding sutThatDelegates = DelegatingServiceBinding.builder(delegate).build();

        assertThat(delegate.getServiceName()).contains("delegate-service-name");
        verify(delegate, times(1)).getServiceName();

        assertThat(sutWithoutServiceName.getServiceName()).isEmpty();
        assertThat(sutWithServiceName.getServiceName()).contains("overwritten-service-name");
        assertThat(sutThatDelegates.getServiceName()).contains("delegate-service-name");
        verify(delegate, times(2)).getServiceName();
    }

    @Test
    void testGetServiceIdentifierDelegation()
    {
        final ServiceBinding delegate = mock(ServiceBinding.class);
        when(delegate.getServiceIdentifier())
            .thenReturn(Optional.of(ServiceIdentifier.of("delegate-service-identifier")));

        final ServiceBinding sutWithoutServiceIdentifier =
            DelegatingServiceBinding.builder(delegate).withServiceIdentifier(null).build();
        final ServiceBinding sutWithServiceIdentifier =
            DelegatingServiceBinding
                .builder(delegate)
                .withServiceIdentifier(ServiceIdentifier.of("overwritten-service-identifier"))
                .build();
        final ServiceBinding sutThatDelegates = DelegatingServiceBinding.builder(delegate).build();

        assertThat(delegate.getServiceIdentifier()).contains(ServiceIdentifier.of("delegate-service-identifier"));
        verify(delegate, times(1)).getServiceIdentifier();

        assertThat(sutWithoutServiceIdentifier.getServiceIdentifier()).isEmpty();
        assertThat(sutWithServiceIdentifier.getServiceIdentifier())
            .contains(ServiceIdentifier.of("overwritten-service-identifier"));
        assertThat(sutThatDelegates.getServiceIdentifier())
            .contains(ServiceIdentifier.of("delegate-service-identifier"));
        verify(delegate, times(2)).getServiceIdentifier();
    }

    @Test
    void testGetServicePlanDelegation()
    {
        final ServiceBinding delegate = mock(ServiceBinding.class);
        when(delegate.getServicePlan()).thenReturn(Optional.of("delegate-service-plan"));

        final ServiceBinding sutWithoutServicePlan =
            DelegatingServiceBinding.builder(delegate).withServicePlan(null).build();
        final ServiceBinding sutWithServicePlan =
            DelegatingServiceBinding.builder(delegate).withServicePlan("overwritten-service-plan").build();
        final ServiceBinding sutThatDelegates = DelegatingServiceBinding.builder(delegate).build();

        assertThat(delegate.getServicePlan()).contains("delegate-service-plan");
        verify(delegate, times(1)).getServicePlan();

        assertThat(sutWithoutServicePlan.getServicePlan()).isEmpty();
        assertThat(sutWithServicePlan.getServicePlan()).contains("overwritten-service-plan");
        assertThat(sutThatDelegates.getServicePlan()).contains("delegate-service-plan");
        verify(delegate, times(2)).getServicePlan();
    }

    @Test
    void testGetTagsDelegation()
    {
        final ServiceBinding delegate = mock(ServiceBinding.class);
        when(delegate.getTags()).thenReturn(Arrays.asList("delegate-tag-1", "delegate-tag-2"));

        final ServiceBinding sutWithoutTags = DelegatingServiceBinding.builder(delegate).withTags(null).build();
        final ServiceBinding sutWithTags =
            DelegatingServiceBinding
                .builder(delegate)
                .withTags(Arrays.asList("overwritten-tag-1", "overwritten-tag-2"))
                .build();
        final ServiceBinding sutWithEmptyTags =
            DelegatingServiceBinding.builder(delegate).withTags(new ArrayList<>()).build();
        final ServiceBinding sutThatDelegates = DelegatingServiceBinding.builder(delegate).build();

        assertThat(delegate.getTags()).containsExactlyInAnyOrder("delegate-tag-1", "delegate-tag-2");
        verify(delegate, times(1)).getTags();

        assertThat(sutWithoutTags.getTags()).isEmpty();
        assertThat(sutWithTags.getTags()).containsExactlyInAnyOrder("overwritten-tag-1", "overwritten-tag-2");
        assertThat(sutWithEmptyTags.getTags()).isEmpty();
        assertThat(sutThatDelegates.getTags()).containsExactlyInAnyOrder("delegate-tag-1", "delegate-tag-2");
        verify(delegate, times(2)).getTags();
    }

    @Test
    void testWithTagsCreatesDeepCopy()
    {
        final List<String> passedTags = new ArrayList<>();
        passedTags.add("tag-1");
        passedTags.add("tag-2");

        final ServiceBinding sut =
            DelegatingServiceBinding.builder(mock(ServiceBinding.class)).withTags(passedTags).build();

        assertThat(sut.getTags()).containsExactlyInAnyOrder("tag-1", "tag-2");

        // modify the original list
        passedTags.add("tag-3");
        passedTags.remove("tag-1");

        assertThat(sut.getTags()).containsExactlyInAnyOrder("tag-1", "tag-2");
    }

    @Test
    void testGetCredentialsDelegation()
    {
        final Map<String, Object> delegateCredentials = new HashMap<>();
        delegateCredentials.put("delegate-key1", "foo");
        delegateCredentials.put("delegate-key2", "bar");
        final ServiceBinding delegate =
            spy(
                DefaultServiceBinding
                    .builder()
                    .copy(Collections.emptyMap())
                    .withCredentials(delegateCredentials)
                    .build());

        final Map<String, Object> overwrittenCredentials = new HashMap<>();
        overwrittenCredentials.put("overwritten-key1", "baz");
        overwrittenCredentials.put("overwritten-key2", "qux");

        final ServiceBinding sutWithoutCredentials =
            DelegatingServiceBinding.builder(delegate).withCredentials(null).build();
        final ServiceBinding sutWithCredentials =
            DelegatingServiceBinding.builder(delegate).withCredentials(overwrittenCredentials).build();
        final ServiceBinding sutWithEmptyCredentials =
            DelegatingServiceBinding.builder(delegate).withCredentials(new HashMap<>()).build();
        final ServiceBinding sutThatDelegates = DelegatingServiceBinding.builder(delegate).build();

        assertThat(delegate.getCredentials()).containsExactlyInAnyOrderEntriesOf(delegateCredentials);
        verify(delegate, times(1)).getCredentials();

        assertThat(sutWithoutCredentials.getCredentials()).isEmpty();
        assertThat(sutWithCredentials.getCredentials()).containsExactlyInAnyOrderEntriesOf(overwrittenCredentials);
        assertThat(sutWithEmptyCredentials.getCredentials()).isEmpty();
        assertThat(sutThatDelegates.getCredentials()).containsExactlyInAnyOrderEntriesOf(delegateCredentials);
        verify(delegate, times(2)).getCredentials();
    }

    @Test
    void testWithCredentialsCreatesDeepCopy()
    {
        final Map<String, Object> passedCredentials = new HashMap<>();
        passedCredentials.put("string", "string");

        final List<Object> nestedList = new ArrayList<>();
        nestedList.add("item-1");
        nestedList.add(Arrays.asList("subitem-1", "subitem-2"));
        nestedList.add(Collections.singletonMap("key", "value"));
        passedCredentials.put("list", nestedList);

        final Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("string", "string");
        nestedMap.put("list", Arrays.asList("item-1", "item-2"));
        nestedMap.put("map", Collections.singletonMap("key", "value"));
        passedCredentials.put("map", nestedMap);

        final ServiceBinding sut =
            DelegatingServiceBinding.builder(mock(ServiceBinding.class)).withCredentials(passedCredentials).build();

        assertThat(sut.getCredentials().get("string")).isEqualTo("string");
        assertThat(sut.getCredentials().get("list"))
            .asList()
            .isNotEmpty()
            .containsExactly(
                "item-1",
                Arrays.asList("subitem-1", "subitem-2"),
                Collections.singletonMap("key", "value"));
        assertThat(sut.getCredentials().get("map"))
            .asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
            .containsEntry("string", "string")
            .containsEntry("list", Arrays.asList("item-1", "item-2"))
            .containsEntry("map", Collections.singletonMap("key", "value"));

        // modify the original map
        passedCredentials.put("string", "modified-string");
        passedCredentials.remove("list");
        nestedMap.remove("map");

        assertThat(sut.getCredentials().get("string")).isEqualTo("string");
        assertThat(sut.getCredentials().get("list"))
            .asList()
            .containsExactly(
                "item-1",
                Arrays.asList("subitem-1", "subitem-2"),
                Collections.singletonMap("key", "value"));
        assertThat(sut.getCredentials().get("map"))
            .asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
            .containsEntry("string", "string")
            .containsEntry("list", Arrays.asList("item-1", "item-2"))
            .containsEntry("map", Collections.singletonMap("key", "value"));
    }

    @Test
    void testEquals()
    {
        final DefaultServiceBinding delegate =
            DefaultServiceBinding
                .builder()
                .copy(Collections.singletonMap("delegate-properties-key", "value"))
                .withName("delegate-name")
                .withServiceName("delegate-service-name")
                .withServiceIdentifier(ServiceIdentifier.of("delegate-service-identifier"))
                .withServicePlan("delegate-service-plan")
                .withTags(Arrays.asList("delegate-tag1", "delegate-tag2"))
                .withCredentials(Collections.singletonMap("delegate-credentials-key", "value"))
                .build();

        final ServiceBinding firstSut = DelegatingServiceBinding.builder(delegate).build();
        final ServiceBinding secondSut = DelegatingServiceBinding.builder(delegate).build();

        assertThat(firstSut).isEqualTo(firstSut).isEqualTo(secondSut).isNotSameAs(secondSut).isNotEqualTo(delegate);

        {
            // overwrite properties
            final ServiceBinding sut =
                DelegatingServiceBinding
                    .builder(delegate)
                    .withProperties(Collections.singletonMap("overwritten-properties-key", "value"))
                    .build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut).isNotEqualTo(delegate);
        }

        {
            // overwrite name
            final ServiceBinding sut = DelegatingServiceBinding.builder(delegate).withName("overwritten-name").build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut).isNotEqualTo(delegate);
        }

        {
            // overwrite service name
            final ServiceBinding sut =
                DelegatingServiceBinding.builder(delegate).withServiceName("overwritten-service-name").build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut).isNotEqualTo(delegate);
        }

        {
            // overwrite service identifier
            final ServiceBinding sut =
                DelegatingServiceBinding
                    .builder(delegate)
                    .withServiceIdentifier(ServiceIdentifier.of("overwritten-service-identifier"))
                    .build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut).isNotEqualTo(delegate);
        }

        {
            // overwrite service plan
            final ServiceBinding sut =
                DelegatingServiceBinding.builder(delegate).withServicePlan("overwritten-service-plan").build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut).isNotEqualTo(delegate);
        }

        {
            // overwrite tags
            final ServiceBinding sut =
                DelegatingServiceBinding
                    .builder(delegate)
                    .withTags(Arrays.asList("overwritten-tag1", "overwritten-tag2"))
                    .build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut).isNotEqualTo(delegate);
        }

        {
            // overwrite credentials
            final ServiceBinding sut =
                DelegatingServiceBinding
                    .builder(delegate)
                    .withCredentials(Collections.singletonMap("overwritten-credentials-key", "value"))
                    .build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut).isNotEqualTo(delegate);
        }
    }

    @Test
    void testHashCode()
    {
        final DefaultServiceBinding delegate =
            DefaultServiceBinding
                .builder()
                .copy(Collections.singletonMap("delegate-properties-key", "value"))
                .withName("delegate-name")
                .withServiceName("delegate-service-name")
                .withServiceIdentifier(ServiceIdentifier.of("delegate-service-identifier"))
                .withServicePlan("delegate-service-plan")
                .withTags(Arrays.asList("delegate-tag1", "delegate-tag2"))
                .withCredentials(Collections.singletonMap("delegate-credentials-key", "value"))
                .build();

        final ServiceBinding firstSut = DelegatingServiceBinding.builder(delegate).build();
        final ServiceBinding secondSut = DelegatingServiceBinding.builder(delegate).build();

        // the delegate hash code is different because the `properties` are a private field that
        // are not accessible from within the `DelegatingServiceBinding` even though the implementation can
        // (implicitly) delegate to them
        assertThat(firstSut.hashCode()).isEqualTo(secondSut.hashCode()).isNotSameAs(delegate.hashCode());

        {
            // overwrite properties
            final ServiceBinding sutWithSameProperties =
                DelegatingServiceBinding
                    .builder(delegate)
                    .withProperties(Collections.singletonMap("delegate-properties-key", "value"))
                    .build();
            final ServiceBinding sutWithDifferentProperties =
                DelegatingServiceBinding
                    .builder(delegate)
                    .withProperties(Collections.singletonMap("overwritten-properties-key", "value"))
                    .build();

            assertThat(sutWithSameProperties.hashCode())
                .isNotEqualTo(sutWithDifferentProperties.hashCode())
                .isNotEqualTo(firstSut.hashCode())
                .isNotEqualTo(secondSut.hashCode())
                // we CAN produce a hash collision by using the exact same property values as the delegate contains
                .isEqualTo(delegate.hashCode());

            assertThat(sutWithDifferentProperties.hashCode())
                .isNotEqualTo(sutWithSameProperties.hashCode())
                .isNotEqualTo(firstSut.hashCode())
                .isNotEqualTo(secondSut.hashCode())
                .isNotEqualTo(delegate.hashCode());
        }

        {
            // overwrite name
            final ServiceBinding sut = DelegatingServiceBinding.builder(delegate).withName("overwritten-name").build();

            assertThat(sut.hashCode())
                .isNotEqualTo(firstSut.hashCode())
                .isNotEqualTo(secondSut.hashCode())
                .isNotEqualTo(delegate.hashCode());
        }

        {
            // overwrite service name
            final ServiceBinding sut =
                DelegatingServiceBinding.builder(delegate).withServiceName("overwritten-service-name").build();

            assertThat(sut.hashCode())
                .isNotEqualTo(firstSut.hashCode())
                .isNotEqualTo(secondSut.hashCode())
                .isNotEqualTo(delegate.hashCode());
        }

        {
            // overwrite service identifier
            final ServiceBinding sut =
                DelegatingServiceBinding
                    .builder(delegate)
                    .withServiceIdentifier(ServiceIdentifier.of("overwritten-service-identifier"))
                    .build();

            assertThat(sut.hashCode())
                .isNotEqualTo(firstSut.hashCode())
                .isNotEqualTo(secondSut.hashCode())
                .isNotEqualTo(delegate.hashCode());
        }

        {
            // overwrite service plan
            final ServiceBinding sut =
                DelegatingServiceBinding.builder(delegate).withServicePlan("overwritten-service-plan").build();

            assertThat(sut.hashCode())
                .isNotEqualTo(firstSut.hashCode())
                .isNotEqualTo(secondSut.hashCode())
                .isNotEqualTo(delegate.hashCode());
        }

        {
            // overwrite tags
            final ServiceBinding sut =
                DelegatingServiceBinding
                    .builder(delegate)
                    .withTags(Arrays.asList("overwritten-tag1", "overwritten-tag2"))
                    .build();

            assertThat(sut.hashCode())
                .isNotEqualTo(firstSut.hashCode())
                .isNotEqualTo(secondSut.hashCode())
                .isNotEqualTo(delegate.hashCode());
        }

        {
            // overwrite credentials
            final ServiceBinding sut =
                DelegatingServiceBinding
                    .builder(delegate)
                    .withCredentials(Collections.singletonMap("overwritten-credentials-key", "value"))
                    .build();

            assertThat(sut.hashCode())
                .isNotEqualTo(firstSut.hashCode())
                .isNotEqualTo(secondSut.hashCode())
                .isNotEqualTo(delegate.hashCode());
        }
    }
}
