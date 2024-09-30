package com.sap.cloud.environment.servicebinding;

import com.sap.cloud.environment.servicebinding.SapServiceBindingsProperties.ServiceBindingProperties;
import com.sap.cloud.environment.servicebinding.api.DefaultServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBindingAccessor;
import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;
import com.sap.cloud.environment.servicebinding.environment.SapServiceBindingsPropertiesAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

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
            .forEach(binding -> logger.debug("Service binding {} found in properties.", binding.getName().get()) //NOSONAR
            );

        return serviceBindings;
    }

    @Nonnull
    private List<ServiceBinding> fetchServiceBindings()
    {
        Map<String, ServiceBindingProperties> serviceBindingsProperties =
            SapServiceBindingsPropertiesAccessor.getServiceBindingsProperties();

        if( serviceBindingsProperties.isEmpty() ) {
            logger.debug("No service bindings provided by application properties.");
        }

        return serviceBindingsProperties
            .entrySet()
            .stream()
            .map(entry -> toServiceBinding(entry.getKey(), entry.getValue()))
            .filter(Objects::nonNull)
            .toList();
    }

    @Nullable
    private ServiceBinding toServiceBinding(
        @Nonnull String serviceBindingName,
        @Nonnull final ServiceBindingProperties serviceBindingProperties )
    {
        if( validateServiceBindingProperties(serviceBindingProperties, serviceBindingName) ) {
            return DefaultServiceBinding
                .builder()
                .copy(Collections.emptyMap())
                .withName(
                    StringUtils.hasText(serviceBindingProperties.getName())
                        ? serviceBindingProperties.getName()
                        : serviceBindingName)
                .withServiceName(serviceBindingProperties.getServiceName())
                .withServicePlan(serviceBindingProperties.getPlan())
                .withTags(Arrays.asList(serviceBindingProperties.getTags()))
                .withCredentials(serviceBindingProperties.getCredentials())
                .build();
        } else {
            return null;
        }
    }

    private boolean validateServiceBindingProperties(
        @Nonnull final ServiceBindingProperties serviceBindingProperties,
        @Nonnull final String serviceBindingName )
    {
        boolean isValid = true;
        if( serviceBindingProperties.getServiceName() == null || serviceBindingProperties.getServiceName().isEmpty() ) {
            logger.error("Service binding properties of {} with no service name detected.", serviceBindingName);
            isValid = false;
        }

        if( serviceBindingProperties.getCredentials().isEmpty() ) {
            logger.error("Service binding properties of {} has no credentials.", serviceBindingName);
            isValid = false;
        }

        return isValid;
    }
}
