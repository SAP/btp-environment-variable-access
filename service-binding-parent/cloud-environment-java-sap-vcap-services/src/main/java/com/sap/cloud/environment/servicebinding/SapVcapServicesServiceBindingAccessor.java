/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.sap.cloud.environment.servicebinding;

import com.sun.istack.internal.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sap.cloud.environment.api.DefaultServiceBinding;
import com.sap.cloud.environment.api.ServiceBinding;
import com.sap.cloud.environment.api.ServiceBindingAccessor;
import com.sap.cloud.environment.api.exception.ServiceBindingAccessException;

public class SapVcapServicesServiceBindingAccessor implements ServiceBindingAccessor
{
    @Nonnull
    public static final Function<String, String> DEFAULT_ENVIRONMENT_VARIABLE_READER = System::getenv;

    @NotNull
    private static final String VCAP_SERVICES = "VCAP_SERVICES";

    @Nonnull
    private final Function<String, String> environmentVariableReader;

    public SapVcapServicesServiceBindingAccessor()
    {
        this(DEFAULT_ENVIRONMENT_VARIABLE_READER);
    }

    public SapVcapServicesServiceBindingAccessor( @Nonnull final Function<String, String> environmentVariableReader )
    {
        this.environmentVariableReader = environmentVariableReader;
    }

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings() throws ServiceBindingAccessException
    {
        final String vcapServices = environmentVariableReader.apply(VCAP_SERVICES);

        if (vcapServices == null) {
            return Collections.emptyList();
        }

        final JSONObject parsedVcapServices;
        try {
            parsedVcapServices = new JSONObject(vcapServices);
        } catch (final JSONException e) {
            return Collections.emptyList();
        }

        return parsedVcapServices.keySet()
                                 .stream()
                                 .flatMap(serviceName -> extractServiceBindings(parsedVcapServices,
                                                                                serviceName).stream())
                                 .collect(Collectors.toList());
    }

    @Nonnull
    private List<ServiceBinding> extractServiceBindings( @Nonnull final JSONObject vcapServices,
                                                         @Nonnull final String serviceName )
    {
        final JSONArray jsonServiceBindings;
        try {
            jsonServiceBindings = vcapServices.getJSONArray(serviceName);
        } catch (final JSONException e) {
            return Collections.emptyList();
        }

        final List<ServiceBinding> serviceBindings = new ArrayList<>(jsonServiceBindings.length());
        for (int i = 0; i < jsonServiceBindings.length(); ++i) {
            final JSONObject jsonServiceBinding;
            try {
                jsonServiceBinding = jsonServiceBindings.getJSONObject(i);
            } catch (final JSONException e) {
                continue;
            }

            serviceBindings.add(toServiceBinding(jsonServiceBinding, serviceName));
        }

        return serviceBindings;
    }

    @Nonnull
    private ServiceBinding toServiceBinding( @Nonnull final JSONObject jsonServiceBinding,
                                             @Nonnull final String serviceName )
    {
        return DefaultServiceBinding.builder()
                                    .copy(jsonServiceBinding.toMap())
                                    .withNameKey("name")
                                    .withServiceName(serviceName)
                                    .withServicePlanKey("plan")
                                    .withTagsKey("tags")
                                    .withCredentialsKey("credentials")
                                    .build();
    }
}
