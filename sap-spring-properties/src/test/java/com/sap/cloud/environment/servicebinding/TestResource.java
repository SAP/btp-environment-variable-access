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
        mockEnvironment.setProperty("services.servicebindings.xsuaa.name", "xsuaa-test");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.plan", "broker");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.uaadomain", "localhost");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.clientId", "client");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.key", "encoded-key");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.credential-type", "x509");
        mockEnvironment.setProperty("services.servicebindings.service-manager.name", "service-manager-test");
        mockEnvironment
            .setProperty("services.servicebindings.service-manager.credentials.url", "https://localhost:8080");
        mockEnvironment
            .setProperty("services.servicebindings.service-manager.credentials.sm_url", "https://localhost:9785");
        mockEnvironment.setProperty("services.servicebindings.service-manager.credentials.clientId", "client");
        mockEnvironment.setProperty("services.servicebindings.service-manager.credentials.clientSecret", "secret");
        mockEnvironment.setProperty("services.servicebindings.service-manager.credentials.credential-type", "binding");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getEmptyProperties()
    {
        return new MockEnvironment();
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesNoName()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("services.servicebindings.xsuaa.plan", "broker");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.uaadomain", "localhost");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.clientId", "client");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.key", "encoded-key");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.credential-type", "x509");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesEmptyName()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("services.servicebindings.xsuaa.name", "");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.plan", "broker");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.uaadomain", "localhost");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.clientId", "client");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.key", "encoded-key");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.credentials.credential-type", "x509");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesNoCredentials()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("services.servicebindings.xsuaa.name", "xsuaa-test");
        mockEnvironment.setProperty("services.servicebindings.xsuaa.plan", "broker");

        return mockEnvironment;
    }
}
