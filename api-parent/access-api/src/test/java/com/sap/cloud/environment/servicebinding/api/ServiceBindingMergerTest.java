package com.sap.cloud.environment.servicebinding.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import static com.sap.cloud.environment.servicebinding.api.ServiceBindingMerger.KEEP_EVERYTHING;
import static com.sap.cloud.environment.servicebinding.api.ServiceBindingMerger.KEEP_UNIQUE;
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

        final ServiceBindingMerger sut =
            new ServiceBindingMerger(Collections.singleton(accessor), ServiceNameAndPlanComparer.INSTANCE);

        final List<ServiceBinding> mergedServiceBindings = sut.getServiceBindings();
        assertThat(mergedServiceBindings).containsExactlyInAnyOrder(serviceBinding1, serviceBinding2);
    }

    @Test
    void mergeSingleAccessorWithDuplicates()
    {
        final ServiceBinding serviceBinding1 = serviceBinding("XSUAA", "lite");
        final ServiceBinding serviceBinding2 = serviceBinding("XSUAA", "lite");

        final ServiceBindingAccessor accessor = mock(ServiceBindingAccessor.class);
        when(accessor.getServiceBindings()).thenReturn(Arrays.asList(serviceBinding1, serviceBinding2));

        final ServiceBindingMerger sut =
            new ServiceBindingMerger(Collections.singleton(accessor), ServiceNameAndPlanComparer.INSTANCE);

        final List<ServiceBinding> mergedServiceBindings = sut.getServiceBindings();
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

        final ServiceBindingMerger sut =
            new ServiceBindingMerger(Arrays.asList(accessor1, accessor2), ServiceNameAndPlanComparer.INSTANCE);

        final List<ServiceBinding> mergedServiceBindings = sut.getServiceBindings();
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

        final ServiceBindingMerger sut =
            new ServiceBindingMerger(Arrays.asList(accessor1, accessor2), ServiceNameAndPlanComparer.INSTANCE);

        final List<ServiceBinding> mergedServiceBindings = sut.getServiceBindings();
        assertThat(mergedServiceBindings).containsExactlyInAnyOrder(serviceBinding1);
    }

    @Test
    void keepEverythingEvenKeepsSameReference()
    {
        final ServiceBinding binding = serviceBinding("XSUAA", "lite");

        assertThat(KEEP_EVERYTHING.areEqual(binding, binding)).isFalse();
    }

    @Test
    void keepUniqueDoesNotIncludeDuplicates()
    {
        final ComparableServiceBinding bindingA = new ComparableServiceBinding();
        final ComparableServiceBinding bindingB = new ComparableServiceBinding();
        final ComparableServiceBinding bindingC = new ComparableServiceBinding();

        // bindings A and C are equal
        bindingA.getEqualBindings().add(bindingC);
        bindingC.getEqualBindings().add(bindingA);

        final ServiceBindingAccessor accessorA = () -> Arrays.asList(bindingA, bindingB);
        final ServiceBindingAccessor accessorB = () -> Arrays.asList(bindingB, bindingC);

        final ServiceBindingMerger sut = new ServiceBindingMerger(Arrays.asList(accessorA, accessorB), KEEP_UNIQUE);

        assertThat(sut.getServiceBindings()).containsExactlyInAnyOrder(bindingA, bindingB);
    }

    @Test
    void keepUniqueUsesEquals()
    {
        final ComparableServiceBinding a = new ComparableServiceBinding();
        final ComparableServiceBinding b = new ComparableServiceBinding();

        assertThat(KEEP_UNIQUE.areEqual(a, a)).isTrue();
        assertThat(KEEP_UNIQUE.areEqual(a, b)).isFalse();

        // the order of these assertions is important, since the `containsExactly` assertion will also use the `equals` method.
        // therefore, if we were to check for `a` first, `b#equals` would also be invoked
        assertThat(b.getEqualsInvocations()).isEmpty();
        assertThat(a.getEqualsInvocations()).containsExactly(a, b);
    }

    @Nonnull
    private static ServiceBinding serviceBinding( @Nonnull final String serviceType, @Nonnull final String plan )
    {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("type", serviceType);
        properties.put("plan", plan);

        // we add an additional (unique) property to make sure that service bindings with equal keys are not actually
        // equal (in terms of object comparison)
        // that way, we are able to test whether the merger considers the order in which the bindings are returned
        properties.put("id", UUID.randomUUID().toString());

        return DefaultServiceBinding
            .builder()
            .copy(properties)
            .withNameKey("id")
            .withServiceNameKey("type")
            .withServicePlanKey("plan")
            .build();
    }

    private enum ServiceNameAndPlanComparer implements ServiceBindingMerger.EqualityComparer
    {
        INSTANCE;

        @Override
        public boolean areEqual( @Nonnull final ServiceBinding a, @Nonnull final ServiceBinding b )
        {
            return a.getServiceName().equals(b.getServiceName()) && a.getServicePlan().equals(b.getServicePlan());
        }

    }

    private static class ComparableServiceBinding implements ServiceBinding
    {
        private final List<ServiceBinding> equalBindings = new ArrayList<>();
        private final List<Object> equalsInvocations = new ArrayList<>();

        @Nonnull
        public List<ServiceBinding> getEqualBindings()
        {
            return equalBindings;
        }

        @Nonnull
        public List<Object> getEqualsInvocations()
        {
            return equalsInvocations;
        }

        @Nonnull
        @Override
        public Set<String> getKeys()
        {
            return Collections.emptySet();
        }

        @Override
        public boolean containsKey( @Nonnull String key )
        {
            return false;
        }

        @Nonnull
        @Override
        public Optional<Object> get( @Nonnull String key )
        {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<String> getName()
        {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<String> getServiceName()
        {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<String> getServicePlan()
        {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public List<String> getTags()
        {
            return Collections.emptyList();
        }

        @Nonnull
        @Override
        public Map<String, Object> getCredentials()
        {
            return Collections.emptyMap();
        }

        @Override
        public boolean equals( @Nullable final Object obj )
        {
            equalsInvocations.add(obj);

            if( this == obj ) {
                return true;
            }

            return equalBindings.stream().anyMatch(binding -> binding == obj);
        }
    }
}
