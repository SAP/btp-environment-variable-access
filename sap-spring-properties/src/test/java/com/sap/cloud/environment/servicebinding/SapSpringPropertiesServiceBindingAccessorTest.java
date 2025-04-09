package com.sap.cloud.environment.servicebinding;

import com.sap.cloud.environment.servicebinding.api.ServiceBinding;
import com.sap.cloud.environment.servicebinding.environment.SapServiceBindingEnvironmentPostProcessor;
import com.sap.cloud.environment.servicebinding.environment.SapServiceBindingsPropertiesAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SapSpringPropertiesServiceBindingAccessorTest
{
    @BeforeEach
    void setup()
    {
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
        assertContainsServiceManagerBindings(serviceBindings);
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
    void getBindingsInvalidNoName()
    {
        final MockEnvironment mockEnvironment = TestResource.getInvalidPropertiesNoServiceName();
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
                .filter(
                    binding -> binding.getServiceName().isPresent() && binding.getServiceName().get().equals("xsuaa"))
                .findFirst();
        assertTrue(maybeXsuaaBinding.isPresent());
        ServiceBinding xsuaaCertBinding = maybeXsuaaBinding.get();
        assertTrue(xsuaaCertBinding.getServicePlan().isPresent());
        assertEquals("broker", xsuaaCertBinding.getServicePlan().get());
        assertTrue(xsuaaCertBinding.getServiceName().isPresent());
        assertEquals("xsuaa", xsuaaCertBinding.getServiceName().get());
        assertEquals("xsuaa-test", xsuaaCertBinding.getName().get());
        assertEquals("https://localhost:8080", xsuaaCertBinding.getCredentials().get("certurl"));
    }

    private void assertContainsServiceManagerBindings( List<ServiceBinding> serviceBindings )
    {
        Map<String, ServiceBinding> serviceManagerBindings =
            serviceBindings
                .stream()
                .filter(
                    binding -> binding.getServiceName().isPresent()
                        && binding.getServiceName().get().equals("service-manager"))
                .collect(
                    Collectors
                        .toMap(serviceBinding -> serviceBinding.getName().get(), serviceBinding -> serviceBinding));
        assertEquals(2, serviceManagerBindings.size());

        ServiceBinding serviceManagerBinding1 = serviceManagerBindings.get("service-manager-test");
        assertEquals("service-manager", serviceManagerBinding1.getServiceName().get());
        assertEquals("service-manager-test", serviceManagerBinding1.getName().get());
        assertEquals("standard", serviceManagerBinding1.getServicePlan().get());
        assertNotNull(serviceManagerBinding1.getCredentials());
        assertEquals("https://localhost:8080", serviceManagerBinding1.getCredentials().get("url"));

        ServiceBinding serviceManagerBinding2 = serviceManagerBindings.get("service_manager_test2");
        assertEquals("service-manager", serviceManagerBinding2.getServiceName().get());
        assertEquals("service_manager_test2", serviceManagerBinding2.getName().get());
        assertEquals("standard", serviceManagerBinding2.getServicePlan().get());
        assertNotNull(serviceManagerBinding2.getCredentials());
        assertEquals("https://localhost:8081", serviceManagerBinding2.getCredentials().get("url"));
    }
}
