package com.sap.cloud.environment.servicebinding;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Properties of service bindings defined in a properties file such as Spring's default file 'application.properties'.
 *
 * @param serviceBindings properties of configured service bindings
 */
public record SapServiceBindingsProperties(
        Map<String, ServiceBindingProperties> serviceBindings
) {
    /**
     * properties of a service binding
     *
     * @param name name of the service binding (optional)
     * @param serviceName name of the service
     * @param plan default plan used to create the service binding
     * @param tags tags of the service binding
     * @param credentials properties for credentials
     */
    public record ServiceBindingProperties(
            String name,
            String serviceName,
            String plan,
            String[] tags,
            Map<String, Object> credentials
    )
    {
        public ServiceBindingProperties(String name,
            String serviceName,
            String plan,
            String[] tags,
            Map<String, Object> credentials) {
            this.name = name;
            this.serviceName = serviceName;
            this.plan = StringUtils.hasText(plan) ? plan : "standard";
            this.tags = (tags == null || tags.length == 0) ? new String[0] : tags;
            this.credentials = (credentials == null || credentials.isEmpty()) ? new HashMap<>() : credentials;
    }
    }
}