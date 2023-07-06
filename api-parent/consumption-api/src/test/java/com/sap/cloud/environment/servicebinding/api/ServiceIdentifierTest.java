/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding.api;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ServiceIdentifierTest
{
    @Test
    void testInstancesAreCachedCaseInsensitively()
    {
        final Optional<ServiceIdentifier> a = ServiceIdentifier.of("foo");
        final Optional<ServiceIdentifier> b = ServiceIdentifier.of("FOO");
        final Optional<ServiceIdentifier> c = ServiceIdentifier.of("bar");

        assertThat(a).isNotEmpty();
        assertThat(b).isNotEmpty();
        assertThat(c).isNotEmpty();

        assertThat(a).isEqualTo(b).isNotEqualTo(c);
        assertThat(a.get()).isSameAs(b.get()).isNotSameAs(c.get());
    }

    @Test
    void testOfTrimsIdentifier()
    {
        assertThat(ServiceIdentifier.of(" foo ")).isEqualTo(ServiceIdentifier.of("foo"));
        assertThat(ServiceIdentifier.of(" \nbar \t")).isEqualTo(ServiceIdentifier.of("bar"));
        assertThat(ServiceIdentifier.of(" \nb  a\tz \t")).isEqualTo(ServiceIdentifier.of("b  a\tz"));
    }

    @Test
    void testOfReturnsEmptyResultForInvalidId()
    {
        assertThat(ServiceIdentifier.of("")).isEmpty();
        assertThat(ServiceIdentifier.of("  ")).isEmpty();
    }

    @Test
    void testFromServiceNameUsesServiceNameMethod()
    {
        final ServiceBinding binding = mock(ServiceBinding.class);
        doReturn(Optional.of("foo")).when(binding).getServiceName();

        assertThat(ServiceIdentifier.fromServiceName(binding)).isEqualTo(ServiceIdentifier.of("foo"));

        verify(binding, times(1)).getServiceName();
    }

    @Test
    void testFromServiceNameThrowsExceptionForNullServiceName()
    {
        final ServiceBinding binding = mock(ServiceBinding.class);
        doReturn(Optional.empty()).when(binding).getServiceName();

        assertThatThrownBy(() -> ServiceIdentifier.fromServiceName(binding))
            .isInstanceOf(IllegalArgumentException.class);

        verify(binding, times(1)).getServiceName();
    }

    @Test
    void testEquality()
    {
        assertThat(ServiceIdentifier.of("foo")).isEqualTo(ServiceIdentifier.of("foo"));
        assertThat(ServiceIdentifier.of("foo")).isNotEqualTo(ServiceIdentifier.of("bar"));
    }

    @Test
    void testHashCode()
    {
        assertThat(ServiceIdentifier.of("foo").hashCode()).isEqualTo(ServiceIdentifier.of("foo").hashCode());
        assertThat(ServiceIdentifier.of("foo").hashCode()).isNotEqualTo(ServiceIdentifier.of("bar").hashCode());
    }

    @Test
    void testToString()
    {
        assertThat(ServiceIdentifier.of("foo")).isNotEmpty().get().hasToString("foo");
        assertThat(ServiceIdentifier.of("BAR")).isNotEmpty().get().hasToString("bar");
        assertThat(ServiceIdentifier.of("  BaZ \t")).isNotEmpty().get().hasToString("baz");
    }
}
