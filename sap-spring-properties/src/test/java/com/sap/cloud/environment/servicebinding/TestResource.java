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
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.name", "xsuaa-test");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.plan", "broker");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.tags", "xsuaa, test-xsuaa");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.label", "xsuaa");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.uaadomain", "localhost");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.clientId", "client");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.key", "encoded-key");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.credential-type", "x509");
        mockEnvironment.setProperty("cds.servicebindings.service-manager.name", "service-manager-test");
        mockEnvironment.setProperty("cds.servicebindings.service-manager.tags", "test-service-manager");
        mockEnvironment.setProperty("cds.servicebindings.service-manager.label", "service-manager");
        mockEnvironment.setProperty("cds.servicebindings.service-manager.credentials.url", "https://localhost:8080");
        mockEnvironment.setProperty("cds.servicebindings.service-manager.credentials.sm_url", "https://localhost:9785");
        mockEnvironment.setProperty("cds.servicebindings.service-manager.credentials.clientId", "client");
        mockEnvironment.setProperty("cds.servicebindings.service-manager.credentials.clientSecret", "secret");
        mockEnvironment.setProperty("cds.servicebindings.service-manager.credentials.credential-type", "binding");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getEmptyProperties()
    {
        return new MockEnvironment();
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesNoLabel()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.name", "xsuaa-test");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.plan", "broker");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.tags", "xsuaa");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.uaadomain", "localhost");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.clientId", "client");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.key", "encoded-key");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.credential-type", "x509");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesEmptyLabel()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.name", "xsuaa-test");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.plan", "broker");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.tags", "xsuaa");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.label", "");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.uaadomain", "localhost");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.clientId", "client");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.key", "encoded-key");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.credential-type", "x509");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesNoTags()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.name", "xsuaa-test");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.plan", "broker");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.label", "xsuaa");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.uaadomain", "localhost");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.clientId", "client");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.key", "encoded-key");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.credential-type", "x509");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesEmptyTags()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.name", "xsuaa-test");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.plan", "broker");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.label", "xsuaa");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.tags", "");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.uaadomain", "localhost");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.clientId", "client");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.key", "encoded-key");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.credential-type", "x509");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesNoName()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.plan", "broker");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.label", "xsuaa");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.tags", "xsuaa");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.uaadomain", "localhost");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.clientId", "client");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.key", "encoded-key");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.credential-type", "x509");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesEmptyName()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.name", "");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.plan", "broker");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.label", "xsuaa");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.tags", "xsuaa");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.uaadomain", "localhost");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certUrl", "https://localhost:8080");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.clientId", "client");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.key", "encoded-key");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.certificate", "encoded-certificate");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.credentials.credential-type", "x509");

        return mockEnvironment;
    }

    @Nonnull
    public static MockEnvironment getInvalidPropertiesNoCredentials()
    {
        final MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.name", "xsuaa-test");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.plan", "broker");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.label", "xsuaa");
        mockEnvironment.setProperty("cds.servicebindings.xsuaa.tags", "xsuaa");

        return mockEnvironment;
    }
}
