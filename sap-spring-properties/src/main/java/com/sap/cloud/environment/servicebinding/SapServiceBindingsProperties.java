package com.sap.cloud.environment.servicebinding;

import java.util.HashMap;
import java.util.Map;

/**
 * Properties of service bindings defined in a properties file such as Spring's default file 'application.properties'.
 */
public class SapServiceBindingsProperties
{
    /**
     * properties of configured service bindings
     */
    private Map<String, ServiceBindingProperties> serviceBindings;

    public Map<String, ServiceBindingProperties> getServiceBindings()
    {
        return serviceBindings;
    }

    public void setServiceBindings( Map<String, ServiceBindingProperties> serviceBindings )
    {
        this.serviceBindings = serviceBindings;
    }

    public static class ServiceBindingProperties
    {
        /**
         * name of the service binding (optional)
         */
        private String name;

        /**
         * name of the service
         */
        private String serviceName;

        /**
         * default plan used to create the service binding
         */
        private String plan = "standard";

        /**
         * tags of the service binding
         */
        private String[] tags = new String[0];

        /**
         * properties for credentials
         */
        private Map<String, Object> credentials = new HashMap<>();

        public String getName()
        {
            return name;
        }

        public void setName( String name )
        {
            this.name = name;
        }

        public Map<String, Object> getCredentials()
        {
            return credentials;
        }

        public void setCredentials( Map<String, Object> credentials )
        {
            this.credentials = credentials;
        }

        public String[] getTags()
        {
            return tags;
        }

        public void setTags( String[] tags )
        {
            this.tags = tags;
        }

        public String getServiceName()
        {
            return serviceName;
        }

        public void setServiceName( String serviceName )
        {
            this.serviceName = serviceName;
        }

        public String getPlan()
        {
            return plan;
        }

        public void setPlan( String plan )
        {
            this.plan = plan;
        }
    }
}
