/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.cloud.environment.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBindingAccessorTest
{
    ServiceBindingAccessor sut;
    ServiceBinding serviceBinding1;
    ServiceBinding serviceBinding2;

    @BeforeEach
    void setup()
    {
        serviceBinding1 = DefaultServiceBinding.builder().copy(Collections.EMPTY_MAP).withName("MY_SERVICE").build();
        serviceBinding2 = DefaultServiceBinding.builder().copy(Collections.EMPTY_MAP).withName("MY_SERVICE_2").build();

        sut = mock(ServiceBindingAccessor.class);
        when(sut.getServiceBindings()).thenReturn(Arrays.asList(serviceBinding1, serviceBinding2));
        when(sut.getServiceBindingByName(any())).thenCallRealMethod();
        List<ServiceBinding> s = sut.getServiceBindings();
    }

    @Test
    void getServiceBinding()
    {
        assertThat(sut.getServiceBindingByName("MY_SERVICE")).isEqualTo(serviceBinding1);
    }

    @Test
    void getServiceBinding_NotFound()
    {
        assertThatThrownBy(() -> {
            sut.getServiceBindingByName("NOT_EXISTING");
        }).isInstanceOf(IllegalStateException.class).hasMessageContaining("There exists no service binding of name");
    }

    @Test
    void getServiceBinding_FoundMultiple()
    {
        when(sut.getServiceBindings()).thenReturn(Arrays.asList(serviceBinding1, serviceBinding2, serviceBinding2));
        assertThatThrownBy(() -> {
            sut.getServiceBindingByName("MY_SERVICE_2");
        }).isInstanceOf(IllegalStateException.class).hasMessageContaining("Found multiple service bindings of name");
    }
}

