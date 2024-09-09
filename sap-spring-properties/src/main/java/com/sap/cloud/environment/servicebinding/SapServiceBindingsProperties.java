package com.sap.cloud.environment.servicebinding;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Properties of service bindings defined in a properties file such as Spring's default file 'application.properties'.
 */
public class SapServiceBindingsProperties {
    /**
     * default plan name
     */
    private static final String BINDING_PLAN = "standard";

    /**
     * properties of configured service bindings
     */
    private Map<String, ServiceBindingProperties> serviceBindings;

    public Map<String, ServiceBindingProperties> getServiceBindings() {
        return serviceBindings;
    }

    public void setServiceBindings(Map<String, ServiceBindingProperties> serviceBindings) {
        this.serviceBindings = serviceBindings;
    }

    public static class ServiceBindingProperties {
        /**
         * properties for credentials
         */
        private Map<String, Object> credentials;

        /**
         * label of the service binding
         */
        private String label;

        /**
         * tags of the service binding
         */
        private String[] tags;

        /**
         * name of the service binding
         */
        private String name;

        /**
         * name of the service
         */
        private String serviceName;

        /**
         * name of the instance
         */
        private String instanceName;

        /**
         * plan used to create the service binding
         */
        private String plan = BINDING_PLAN;

        public Map<String, Object> getCredentials() {
            if (credentials == null) {
                return Collections.emptyMap();
            } else {
                /* the key names are converted to lower-case */
                return credentials
                        .entrySet()
                        .stream()
                        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey().toLowerCase(), entry.getValue()))
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
            }
        }

        public void setCredentials(Map<String, Object> credentials) {
            this.credentials = credentials;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String[] getTags() {
            return tags;
        }

        public void setTags(String[] tags) {
            this.tags = tags;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getInstanceName() {
            return instanceName;
        }

        public void setInstanceName(String instanceName) {
            this.instanceName = instanceName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getPlan() {
            return plan;
        }

        public void setPlan(String plan) {
            this.plan = plan;
        }
    }
}
