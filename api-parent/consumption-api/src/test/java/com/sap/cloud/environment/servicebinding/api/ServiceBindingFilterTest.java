package com.sap.cloud.environment.servicebinding.api;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static com.sap.cloud.environment.servicebinding.api.ServiceBindingFilter.FALSE;
import static com.sap.cloud.environment.servicebinding.api.ServiceBindingFilter.TRUE;
import static org.assertj.core.api.Assertions.assertThat;

class ServiceBindingFilterTest
{
    private static final ServiceBinding EMPTY_BINDING =
        DefaultServiceBinding.builder().copy(Collections.emptyMap()).build();

    @Test
    void testAndMatching()
    {
        assertThat(TRUE.and(TRUE).matches(EMPTY_BINDING)).isTrue();
        assertThat(TRUE.and(FALSE).matches(EMPTY_BINDING)).isFalse();
        assertThat(FALSE.and(TRUE).matches(EMPTY_BINDING)).isFalse();
        assertThat(FALSE.and(FALSE).matches(EMPTY_BINDING)).isFalse();
    }

    @Test
    void testOrMatching()
    {
        assertThat(TRUE.or(TRUE).matches(EMPTY_BINDING)).isTrue();
        assertThat(TRUE.or(FALSE).matches(EMPTY_BINDING)).isTrue();
        assertThat(FALSE.or(TRUE).matches(EMPTY_BINDING)).isTrue();
        assertThat(FALSE.or(FALSE).matches(EMPTY_BINDING)).isFalse();
    }

    @Test
    void testNotMatching()
    {
        assertThat(TRUE.not().matches(EMPTY_BINDING)).isFalse();
        assertThat(FALSE.not().matches(EMPTY_BINDING)).isTrue();
    }
}
