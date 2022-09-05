/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sap.cloud.environment.servicebinding.api.exception.ValueCastException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TypedMapViewTest
{
    private static final Map<String, Object> PRIMITIVE_VALUES = new HashMap<>();

    private static final Collection<Method> TYPED_ACCESSORS = new ArrayList<>();

    private static final int INTEGER = 42;

    private static final double DOUBLE = 13.37d;

    private static final BigDecimal BIG_DECIMAL = BigDecimal.valueOf(Long.MAX_VALUE, Integer.MAX_VALUE);

    @BeforeAll
    static void beforeAll()
        throws NoSuchMethodException
    {
        PRIMITIVE_VALUES.put("Boolean", true);
        PRIMITIVE_VALUES.put("Integer", INTEGER);
        PRIMITIVE_VALUES.put("Double", DOUBLE);
        PRIMITIVE_VALUES.put("Number", BIG_DECIMAL);
        PRIMITIVE_VALUES.put("String", "Value");
        PRIMITIVE_VALUES.put("Object", null);

        TYPED_ACCESSORS.add(TypedMapView.class.getDeclaredMethod("getBoolean", String.class));
        TYPED_ACCESSORS.add(TypedMapView.class.getDeclaredMethod("getInteger", String.class));
        TYPED_ACCESSORS.add(TypedMapView.class.getDeclaredMethod("getDouble", String.class));
        TYPED_ACCESSORS.add(TypedMapView.class.getDeclaredMethod("getNumber", String.class));
        TYPED_ACCESSORS.add(TypedMapView.class.getDeclaredMethod("getString", String.class));
        TYPED_ACCESSORS.add(TypedMapView.class.getDeclaredMethod("getListView", String.class));
        TYPED_ACCESSORS.add(TypedMapView.class.getDeclaredMethod("getMapView", String.class));
    }

    @Test
    void create()
    {
        final Map<String, Object> map = new HashMap<>(PRIMITIVE_VALUES);
        map.put("MapView", mock(TypedMapView.class));
        map.put("ListView", mock(TypedListView.class));

        final TypedMapView sut = TypedMapView.fromMap(map);

        assertThat(sut).isNotNull();

        assertThat(sut.getKeys()).containsExactlyInAnyOrderElementsOf(map.keySet());

        assertThat(sut.get("Object")).isNull();
        assertThat(sut.getBoolean("Boolean")).isTrue();
        assertThat(sut.getInteger("Integer")).isEqualTo(INTEGER);
        assertThat(sut.getDouble("Double")).isEqualTo(DOUBLE);
        assertThat(sut.getNumber("Number")).isEqualTo(BigDecimal.valueOf(Long.MAX_VALUE, Integer.MAX_VALUE));
        assertThat(sut.getString("String")).isEqualTo("Value");
        assertThat(sut.getMapView("MapView")).isNotNull();
        assertThat(sut.getListView("ListView")).isNotNull();
    }

    @Test
    void createTransformsNestedMapToMapView()
    {
        final TypedMapView sut = TypedMapView.fromMap(Collections.singletonMap("Map", PRIMITIVE_VALUES));

        assertThat(sut.getMapView("Map")).isNotNull();
        assertThat(sut.getMapView("Map").getKeys()).containsExactlyInAnyOrderElementsOf(PRIMITIVE_VALUES.keySet());
    }

    @Test
    void createTransformsNestedListToListView()
    {
        final TypedMapView sut = TypedMapView.fromMap(Collections.singletonMap("List", Arrays.asList("a", "b", "c")));

        assertThat(sut.getListView("List")).isNotNull();
        assertThat(sut.getListView("List").getSize()).isEqualTo(3);
    }

    @Test
    void getBoolean()
        throws NoSuchMethodException
    {
        final TypedMapView sut = TypedMapView.fromMap(Collections.singletonMap("Key", true));

        expectValueCastExceptionForAllBut(sut, "Key", TypedMapView.class.getDeclaredMethod("getBoolean", String.class));
    }

    @Test
    void getMaybeBoolean()
    {
        final Map<String, Object> map = new HashMap<>();
        map.put("valid", true);
        map.put("invalid", "value");

        final TypedMapView sut = TypedMapView.fromMap(map);

        assertThat(sut.getMaybeBoolean("valid")).isPresent();
        assertThat(sut.getMaybeBoolean("invalid")).isEmpty(); // cast error
        assertThat(sut.getMaybeBoolean("foo")).isEmpty(); // key does not exist
    }

    @Test
    void getInteger()
        throws NoSuchMethodException
    {
        final TypedMapView sut = TypedMapView.fromMap(Collections.singletonMap("Key", INTEGER));

        expectValueCastExceptionForAllBut(
            sut,
            "Key",
            TypedMapView.class.getDeclaredMethod("getInteger", String.class),
            TypedMapView.class.getDeclaredMethod("getDouble", String.class),
            TypedMapView.class.getDeclaredMethod("getNumber", String.class));
    }

    @Test
    void getMaybeInteger()
    {
        final Map<String, Object> map = new HashMap<>();
        map.put("valid", INTEGER);
        map.put("invalid", "value");

        final TypedMapView sut = TypedMapView.fromMap(map);

        assertThat(sut.maybeInteger("valid")).isPresent();
        assertThat(sut.maybeInteger("invalid")).isEmpty(); // cast error
        assertThat(sut.maybeInteger("foo")).isEmpty(); // key does not exist
    }

    @Test
    void getDouble()
        throws NoSuchMethodException
    {
        final TypedMapView sut = TypedMapView.fromMap(Collections.singletonMap("Key", DOUBLE));

        expectValueCastExceptionForAllBut(
            sut,
            "Key",
            TypedMapView.class.getDeclaredMethod("getInteger", String.class),
            TypedMapView.class.getDeclaredMethod("getDouble", String.class),
            TypedMapView.class.getDeclaredMethod("getNumber", String.class));
    }

    @Test
    void getMaybeDouble()
    {
        final Map<String, Object> map = new HashMap<>();
        map.put("valid", DOUBLE);
        map.put("invalid", "value");

        final TypedMapView sut = TypedMapView.fromMap(map);

        assertThat(sut.getMaybeDouble("valid")).isPresent();
        assertThat(sut.getMaybeDouble("invalid")).isEmpty(); // cast error
        assertThat(sut.getMaybeDouble("foo")).isEmpty(); // key does not exist
    }

    @Test
    void getNumber()
        throws NoSuchMethodException
    {
        final TypedMapView sut =
            TypedMapView
                .fromMap(Collections.singletonMap("Key", BigDecimal.valueOf(Long.MAX_VALUE, Integer.MAX_VALUE)));

        expectValueCastExceptionForAllBut(
            sut,
            "Key",
            TypedMapView.class.getDeclaredMethod("getNumber", String.class),
            TypedMapView.class.getDeclaredMethod("getInteger", String.class),
            TypedMapView.class.getDeclaredMethod("getDouble", String.class));
    }

    @Test
    void getMaybeNumber()
    {
        final Map<String, Object> map = new HashMap<>();
        map.put("valid", BIG_DECIMAL);
        map.put("invalid", "value");

        final TypedMapView sut = TypedMapView.fromMap(map);

        assertThat(sut.getMaybeNumber("valid")).isPresent();
        assertThat(sut.getMaybeNumber("invalid")).isEmpty(); // cast error
        assertThat(sut.getMaybeNumber("foo")).isEmpty(); // key does not exist
    }

    @Test
    void getString()
        throws NoSuchMethodException
    {
        final TypedMapView sut = TypedMapView.fromMap(Collections.singletonMap("Key", "Value"));

        expectValueCastExceptionForAllBut(sut, "Key", TypedMapView.class.getDeclaredMethod("getString", String.class));
    }

    @Test
    void getMaybeString()
    {
        final Map<String, Object> map = new HashMap<>();
        map.put("valid", "string");
        map.put("invalid", BIG_DECIMAL);

        final TypedMapView sut = TypedMapView.fromMap(map);

        assertThat(sut.getMaybeString("valid")).isPresent();
        assertThat(sut.getMaybeString("invalid")).isEmpty(); // cast error
        assertThat(sut.getMaybeString("foo")).isEmpty(); // key does not exist
    }

    @Test
    void getMapView()
        throws NoSuchMethodException
    {
        final TypedMapView sut = TypedMapView.fromMap(Collections.singletonMap("Key", mock(TypedMapView.class)));

        expectValueCastExceptionForAllBut(sut, "Key", TypedMapView.class.getDeclaredMethod("getMapView", String.class));
    }

    @Test
    void getMaybeMapView()
    {
        final Map<String, Object> map = new HashMap<>();
        map.put("valid", mock(TypedMapView.class));
        map.put("invalid", "value");

        final TypedMapView sut = TypedMapView.fromMap(map);

        assertThat(sut.getMaybeMapView("valid")).isPresent();
        assertThat(sut.getMaybeMapView("invalid")).isEmpty(); // cast error
        assertThat(sut.getMaybeMapView("foo")).isEmpty(); // key does not exist
    }

    @Test
    void getListView()
        throws NoSuchMethodException
    {
        final TypedMapView sut = TypedMapView.fromMap(Collections.singletonMap("Key", mock(TypedListView.class)));

        expectValueCastExceptionForAllBut(
            sut,
            "Key",
            TypedMapView.class.getDeclaredMethod("getListView", String.class));
    }

    @Test
    void getMaybeListView()
    {
        final Map<String, Object> map = new HashMap<>();
        map.put("valid", mock(TypedListView.class));
        map.put("invalid", "value");

        final TypedMapView sut = TypedMapView.fromMap(map);

        assertThat(sut.getMaybeListView("valid")).isPresent();
        assertThat(sut.getMaybeListView("invalid")).isEmpty(); // cast error
        assertThat(sut.getMaybeListView("foo")).isEmpty(); // key does not exist
    }

    @Test
    void getEntries()
    {
        final Map<String, Object> primitiveValues = new HashMap<>(PRIMITIVE_VALUES);
        primitiveValues.put("String2", "Value2");

        final TypedMapView sut = TypedMapView.fromMap(primitiveValues);
        final Map<String, String> stringMap = sut.getEntries(String.class);
        assertThat(stringMap).hasSize(2);
        assertThat(stringMap.get("String")).isEqualTo("Value");
        assertThat(stringMap.get("String2")).isEqualTo("Value2");
    }

    @Test
    void getEntriesAlsoReturnsSubTypes()
    {
        final TypedMapView sut = TypedMapView.fromMap(PRIMITIVE_VALUES);

        assertThat(sut.getEntries(Integer.class).values()).containsExactlyInAnyOrder(INTEGER);
        assertThat(sut.getEntries(Double.class).values()).containsExactlyInAnyOrder(DOUBLE);
        assertThat(sut.getEntries(Number.class).values()).containsExactlyInAnyOrder(INTEGER, DOUBLE, BIG_DECIMAL);
    }

    private static void expectValueCastExceptionForAllBut(
        @Nonnull final TypedMapView sut,
        @Nonnull final String key,
        @Nonnull final Method... methods )
    {

        final List<Method> expectedWorkingMethods = Arrays.asList(methods);
        for( final Method typedAccessor : TYPED_ACCESSORS ) {
            if( expectedWorkingMethods.contains(typedAccessor) ) {
                assertThatNoException().isThrownBy(() -> typedAccessor.invoke(sut, key));
            } else {
                assertThatThrownBy(() -> typedAccessor.invoke(sut, key))
                    .hasCauseExactlyInstanceOf(ValueCastException.class);
            }
        }
    }
}
