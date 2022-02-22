package com.sap.cloud.environment.api;


import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBindingMergerTest
{
    @Test
    void mergeSingleAccessor()
    {
        final ServiceBinding serviceBinding1 = serviceBinding("XSUAA", "lite");
        final ServiceBinding serviceBinding2 = serviceBinding("XSUAA", "application");

        final ServiceBindingAccessor accessor = mock(ServiceBindingAccessor.class);
        when(accessor.getServiceBindings()).thenReturn(Arrays.asList(serviceBinding1, serviceBinding2));

        final ServiceBindingMerger sut = new ServiceBindingMerger(Collections.singleton(accessor), ServiceTypeAndPlanSelector.INSTANCE);

        final Iterable<ServiceBinding> mergedServiceBindings = sut.getServiceBindings();
        assertThat(mergedServiceBindings).containsExactlyInAnyOrder(serviceBinding1, serviceBinding2);
    }

    @Nonnull
    private static ServiceBinding serviceBinding( @Nonnull final String serviceType, @Nonnull final String plan )
    {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("type", serviceType);
        properties.put("plan", plan);

        // we add an additional (unique) property to make sure that service bindings with equal keys are not actually equal (in terms of object comparison)
        // that way, we are able to test whether the merger considers the order in which the bindings are returned
        properties.put("id", UUID.randomUUID().toString());

        return DefaultServiceBinding.builder().copy(properties).build();
    }

    @Test
    void mergeSingleAccessorWithDuplicates()
    {
        final ServiceBinding serviceBinding1 = serviceBinding("XSUAA", "lite");
        final ServiceBinding serviceBinding2 = serviceBinding("XSUAA", "lite");

        final ServiceBindingAccessor accessor = mock(ServiceBindingAccessor.class);
        when(accessor.getServiceBindings()).thenReturn(Arrays.asList(serviceBinding1, serviceBinding2));

        final ServiceBindingMerger sut = new ServiceBindingMerger(Collections.singleton(accessor), ServiceTypeAndPlanSelector.INSTANCE);

        final Iterable<ServiceBinding> mergedServiceBindings = sut.getServiceBindings();
        assertThat(mergedServiceBindings).containsExactlyInAnyOrder(serviceBinding1);
    }

    @Test
    void mergeMultipleAccessors()
    {
        final ServiceBinding serviceBinding1 = serviceBinding("XSUAA", "lite");
        final ServiceBinding serviceBinding2 = serviceBinding("XSUAA", "application");

        final ServiceBindingAccessor accessor1 = mock(ServiceBindingAccessor.class);
        when(accessor1.getServiceBindings()).thenReturn(Collections.singletonList(serviceBinding1));

        final ServiceBindingAccessor accessor2 = mock(ServiceBindingAccessor.class);
        when(accessor2.getServiceBindings()).thenReturn(Collections.singletonList(serviceBinding2));

        final ServiceBindingMerger sut = new ServiceBindingMerger(Arrays.asList(accessor1, accessor2), ServiceTypeAndPlanSelector.INSTANCE);

        final Iterable<ServiceBinding> mergedServiceBindings = sut.getServiceBindings();
        assertThat(mergedServiceBindings).containsExactlyInAnyOrder(serviceBinding1, serviceBinding2);
    }

    @Test
    void mergeMultipleAccessorsWithDuplicates()
    {
        final ServiceBinding serviceBinding1 = serviceBinding("XSUAA", "lite");
        final ServiceBinding serviceBinding2 = serviceBinding("XSUAA", "lite");

        final ServiceBindingAccessor accessor1 = mock(ServiceBindingAccessor.class);
        when(accessor1.getServiceBindings()).thenReturn(Collections.singletonList(serviceBinding1));

        final ServiceBindingAccessor accessor2 = mock(ServiceBindingAccessor.class);
        when(accessor2.getServiceBindings()).thenReturn(Collections.singletonList(serviceBinding2));

        final ServiceBindingMerger sut = new ServiceBindingMerger(Arrays.asList(accessor1, accessor2), ServiceTypeAndPlanSelector.INSTANCE);

        final Iterable<ServiceBinding> mergedServiceBindings = sut.getServiceBindings();
        assertThat(mergedServiceBindings).containsExactlyInAnyOrder(serviceBinding1);
    }

    private enum ServiceTypeAndPlanSelector implements ServiceBindingMerger.KeySelector
    {
        INSTANCE;

        @Nonnull
        @Override
        public Object selectKey( @Nonnull final ServiceBinding serviceBinding )
        {
            final Object serviceTypeProperty = serviceBinding.get("type");
            final Object planProperty = serviceBinding.get("plan");

            return Objects.requireNonNull(serviceTypeProperty) + ":" + Objects.requireNonNull(planProperty);
        }
    }
}