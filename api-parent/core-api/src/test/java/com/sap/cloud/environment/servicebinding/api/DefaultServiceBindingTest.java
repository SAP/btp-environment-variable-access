/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sap.cloud.environment.servicebinding.api.exception.UnsupportedPropertyTypeException;

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
        assertThat(sut.get("Boolean1").orElse(null)).isEqualTo(true);
        assertThat(sut.get("Boolean2").orElse(null)).isEqualTo(false);
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
        assertThat(sut.get("Byte").orElse(null)).isEqualTo(BYTE);
        assertThat(sut.get("Integer").orElse(null)).isEqualTo(INTEGER);
        assertThat(sut.get("Long").orElse(null)).isEqualTo(LONG);
        assertThat(sut.get("Float").orElse(null)).isEqualTo(FLOAT);
        assertThat(sut.get("Double").orElse(null)).isEqualTo(DOUBLE);
        assertThat(sut.get("BigDecimal").orElse(null)).isEqualTo(BIG_DECIMAL);
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
        assertThat(sut.get("String1").orElse(null)).isEqualTo("foo");
        assertThat(sut.get("String2").orElse(null)).isEqualTo("");
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
        assertThat(sut.getName().isPresent()).isFalse();
        assertThat(sut.getServiceName().isPresent()).isFalse();
        assertThat(sut.getServicePlan().isPresent()).isFalse();
        assertThat(sut.getTags()).isEmpty();
        assertThat(sut.getCredentials()).isEmpty();
    }
}
