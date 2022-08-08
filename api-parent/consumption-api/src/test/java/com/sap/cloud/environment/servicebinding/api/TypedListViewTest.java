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
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sap.cloud.environment.servicebinding.api.exception.ValueCastException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TypedListViewTest
{
    private static final Collection<Object> PRIMITIVE_VALUES = new ArrayList<>();

    private static final Collection<Method> TYPED_ACCESSORS = new ArrayList<>();

    private static final int INTEGER = 42;

    private static final double DOUBLE = 13.37d;

    private static final BigDecimal BIG_DECIMAL = BigDecimal.valueOf(Long.MAX_VALUE, Integer.MAX_VALUE);

    @BeforeAll
    static void beforeAll()
        throws NoSuchMethodException
    {
        PRIMITIVE_VALUES.add(null);
        PRIMITIVE_VALUES.add(true);
        PRIMITIVE_VALUES.add(INTEGER);
        PRIMITIVE_VALUES.add(DOUBLE);
        PRIMITIVE_VALUES.add(BIG_DECIMAL);
        PRIMITIVE_VALUES.add("Value");

        TYPED_ACCESSORS.add(TypedListView.class.getDeclaredMethod("getBoolean", int.class));
        TYPED_ACCESSORS.add(TypedListView.class.getDeclaredMethod("getInteger", int.class));
        TYPED_ACCESSORS.add(TypedListView.class.getDeclaredMethod("getDouble", int.class));
        TYPED_ACCESSORS.add(TypedListView.class.getDeclaredMethod("getNumber", int.class));
        TYPED_ACCESSORS.add(TypedListView.class.getDeclaredMethod("getString", int.class));
        TYPED_ACCESSORS.add(TypedListView.class.getDeclaredMethod("getListView", int.class));
        TYPED_ACCESSORS.add(TypedListView.class.getDeclaredMethod("getMapView", int.class));
    }

    @Test
    void create()
    {
        final Collection<Object> collection = new ArrayList<>(PRIMITIVE_VALUES);
        collection.add(mock(TypedMapView.class));
        collection.add(mock(TypedListView.class));

        final TypedListView sut = TypedListView.fromIterable(collection);

        assertThat(sut).isNotNull();

        assertThat(sut.getSize()).isEqualTo(8);

        assertThat(sut.get(0)).isNull();
        assertThat(sut.getBoolean(1)).isTrue();
        assertThat(sut.getInteger(2)).isEqualTo(INTEGER);
        assertThat(sut.getDouble(3)).isEqualTo(DOUBLE);
        assertThat(sut.getNumber(4)).isEqualTo(BigDecimal.valueOf(Long.MAX_VALUE, Integer.MAX_VALUE));
        assertThat(sut.getString(5)).isEqualTo("Value");
        assertThat(sut.getMapView(6)).isNotNull();
        assertThat(sut.getListView(7)).isNotNull();
    }

    @Test
    void createTransformsNestedMapToMapView()
    {
        final TypedListView sut =
            TypedListView.fromIterable(Collections.singletonList(Collections.singletonMap("Key", "Value")));

        assertThat(sut.getMapView(0)).isNotNull();
        assertThat(sut.getMapView(0).getKeys()).containsExactlyInAnyOrder("Key");
    }

    @Test
    void createTransformsNestedListToListView()
    {
        final TypedListView sut = TypedListView.fromIterable(Collections.singletonList(PRIMITIVE_VALUES));

        assertThat(sut.getListView(0)).isNotNull();
        assertThat(sut.getListView(0).getSize()).isEqualTo(6);
    }

    @Test
    void getBoolean()
        throws NoSuchMethodException
    {
        final TypedListView sut = TypedListView.fromIterable(Collections.singletonList(true));

        expectValueCastExceptionForAllBut(sut, 0, TypedListView.class.getDeclaredMethod("getBoolean", int.class));
    }

    @Test
    void getInteger()
        throws NoSuchMethodException
    {
        final TypedListView sut = TypedListView.fromIterable(Collections.singletonList(INTEGER));

        expectValueCastExceptionForAllBut(
            sut,
            0,
            TypedListView.class.getDeclaredMethod("getInteger", int.class),
            TypedListView.class.getDeclaredMethod("getDouble", int.class),
            TypedListView.class.getDeclaredMethod("getNumber", int.class));
    }

    @Test
    void getDouble()
        throws NoSuchMethodException
    {
        final TypedListView sut = TypedListView.fromIterable(Collections.singletonList(DOUBLE));

        expectValueCastExceptionForAllBut(
            sut,
            0,
            TypedListView.class.getDeclaredMethod("getInteger", int.class),
            TypedListView.class.getDeclaredMethod("getDouble", int.class),
            TypedListView.class.getDeclaredMethod("getNumber", int.class));
    }

    @Test
    void getNumber()
        throws NoSuchMethodException
    {
        final TypedListView sut =
            TypedListView
                .fromIterable(Collections.singletonList(BigDecimal.valueOf(Long.MAX_VALUE, Integer.MAX_VALUE)));

        expectValueCastExceptionForAllBut(
            sut,
            0,
            TypedListView.class.getDeclaredMethod("getInteger", int.class),
            TypedListView.class.getDeclaredMethod("getDouble", int.class),
            TypedListView.class.getDeclaredMethod("getNumber", int.class));
    }

    @Test
    void getString()
        throws NoSuchMethodException
    {
        final TypedListView sut = TypedListView.fromIterable(Collections.singletonList("Value"));

        expectValueCastExceptionForAllBut(sut, 0, TypedListView.class.getDeclaredMethod("getString", int.class));
    }

    @Test
    void getMapView()
        throws NoSuchMethodException
    {
        final TypedListView sut = TypedListView.fromIterable(Collections.singletonList(mock(TypedMapView.class)));

        expectValueCastExceptionForAllBut(sut, 0, TypedListView.class.getDeclaredMethod("getMapView", int.class));
    }

    @Test
    void getListView()
        throws NoSuchMethodException
    {
        final TypedListView sut = TypedListView.fromIterable(Collections.singletonList(mock(TypedListView.class)));

        expectValueCastExceptionForAllBut(sut, 0, TypedListView.class.getDeclaredMethod("getListView", int.class));
    }

    @Test
    void getItems()
    {
        final Collection<Object> primitiveValues = new ArrayList<>(PRIMITIVE_VALUES);
        primitiveValues.add("Value2");

        final TypedListView sut = TypedListView.fromIterable(primitiveValues);
        final List<String> stringList = sut.getItems(String.class);
        assertThat(stringList).hasSize(2).contains("Value").contains("Value2");
    }

    @Test
    void getItemsAlsoReturnSubTypes()
    {
        final TypedListView sut = TypedListView.fromIterable(PRIMITIVE_VALUES);

        assertThat(sut.getItems(Integer.class)).containsExactly(INTEGER);
        assertThat(sut.getItems(Double.class)).containsExactly(DOUBLE);
        assertThat(sut.getItems(Number.class)).containsExactly(INTEGER, DOUBLE, BIG_DECIMAL);
    }

    private static void expectValueCastExceptionForAllBut(
        @Nonnull final TypedListView sut,
        final int index,
        @Nonnull final Method... methods )
    {

        final List<Method> expectedWorkingMethods = Arrays.asList(methods);
        for( final Method typedAccessor : TYPED_ACCESSORS ) {
            if( expectedWorkingMethods.contains(typedAccessor) ) {
                assertThatNoException().isThrownBy(() -> typedAccessor.invoke(sut, index));
            } else {
                assertThatThrownBy(() -> typedAccessor.invoke(sut, index))
                    .hasCauseExactlyInstanceOf(ValueCastException.class);
            }
        }
    }
}
