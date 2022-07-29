/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.environment.servicebinding.api.DefaultServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBinding;
import com.sap.cloud.environment.servicebinding.api.ServiceBindingAccessor;
import com.sap.cloud.environment.servicebinding.api.exception.ServiceBindingAccessException;

/**
 * A {@link ServiceBindingAccessor} that is able to load {@link ServiceBinding}s from SAP's {@code VCAP_SERVICES}
 * structure.
 */
public class SapVcapServicesServiceBindingAccessor implements ServiceBindingAccessor
{
    @Nonnull
    private static final Logger logger = LoggerFactory.getLogger(SapVcapServicesServiceBindingAccessor.class);

    /**
     * The default {@link Function} to read environment variables.
     */
    @Nonnull
    public static final Function<String, String> DEFAULT_ENVIRONMENT_VARIABLE_READER = System::getenv;

    @Nonnull
    private static final String VCAP_SERVICES = "VCAP_SERVICES";

    @Nonnull
    private final Function<String, String> environmentVariableReader;

    /**
     * Initializes a new {@link SapVcapServicesServiceBindingAccessor} instance that uses the
     * {@link #DEFAULT_ENVIRONMENT_VARIABLE_READER}.
     */
    public SapVcapServicesServiceBindingAccessor()
    {
        this(DEFAULT_ENVIRONMENT_VARIABLE_READER);
    }

    /**
     * Initializes a new {@link SapVcapServicesServiceBindingAccessor} instance that uses the given
     * {@code environmentVariableReader}.
     *
     * @param environmentVariableReader
     *            The {@link Function} that should be used to read environment variables.
     */
    public SapVcapServicesServiceBindingAccessor( @Nonnull final Function<String, String> environmentVariableReader )
    {
        this.environmentVariableReader = environmentVariableReader;
    }

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings()
        throws ServiceBindingAccessException
    {
        logger.debug("Trying to determine service bindings using the '{}' environment variable.", VCAP_SERVICES);
        final String vcapServices = environmentVariableReader.apply(VCAP_SERVICES);

        if( vcapServices == null ) {
            logger.debug("Environment variable '{}' is not defined.", VCAP_SERVICES);
            return Collections.emptyList();
        }

        final JSONObject parsedVcapServices;
        try {
            parsedVcapServices = new JSONObject(vcapServices);
        }
        catch( final JSONException e ) {
            logger.debug("Environment variable '{}' ('{}') is not a valid JSON.", VCAP_SERVICES, vcapServices);
            return Collections.emptyList();
        }

        return parsedVcapServices
            .keySet()
            .stream()
            .flatMap(serviceName -> extractServiceBindings(parsedVcapServices, serviceName).stream())
            .collect(Collectors.toList());
    }

    @Nonnull
    private
        List<ServiceBinding>
        extractServiceBindings( @Nonnull final JSONObject vcapServices, @Nonnull final String serviceName )
    {
        final JSONArray jsonServiceBindings;
        try {
            jsonServiceBindings = vcapServices.getJSONArray(serviceName);
        }
        catch( final JSONException e ) {
            logger.debug("Skipping '{}': Unexpected format.", VCAP_SERVICES);
            return Collections.emptyList();
        }

        final List<ServiceBinding> serviceBindings = new ArrayList<>(jsonServiceBindings.length());
        for( int i = 0; i < jsonServiceBindings.length(); ++i ) {
            final JSONObject jsonServiceBinding;
            try {
                jsonServiceBinding = jsonServiceBindings.getJSONObject(i);
            }
            catch( final JSONException e ) {
                continue;
            }

            serviceBindings.add(toServiceBinding(jsonServiceBinding, serviceName));
        }

        logger.debug("Successfully read {} service binding(s) from '{}'.", serviceBindings.size(), VCAP_SERVICES);
        return serviceBindings;
    }

    @Nonnull
    private
        ServiceBinding
        toServiceBinding( @Nonnull final JSONObject jsonServiceBinding, @Nonnull final String serviceName )
    {
        return DefaultServiceBinding
            .builder()
            .copy(jsonServiceBinding.toMap())
            .withNameKey("name")
            .withServiceName(serviceName)
            .withServicePlanKey("plan")
            .withTagsKey("tags")
            .withCredentialsKey("credentials")
            .build();
    }
}
