package com.sap.cloud.environment.servicebinding.environment;

import com.sap.cloud.environment.servicebinding.SapServiceBindingsProperties.ServiceBindingProperties;

import java.util.HashMap;
import java.util.Map;

/*
* The {@link ServiceBindingsPropertiesAccessor} enables Java code not executed in a Spring context to access {@link SapServiceBindingsProperties}.
* The {@link SapServiceBindingsProperties} are fetched by the {@link ServiceBindingEnvironmentPostProcessor} and set in the {@link ServiceBindingsPropertiesAccessor}.
*/
public class SapServiceBindingsPropertiesAccessor
{
    /* A map of {@link SapServiceBindingsProperties}. Key is the name of the service. */
    private static Map<String, ServiceBindingProperties> serviceBindingsProperties = new HashMap<>();

    private SapServiceBindingsPropertiesAccessor()
    {
    }

    public static void setServiceBindingsProperties( Map<String, ServiceBindingProperties> serviceBindingsProperties )
    {
        SapServiceBindingsPropertiesAccessor.serviceBindingsProperties = serviceBindingsProperties;
    }

    public static Map<String, ServiceBindingProperties> getServiceBindingsProperties()
    {
        return SapServiceBindingsPropertiesAccessor.serviceBindingsProperties;
    }
}
