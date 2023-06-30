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
        assertThat(ServiceIdentifier.of("foo")).isSameAs(ServiceIdentifier.of("FOO"));
        assertThat(ServiceIdentifier.of("foo")).isNotSameAs(ServiceIdentifier.of("bar"));
    }

    @Test
    void testOfTrimsIdentifier()
    {
        assertThat(ServiceIdentifier.of(" foo ")).isSameAs(ServiceIdentifier.of("foo"));
        assertThat(ServiceIdentifier.of(" \nbar \t")).isSameAs(ServiceIdentifier.of("bar"));
        assertThat(ServiceIdentifier.of(" \nb  a\tz \t")).isSameAs(ServiceIdentifier.of("b  a\tz"));
    }

    @Test
    void testOfThrowsExceptionForEmptyIdentifier()
    {
        assertThatThrownBy(() -> ServiceIdentifier.of("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ServiceIdentifier.of("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testFromServiceNameUsesServiceNameMethod()
    {
        final ServiceBinding binding = mock(ServiceBinding.class);
        doReturn(Optional.of("foo")).when(binding).getServiceName();

        assertThat(ServiceIdentifier.fromServiceName(binding)).isSameAs(ServiceIdentifier.of("foo"));

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
        assertThat(ServiceIdentifier.of("foo")).hasToString("foo");
        assertThat(ServiceIdentifier.of("BAR")).hasToString("bar");
        assertThat(ServiceIdentifier.of("  BaZ \t")).hasToString("baz");
    }
}
