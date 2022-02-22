package com.sap.cloud.environment.servicebinding;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sap.cloud.environment.api.DefaultServiceBinding;
import com.sap.cloud.environment.api.ServiceBinding;

public final class SecretKeyParsingStrategy implements ParsingStrategy
{
    @Nonnull
    private static final String PLAN_KEY = "plan";

    @Nonnull
    private final Charset charset;

    @Nonnull
    public static SecretKeyParsingStrategy newDefault()
    {
        return new SecretKeyParsingStrategy(StandardCharsets.UTF_8);
    }

    private SecretKeyParsingStrategy( @Nonnull final Charset charset )
    {
        this.charset = charset;
    }

    @Nullable
    @Override
    public ServiceBinding parse( @Nonnull final String serviceName, @Nonnull final String bindingName, @Nonnull final Path bindingPath ) throws IOException
    {
        final List<Path> propertyFiles = Files.list(bindingPath).filter(Files::isRegularFile).collect(Collectors.toList());

        if (propertyFiles.isEmpty()) {
            // service binding directory must contain at least one json file
            return null;
        }

        final Map<String, Object> rawServiceBinding = new HashMap<>();
        boolean credentialsFound = false;
        for (final Path propertyFile : propertyFiles) {
            final String propertyName = propertyFile.getFileName().toString();
            final String fileContent = String.join("\n", Files.readAllLines(propertyFile, charset));
            if (fileContent.isEmpty()) {
                continue;
            }

            try {
                final Map<String, Object> parsedCredentials = new JSONObject(fileContent).toMap();

                if (credentialsFound) {
                    // we expect exactly one valid json object in this service binding
                    return null;
                }

                credentialsFound = true;
                rawServiceBinding.put(PropertySetter.CREDENTIALS_KEY, parsedCredentials);
            } catch (final JSONException e) {
                // property is not a valid json object --> it cannot be the credentials object
                rawServiceBinding.put(propertyName, fileContent);
            }
        }

        if (!credentialsFound)
        {
            // the service binding is expected to have credentials attached to it
            return null;
        }

        return DefaultServiceBinding.builder()
                .copy(rawServiceBinding)
                .withNameResolver(any -> bindingName)
                .withServiceNameResolver(any -> serviceName)
                .withCredentialsKey(PropertySetter.CREDENTIALS_KEY)
                .withServicePlanKey(PLAN_KEY)
                .build();
    }
}
