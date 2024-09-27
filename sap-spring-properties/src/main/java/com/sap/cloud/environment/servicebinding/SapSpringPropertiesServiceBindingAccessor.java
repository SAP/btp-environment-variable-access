package com.sap.cloud.environment.servicebinding;

import com.sap.cloud.environment.servicebinding.api.DefaultServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBindingAccessor;
import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;
import com.sap.cloud.environment.servicebinding.environment.SapServiceBindingsPropertiesAccessor;
import com.sap.cloud.environment.servicebinding.SapServiceBindingsProperties.ServiceBindingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A {@link ServiceBindingAccessor} that is able to load {@link SapServiceBindingsProperties}s from Spring's application
 * properties.
 */
public class SapSpringPropertiesServiceBindingAccessor implements ServiceBindingAccessor
{
    @Nonnull
    private static final Logger logger = LoggerFactory.getLogger(SapSpringPropertiesServiceBindingAccessor.class);

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings()
        throws ServiceBindingAccessException
    {
        List<ServiceBinding> serviceBindings = fetchServiceBindings();

        serviceBindings
            .forEach(binding -> logger.debug("Service binding {} found in properties.", binding.getServiceName().get()) //NOSONAR
            );

        return serviceBindings;
    }

    @Nonnull
    private List<ServiceBinding> fetchServiceBindings()
    {
        Map<String, ServiceBindingProperties> serviceBindingsProperties =
            SapServiceBindingsPropertiesAccessor.getServiceBindingsProperties();

        if( serviceBindingsProperties.isEmpty() ) {
            logger.info("No service bindings found in properties.");
        }

        return serviceBindingsProperties
            .entrySet()
            .stream()
            .map(entry -> toServiceBinding(entry.getKey(), entry.getValue()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Nullable
    private ServiceBinding toServiceBinding(
        @Nonnull String serviceBindingName,
        @Nonnull final ServiceBindingProperties serviceBindingProperties )
    {
        if( validateServiceBindingProperties(serviceBindingProperties) ) {
            return DefaultServiceBinding
                .builder()
                .copy(Collections.emptyMap())
                .withName(serviceBindingName)
                .withServiceName(serviceBindingProperties.getName())
                .withServicePlan(serviceBindingProperties.getPlan())
                .withTags(Arrays.asList(serviceBindingProperties.getTags()))
                .withCredentials(serviceBindingProperties.getCredentials())
                .build();
        } else {
            return null;
        }
    }

    private boolean validateServiceBindingProperties( @Nonnull final ServiceBindingProperties serviceBindingProperties )
    {
        boolean isValid = true;
        if( serviceBindingProperties.getName() == null || serviceBindingProperties.getName().isEmpty() ) {
            logger.error("Service binding properties with no name detected.");
            isValid = false;
        }

        if( serviceBindingProperties.getCredentials().isEmpty() ) {
            logger.error("Service binding properties of {} has no credentials.", serviceBindingProperties.getName());
            isValid = false;
        }

        return isValid;
    }
}
