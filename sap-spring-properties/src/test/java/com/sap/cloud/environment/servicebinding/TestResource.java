package com.sap.cloud.environment.servicebinding;

import javax.annotation.Nonnull;

import org.springframework.mock.env.MockEnvironment;

public final class TestResource
{
    private TestResource()
    {
        throw new IllegalStateException("This utility class must not be initialized.");
    }

    @Nonnull
    public static MockEnvironment getAllBindingsProperties()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.serviceName", "xsuaa");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.plan", "broker");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.credentials.uaadomain", "localhost");
        mockEnvironment
            .setProperty("services.servicebindings.xsuaa-test.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.credentials.clientId", "client");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.credentials.key", "encoded-key");
        mockEnvironment
            .setProperty("services.servicebindings.xsuaa-test.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.credentials.credential-type", "x509");
        mockEnvironment.setProperty("services.servicebindings.service-manager-test.serviceName", "service-manager");
        mockEnvironment
            .setProperty("services.servicebindings.service-manager-test.credentials.url", "https://localhost:8080");
        mockEnvironment
            .setProperty("services.servicebindings.service-manager-test.credentials.sm_url", "https://localhost:9785");
        mockEnvironment.setProperty("services.servicebindings.service-manager-test.credentials.clientId", "client");
        mockEnvironment.setProperty("services.servicebindings.service-manager-test.credentials.clientSecret", "secret");
        mockEnvironment
            .setProperty("services.servicebindings.service-manager-test.credentials.credential-type", "binding");
        mockEnvironment.setProperty("services.servicebindings.service-manager-test2.name", "service_manager_test2");
        mockEnvironment.setProperty("services.servicebindings.service-manager-test2.serviceName", "service-manager");
        mockEnvironment
            .setProperty("services.servicebindings.service-manager-test2.credentials.url", "https://localhost:8081");
        mockEnvironment
            .setProperty("services.servicebindings.service-manager-test2.credentials.sm_url", "https://localhost:9785");
        mockEnvironment.setProperty("services.servicebindings.service-manager-test2.credentials.clientId", "client");
        mockEnvironment
            .setProperty("services.servicebindings.service-manager-test2.credentials.clientSecret", "secret");
        mockEnvironment
            .setProperty("services.servicebindings.service-manager-test2.credentials.credential-type", "binding");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getEmptyProperties()
    {
        return new MockEnvironment();
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesNoServiceName()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.plan", "broker");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.credentials.uaadomain", "localhost");
        mockEnvironment
            .setProperty("services.servicebindings.xsuaa-test.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.credentials.clientId", "client");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.credentials.key", "encoded-key");
        mockEnvironment
            .setProperty("services.servicebindings.xsuaa-test.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.credentials.credential-type", "x509");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesEmptyName()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.name", "");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.plan", "broker");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.credentials.uaadomain", "localhost");
        mockEnvironment
            .setProperty("services.servicebindings.xsuaa-test.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.credentials.clientId", "client");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.credentials.key", "encoded-key");
        mockEnvironment
            .setProperty("services.servicebindings.xsuaa-test.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.credentials.credential-type", "x509");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesNoCredentials()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.name", "xsuaa-test");
        mockEnvironment.setProperty("services.servicebindings.xsuaa-test.plan", "broker");

        return mockEnvironment;
    }
}
