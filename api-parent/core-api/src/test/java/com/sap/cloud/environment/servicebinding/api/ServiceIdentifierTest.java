package com.sap.cloud.environment.servicebinding.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceIdentifierTest
{
    @Test
    void testInstancesAreCachedCaseInsensitively()
    {
        final ServiceIdentifier a = ServiceIdentifier.of("foo");
        final ServiceIdentifier b = ServiceIdentifier.of("FOO");
        final ServiceIdentifier c = ServiceIdentifier.of("bar");

        assertThat(a).isSameAs(b).isNotSameAs(c);
    }

    @Test
    void testOfTrimsIdentifier()
    {
        assertThat(ServiceIdentifier.of(" foo ")).isSameAs(ServiceIdentifier.of("foo"));
        assertThat(ServiceIdentifier.of(" \nbar \t")).isSameAs(ServiceIdentifier.of("bar"));
        assertThat(ServiceIdentifier.of(" \nb  a\tz \t")).isSameAs(ServiceIdentifier.of("b  a\tz"));
    }

    @Test
    void testOfThrowsExceptionForEmptyOrBlankId()
    {
        assertThatThrownBy(() -> ServiceIdentifier.of("")).isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ServiceIdentifier.of("  ")).isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ServiceIdentifier.of("\t")).isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ServiceIdentifier.of("\n")).isExactlyInstanceOf(IllegalArgumentException.class);
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
