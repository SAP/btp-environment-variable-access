package com.sap.cloud.environment.servicebinding;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.sap.cloud.environment.servicebinding.environment.SapServiceBindingEnvironmentPostProcessor;
import com.sap.cloud.environment.servicebinding.environment.SapServiceBindingsPropertiesAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;

import static org.junit.jupiter.api.Assertions.*;

class SapSpringPropertiesServiceBindingAccessorTest
{
    @BeforeEach
    void setup() {
        SapServiceBindingsPropertiesAccessor.setServiceBindingsProperties(new HashMap<>());
    }

    @Test
    void getBindings()
    {
        final MockEnvironment mockEnvironment = TestResource.getAllBindingsProperties();
        final SapServiceBindingEnvironmentPostProcessor sapServiceBindingEnvironmentPostProcessor =
            new SapServiceBindingEnvironmentPostProcessor();
        sapServiceBindingEnvironmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

        SapSpringPropertiesServiceBindingAccessor cut = new SapSpringPropertiesServiceBindingAccessor();
        List<ServiceBinding> serviceBindings = cut.getServiceBindings();

        assertFalse(serviceBindings.isEmpty());
        assertContainsXsuaaBinding(serviceBindings);
        assertContainsServiceManagerBinding(serviceBindings);
    }

    @Test
    void getBindingsNoneExist()
    {
        final MockEnvironment mockEnvironment = TestResource.getEmptyProperties();
        final SapServiceBindingEnvironmentPostProcessor sapServiceBindingEnvironmentPostProcessor =
                new SapServiceBindingEnvironmentPostProcessor();
        sapServiceBindingEnvironmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

        SapSpringPropertiesServiceBindingAccessor cut = new SapSpringPropertiesServiceBindingAccessor();

        assertTrue(cut.getServiceBindings().isEmpty());
    }

    @Test
    void getBindingsInvalidNoLabel()
    {
        final MockEnvironment mockEnvironment = TestResource.getInvalidPropertiesNoLabel();
        final SapServiceBindingEnvironmentPostProcessor sapServiceBindingEnvironmentPostProcessor =
                new SapServiceBindingEnvironmentPostProcessor();
        sapServiceBindingEnvironmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

        SapSpringPropertiesServiceBindingAccessor cut = new SapSpringPropertiesServiceBindingAccessor();

        assertTrue(cut.getServiceBindings().isEmpty());
    }

    @Test
    void getBindingsInvalidEmptyLabel()
    {
        final MockEnvironment mockEnvironment = TestResource.getInvalidPropertiesEmptyLabel();
        final SapServiceBindingEnvironmentPostProcessor sapServiceBindingEnvironmentPostProcessor =
                new SapServiceBindingEnvironmentPostProcessor();
        sapServiceBindingEnvironmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

        SapSpringPropertiesServiceBindingAccessor cut = new SapSpringPropertiesServiceBindingAccessor();

        assertTrue(cut.getServiceBindings().isEmpty());
    }

    @Test
    void getBindingsInvalidNoTags()
    {
        final MockEnvironment mockEnvironment = TestResource.getInvalidPropertiesNoTags();
        final SapServiceBindingEnvironmentPostProcessor sapServiceBindingEnvironmentPostProcessor =
                new SapServiceBindingEnvironmentPostProcessor();
        sapServiceBindingEnvironmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

        SapSpringPropertiesServiceBindingAccessor cut = new SapSpringPropertiesServiceBindingAccessor();

        assertTrue(cut.getServiceBindings().isEmpty());
    }

    @Test
    void getBindingsInvalidEmptyTags()
    {
        final MockEnvironment mockEnvironment = TestResource.getInvalidPropertiesEmptyTags();
        final SapServiceBindingEnvironmentPostProcessor sapServiceBindingEnvironmentPostProcessor =
                new SapServiceBindingEnvironmentPostProcessor();
        sapServiceBindingEnvironmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

        SapSpringPropertiesServiceBindingAccessor cut = new SapSpringPropertiesServiceBindingAccessor();

        assertTrue(cut.getServiceBindings().isEmpty());
    }

    @Test
    void getBindingsInvalidNoName()
    {
        final MockEnvironment mockEnvironment = TestResource.getInvalidPropertiesNoName();
        final SapServiceBindingEnvironmentPostProcessor sapServiceBindingEnvironmentPostProcessor =
                new SapServiceBindingEnvironmentPostProcessor();
        sapServiceBindingEnvironmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

        SapSpringPropertiesServiceBindingAccessor cut = new SapSpringPropertiesServiceBindingAccessor();

        assertTrue(cut.getServiceBindings().isEmpty());
    }

    @Test
    void getBindingsInvalidEmptyName()
    {
        final MockEnvironment mockEnvironment = TestResource.getInvalidPropertiesEmptyName();
        final SapServiceBindingEnvironmentPostProcessor sapServiceBindingEnvironmentPostProcessor =
                new SapServiceBindingEnvironmentPostProcessor();
        sapServiceBindingEnvironmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

        SapSpringPropertiesServiceBindingAccessor cut = new SapSpringPropertiesServiceBindingAccessor();

        assertTrue(cut.getServiceBindings().isEmpty());
    }

    @Test
    void getBindingsInvalidNoCredentials()
    {
        final MockEnvironment mockEnvironment = TestResource.getInvalidPropertiesNoCredentials();
        final SapServiceBindingEnvironmentPostProcessor sapServiceBindingEnvironmentPostProcessor =
                new SapServiceBindingEnvironmentPostProcessor();
        sapServiceBindingEnvironmentPostProcessor.postProcessEnvironment(mockEnvironment, null);

        SapSpringPropertiesServiceBindingAccessor cut = new SapSpringPropertiesServiceBindingAccessor();

        assertTrue(cut.getServiceBindings().isEmpty());
    }

    private void assertContainsXsuaaBinding( List<ServiceBinding> serviceBindings )
    {
        Optional<ServiceBinding> maybeXsuaaBinding =
            serviceBindings
                .stream()
                .filter(binding -> binding.getName().isPresent() && binding.getServiceName().get().equals("xsuaa"))
                .findFirst();
        assertTrue(maybeXsuaaBinding.isPresent());
        ServiceBinding xsuaaCertBinding = maybeXsuaaBinding.get();
        assertTrue(xsuaaCertBinding.getServicePlan().isPresent());
        assertEquals("broker", xsuaaCertBinding.getServicePlan().get());
        assertTrue(xsuaaCertBinding.getTags().contains("xsuaa"));
        assertTrue(xsuaaCertBinding.getTags().contains("test-xsuaa"));
        assertTrue(xsuaaCertBinding.getServiceName().isPresent());
        assertEquals("xsuaa", xsuaaCertBinding.getServiceName().get());
        assertEquals("https://localhost:8080", xsuaaCertBinding.getCredentials().get("certurl"));
    }

    private void assertContainsServiceManagerBinding( List<ServiceBinding> serviceBindings
            ) {
        Optional<ServiceBinding> maybeServiceManagerBinding =
                serviceBindings
                        .stream()
                        .filter(binding -> binding.getName().isPresent() && binding.getServiceName().get().equals("service-manager"))
                        .findFirst();
        assertTrue(maybeServiceManagerBinding.isPresent());
        ServiceBinding serviceManagerBinding = maybeServiceManagerBinding.get();
        assertEquals("service-manager-test", serviceManagerBinding.getName().get());
        assertEquals("standard", serviceManagerBinding.getServicePlan().get());
        assertEquals("test-service-manager", serviceManagerBinding.getTags().get(0));
        assertNotNull(serviceManagerBinding.getCredentials());
    }
}
