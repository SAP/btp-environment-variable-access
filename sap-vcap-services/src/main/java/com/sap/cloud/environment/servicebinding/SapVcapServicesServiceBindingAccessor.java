/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import com.sap.cloud.environment.servicebinding.api.ServiceIdentifier;
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
 * structure. Please refer to
 * <a href="https://docs.cloudfoundry.org/devguide/deploy-apps/environment-variable.html#VCAP-SERVICES">the official
 * documentation</a> for more details about the {@code VCAP_SERVICES} structure.
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
        final Object serviceBindings = vcapServices.get(serviceName);
        if( !(serviceBindings instanceof JSONArray) ) {
            logger.debug("Skipping '{}': Unexpected format.", serviceName);
            return Collections.emptyList();
        }

        final List<ServiceBinding> result =
            StreamSupport
                .stream(((Iterable<?>) serviceBindings).spliterator(), false)
                .filter(JSONObject.class::isInstance)
                .flatMap(binding -> toServiceBindings(((JSONObject) binding).toMap(), serviceName).stream())
                .collect(Collectors.toList());

        logger.debug("Successfully read {} service binding(s) from '{}'.", result.size(), serviceName);
        return result;
    }

    @Nonnull
    protected
        Collection<ServiceBinding>
        toServiceBindings( @Nonnull final Map<String, Object> props, @Nonnull final String serviceName )
    {
        if( isUserProvided(serviceName) ) {
            final List<String> tags = getTagsFromProperties(props);
            return tags.stream().map(tag -> createServiceBinding(props, serviceName, tag)).collect(Collectors.toList());
        } else {
            return Collections.singleton(createServiceBinding(props, serviceName, serviceName));
        }
    }

    protected boolean isUserProvided( @Nonnull final String serviceName )
    {
        return "user-provided".equalsIgnoreCase(serviceName);
    }

    @SuppressWarnings( "unchecked" )
    @Nonnull
    protected List<String> getTagsFromProperties( @Nonnull final Map<String, Object> properties )
    {
        final Object tags = properties.get("tags");
        if( !(tags instanceof List) ) {
            logger.debug("No \"tags\" found in service binding {}.", tags);
            return Collections.emptyList();
        }
        if( ((List<?>) tags).isEmpty() || !(((List<?>) tags).get(0) instanceof String) ) {
            logger.debug("Empty or unexpected format for \"tags\" in service binding {}.", tags);
            return Collections.emptyList();
        }
        return (List<String>) tags;
    }

    @Nonnull
    protected ServiceBinding createServiceBinding(
        @Nonnull final Map<String, Object> properties,
        @Nonnull final String serviceName,
        @Nonnull final String serviceIdentifier )
    {
        return DefaultServiceBinding
            .builder()
            .copy(properties)
            .withNameKey("name")
            .withServiceName(serviceName)
            .withServicePlanKey("plan")
            .withTagsKey("tags")
            .withCredentialsKey("credentials")
            .withServiceIdentifier(ServiceIdentifier.of(serviceIdentifier))
            .build();
    }
}
