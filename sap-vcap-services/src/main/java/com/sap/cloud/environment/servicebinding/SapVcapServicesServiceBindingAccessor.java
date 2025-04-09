package com.sap.cloud.environment.servicebinding;

import java.util.ArrayList;
import java.io.IOException;
import java.util.Collections;
import java.nio.file.Files;
import java.util.List;
import java.nio.file.Path;
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
 * structure. Please refer to
 * <a href="https://docs.cloudfoundry.org/devguide/deploy-apps/environment-variable.html#VCAP-SERVICES">the official
 * documentation</a> for more details about the {@code VCAP_SERVICES} structure.
 */
public class SapVcapServicesServiceBindingAccessor implements ServiceBindingAccessor
{
    @Nonnull
    private static final Logger logger = LoggerFactory.getLogger(SapVcapServicesServiceBindingAccessor.class);

    @Nonnull
    private static final String VCAP_SERVICES = "VCAP_SERVICES";

    @Nonnull
    private static final String VCAP_SERVICES_FILE_PATH = "VCAP_SERVICES_FILE_PATH";

    /**
     * The default {@link Function} to read environment variables.
     */
    @Nonnull
    public static final Function<String, String> DEFAULT_ENVIRONMENT_VARIABLE_READER = System::getenv;

    @Nonnull
    public static final Function<Path, String> DEFAULT_FILE_READER = path -> {
        try {
            return Files.readString(path);
        }
        catch( IOException e ) {
            logger.error("Failed to read VCAP_SERVICES from file: {}", VCAP_SERVICES_FILE_PATH, e);
            return null;
        }
    };

    @Nonnull
    private final Function<String, String> environmentVariableReader;

    @Nonnull
    private final Function<Path, String> fileReader;

    /**
     * Initializes a new {@link SapVcapServicesServiceBindingAccessor} instance that uses the
     * {@link #DEFAULT_ENVIRONMENT_VARIABLE_READER} and {@link #DEFAULT_FILE_READER}.
     */
    public SapVcapServicesServiceBindingAccessor()
    {
        this(DEFAULT_ENVIRONMENT_VARIABLE_READER, DEFAULT_FILE_READER);
    }

    /**
     * Initializes a new {@link SapVcapServicesServiceBindingAccessor} instance that uses the given
     * {@code environmentVariableReader} and {@code fileReader}.
     *
     * @param environmentVariableReader
     *            The {@link Function} that should be used to read environment variables.
     * @param fileReader
     *            The {@link Function} that should be used to read files.
     */
    public SapVcapServicesServiceBindingAccessor(
        @Nonnull final Function<String, String> environmentVariableReader,
        @Nonnull final Function<Path, String> fileReader )
    {
        this.environmentVariableReader = environmentVariableReader;
        this.fileReader = fileReader;
    }

    /**
     * Initializes a new {@link SapVcapServicesServiceBindingAccessor} instance that uses the given
     * {@code environmentVariableReader} and the default {@link #DEFAULT_FILE_READER}.
     *
     * @param environmentVariableReader
     *            The {@link Function} that should be used to read environment variables.
     */
    public SapVcapServicesServiceBindingAccessor( @Nonnull final Function<String, String> environmentVariableReader )
    {
        this.environmentVariableReader = environmentVariableReader;
        this.fileReader = DEFAULT_FILE_READER;
    }

    @Nonnull
    @Override
    public List<ServiceBinding> getServiceBindings()
        throws ServiceBindingAccessException
    {
        logger
            .debug(
                "Trying to determine service bindings using the '{}' and '{}' environment variables.",
                VCAP_SERVICES,
                VCAP_SERVICES_FILE_PATH);
        String vcapServices = environmentVariableReader.apply(VCAP_SERVICES);
        final String vcapServicesFilePath = environmentVariableReader.apply(VCAP_SERVICES_FILE_PATH);

        if( vcapServices == null && vcapServicesFilePath != null ) {
            logger
                .debug(
                    "Environment variable '{}' is not defined. Falling back to environment variable '{}'",
                    VCAP_SERVICES,
                    VCAP_SERVICES_FILE_PATH);

            vcapServices = fileReader.apply(Path.of(vcapServicesFilePath));
            if( vcapServices == null ) {
                return Collections.emptyList(); // File is empty or failed to read the file
            }
        }

        if( vcapServices == null && vcapServicesFilePath == null ) {
            logger.debug("Environment variable '{}' and '{}' are not defined.", VCAP_SERVICES, VCAP_SERVICES_FILE_PATH);
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
