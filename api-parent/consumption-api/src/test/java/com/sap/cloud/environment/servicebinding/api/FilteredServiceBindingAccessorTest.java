package com.sap.cloud.environment.servicebinding.api;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FilteredServiceBindingAccessorTest
{
    private static final ServiceBinding EMPTY_BINDING =
        DefaultServiceBinding.builder().copy(Collections.emptyMap()).build();
    private static final ServiceBinding BINDING_WITH_TAG =
        DefaultServiceBinding.builder().copy(Collections.emptyMap()).withTags(Collections.singletonList("Tag")).build();
    private static final ServiceBinding BINDING_WITH_SERVICE_NAME =
        DefaultServiceBinding.builder().copy(Collections.emptyMap()).withServiceName("ServiceName").build();

    @Test
    void testFilterForTagOrServiceName()
    {
        final ServiceBindingAccessor delegate =
            () -> Arrays.asList(EMPTY_BINDING, BINDING_WITH_TAG, BINDING_WITH_SERVICE_NAME);

        final FilteredServiceBindingAccessor sut =
            FilteredServiceBindingAccessor
                .from(delegate)
                .where(
                    ServiceBindingProperty
                        .tags()
                        .contains("tag")
                        .or(ServiceBindingProperty.serviceName().isEqualTo("servicename")))
                .build();

        assertThat(sut.getServiceBindings()).containsExactlyInAnyOrder(BINDING_WITH_TAG, BINDING_WITH_SERVICE_NAME);
    }
}
