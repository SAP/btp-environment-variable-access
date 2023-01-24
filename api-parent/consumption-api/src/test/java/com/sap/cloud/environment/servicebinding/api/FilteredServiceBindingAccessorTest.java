package com.sap.cloud.environment.servicebinding.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
                    ServiceBindingProperty.TAGS
                        .contains("tag")
                        .or(ServiceBindingProperty.SERVICE_NAME.isEqualTo("servicename")))
                .build();

        assertThat(sut.getServiceBindings()).containsExactlyInAnyOrder(BINDING_WITH_TAG, BINDING_WITH_SERVICE_NAME);
    }

    @Test
    void testFilterAlternativeUsage()
    {
        final ServiceBindingAccessor accessor =
            () -> Arrays.asList(EMPTY_BINDING, BINDING_WITH_TAG, BINDING_WITH_SERVICE_NAME);

        final ServiceBindingFilter filter =
            ServiceBindingProperty.TAGS
                .contains("tag")
                .or(ServiceBindingProperty.SERVICE_NAME.isEqualTo("servicename"));

        final List<ServiceBinding> filteredBindings =
            accessor.getServiceBindings().stream().filter(filter::matches).collect(Collectors.toList());

        assertThat(filteredBindings).containsExactlyInAnyOrder(BINDING_WITH_TAG, BINDING_WITH_SERVICE_NAME);
    }
}
