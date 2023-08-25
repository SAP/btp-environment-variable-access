/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import com.sap.cloud.environment.servicebinding.api.exception.UnsupportedPropertyTypeException;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings( "unchecked" )
class DefaultServiceBindingTest
{
    private static final byte BYTE = 42;

    private static final int INTEGER = 42;

    private static final long LONG = 42L;

    private static final float FLOAT = 13.37f;

    private static final double DOUBLE = 13.37d;

    private static final BigDecimal BIG_DECIMAL = BigDecimal.valueOf(Long.MAX_VALUE, Integer.MAX_VALUE);

    @Test
    void testInputDataMayContainBoolean()
    {
        final Map<String, Object> input = new HashMap<>();
        input.put("Boolean1", true);
        input.put("Boolean2", false);
        input.put("Booleans", Arrays.asList(true, false));

        final DefaultServiceBinding sut = DefaultServiceBinding.builder().copy(input).build();

        assertThat(sut).isNotNull();
        assertThat(sut.get("Boolean1")).hasValue(true);
        assertThat(sut.get("Boolean2")).hasValue(false);
        assertThat((Iterable<Boolean>) sut.get("Booleans").orElse(null)).containsExactly(true, false);
    }

    @Test
    void testInputDataMayContainNumber()
    {
        final Map<String, Object> input = new HashMap<>();
        input.put("Byte", BYTE);
        input.put("Integer", INTEGER);
        input.put("Long", LONG);
        input.put("Float", FLOAT);
        input.put("Double", DOUBLE);
        input.put("BigDecimal", BIG_DECIMAL);
        input.put("Numbers", Arrays.asList(BYTE, INTEGER, LONG, FLOAT, DOUBLE, BIG_DECIMAL));

        final DefaultServiceBinding sut = DefaultServiceBinding.builder().copy(input).build();

        assertThat(sut).isNotNull();
        assertThat(sut.get("Byte")).hasValue(BYTE);
        assertThat(sut.get("Integer")).hasValue(INTEGER);
        assertThat(sut.get("Long")).hasValue(LONG);
        assertThat(sut.get("Float")).hasValue(FLOAT);
        assertThat(sut.get("Double")).hasValue(DOUBLE);
        assertThat(sut.get("BigDecimal")).hasValue(BIG_DECIMAL);
        assertThat((Iterable<Number>) sut.get("Numbers").orElse(null))
            .containsExactly(BYTE, INTEGER, LONG, FLOAT, DOUBLE, BIG_DECIMAL);
    }

    @Test
    void testInputDataMayContainString()
    {
        final Map<String, Object> input = new HashMap<>();
        input.put("String1", "foo");
        input.put("String2", "");
        input.put("Strings", Arrays.asList("foo", ""));

        final DefaultServiceBinding sut = DefaultServiceBinding.builder().copy(input).build();

        assertThat(sut).isNotNull();
        assertThat(sut.get("String1")).hasValue("foo");
        assertThat(sut.get("String2")).hasValue("");
        assertThat((Iterable<String>) sut.get("Strings").orElse(null)).containsExactly("foo", "");
    }

    @Test
    void testInputDataMayContainContainer()
    {
        final Map<String, Object> input = new HashMap<>();

        final Map<String, Object> map = new HashMap<>();
        map.put("String", "foo");
        map.put("Integer", INTEGER);
        input.put("Map", map);
        input.put("Collection", Arrays.asList("foo", INTEGER));

        final DefaultServiceBinding sut = DefaultServiceBinding.builder().copy(input).build();

        assertThat(sut).isNotNull();

        final Map<String, Object> actualMap = (Map<String, Object>) sut.get("Map").orElse(Collections.emptyMap());
        assertThat(actualMap).isNotNull();
        assertThat(actualMap.get("String")).isEqualTo("foo");
        assertThat(actualMap.get("Integer")).isEqualTo(INTEGER);

        final Collection<Object> actualCollection =
            (Collection<Object>) sut.get("Collection").orElse(Collections.emptyList());
        assertThat(actualCollection).isNotNull();
        assertThat(actualCollection).containsExactly("foo", INTEGER);
    }

    @Test
    void testInputDataMayNotContainMutableObject()
    {
        final Map<String, Object> input = new HashMap<>();
        input.put("StringBuilder", new StringBuilder());

        assertThatThrownBy(() -> DefaultServiceBinding.builder().copy(input))
            .isExactlyInstanceOf(UnsupportedPropertyTypeException.class);
    }

    @Test
    void testDefaultReturnValuesOfConvenientGetters()
    {
        final DefaultServiceBinding sut = DefaultServiceBinding.builder().copy(Collections.emptyMap()).build();

        assertThat(sut).isNotNull();
        assertThat(sut.getName()).isEmpty();
        assertThat(sut.getServiceName()).isEmpty();
        assertThat(sut.getServiceIdentifier()).isEmpty();
        assertThat(sut.getServicePlan()).isEmpty();
        assertThat(sut.getTags()).isEmpty();
        assertThat(sut.getCredentials()).isEmpty();
    }

    @Test
    void testServiceIdentifierIsDerivedFromServiceNameByDefault()
    {
        final DefaultServiceBinding sut =
            DefaultServiceBinding.builder().copy(Collections.emptyMap()).withServiceName("foo").build();

        assertThat(sut.getServiceName()).contains("foo");
        assertThat(sut.getServiceIdentifier()).contains(ServiceIdentifier.of("foo"));
    }

    @Test
    void testServiceIdentifierCanBeOverwritten()
    {
        final DefaultServiceBinding sut =
            DefaultServiceBinding
                .builder()
                .copy(Collections.emptyMap())
                .withServiceName("foo")
                .withServiceIdentifier(ServiceIdentifier.of("bar"))
                .build();

        assertThat(sut.getServiceName()).contains("foo");
        assertThat(sut.getServiceIdentifier()).contains(ServiceIdentifier.of("bar"));
    }

    @Test
    void testEquals()
    {
        final DefaultServiceBinding firstSut = builderWithInitialValues().build();
        final DefaultServiceBinding secondSut = builderWithInitialValues().build();

        assertThat(firstSut).isEqualTo(secondSut).isNotSameAs(secondSut);

        {
            // different properties
            final DefaultServiceBinding sut =
                builderWithInitialValues(Collections.singletonMap("other-properties-key", "value")).build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut);
        }

        {
            // different name
            final DefaultServiceBinding sut = builderWithInitialValues().withName("other-name").build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut);
        }

        {
            // different service name
            final DefaultServiceBinding sut = builderWithInitialValues().withServiceName("other-service-name").build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut);
        }

        {
            // different service identifier
            final DefaultServiceBinding sut =
                builderWithInitialValues()
                    .withServiceIdentifier(ServiceIdentifier.of("other-service-identifier"))
                    .build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut);
        }

        {
            // different service plan
            final DefaultServiceBinding sut = builderWithInitialValues().withServicePlan("other-service-plan").build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut);
        }

        {
            // different tags
            final DefaultServiceBinding sut =
                builderWithInitialValues().withTags(Arrays.asList("other-tag1", "other-tag2")).build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut);
        }

        {
            // different credentials
            final DefaultServiceBinding sut =
                builderWithInitialValues()
                    .withCredentials(Collections.singletonMap("other-credentials-key", "value"))
                    .build();

            assertThat(sut).isNotEqualTo(firstSut).isNotEqualTo(secondSut);
        }
    }

    @Test
    void testHashCode()
    {
        final DefaultServiceBinding firstSut = builderWithInitialValues().build();
        final DefaultServiceBinding secondSut = builderWithInitialValues().build();

        assertThat(firstSut.hashCode()).isEqualTo(secondSut.hashCode());

        {
            // different properties
            final DefaultServiceBinding sut =
                builderWithInitialValues(Collections.singletonMap("other-properties-key", "value")).build();

            assertThat(sut.hashCode()).isNotEqualTo(firstSut.hashCode()).isNotEqualTo(secondSut.hashCode());
        }

        {
            // different name
            final DefaultServiceBinding sut = builderWithInitialValues().withName("other-name").build();

            assertThat(sut.hashCode()).isNotEqualTo(firstSut.hashCode()).isNotEqualTo(secondSut.hashCode());
        }

        {
            // different service name
            final DefaultServiceBinding sut = builderWithInitialValues().withServiceName("other-service-name").build();

            assertThat(sut.hashCode()).isNotEqualTo(firstSut.hashCode()).isNotEqualTo(secondSut.hashCode());
        }

        {
            // different service identifier
            final DefaultServiceBinding sut =
                builderWithInitialValues()
                    .withServiceIdentifier(ServiceIdentifier.of("other-service-identifier"))
                    .build();

            assertThat(sut.hashCode()).isNotEqualTo(firstSut.hashCode()).isNotEqualTo(secondSut.hashCode());
        }

        {
            // different service plan
            final DefaultServiceBinding sut = builderWithInitialValues().withServicePlan("other-service-plan").build();

            assertThat(sut.hashCode()).isNotEqualTo(firstSut.hashCode()).isNotEqualTo(secondSut.hashCode());
        }

        {
            // different tags
            final DefaultServiceBinding sut =
                builderWithInitialValues().withTags(Arrays.asList("other-tag1", "other-tag2")).build();

            assertThat(sut.hashCode()).isNotEqualTo(firstSut.hashCode()).isNotEqualTo(secondSut.hashCode());
        }

        {
            // different credentials
            final DefaultServiceBinding sut =
                builderWithInitialValues()
                    .withCredentials(Collections.singletonMap("other-credentials-key", "value"))
                    .build();

            assertThat(sut.hashCode()).isNotEqualTo(firstSut.hashCode()).isNotEqualTo(secondSut.hashCode());
        }
    }

    @Nonnull
    private static DefaultServiceBinding.TerminalBuilder builderWithInitialValues()
    {
        return builderWithInitialValues(Collections.singletonMap("initial-properties-key", "value"));
    }

    @Nonnull
    private static DefaultServiceBinding.TerminalBuilder builderWithInitialValues(
        @Nonnull final Map<String, Object> properties )
    {
        return DefaultServiceBinding
            .builder()
            .copy(properties)
            .withName("initial-name")
            .withServiceName("initial-service-name")
            .withServiceIdentifier(ServiceIdentifier.of("initial-service-identifier"))
            .withServicePlan("initial-service-plan")
            .withTags(Arrays.asList("initial-tag1", "initial-tag2"))
            .withCredentials(Collections.singletonMap("initial-credentials-key", "value"));
    }
}
