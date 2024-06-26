package com.sap.cloud.environment.servicebinding.api;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an <b>equatable</b> service identifier that can be used across applications.
 */
public final class ServiceIdentifier
{
    @Nonnull
    private static final Map<String, ServiceIdentifier> INSTANCES = new ConcurrentHashMap<>();

    /**
     * Represents the <a href=
     * "https://help.sap.com/docs/btp/sap-business-technology-platform/what-is-sap-authorization-and-trust-management-service">SAP
     * Extended Service for User and Account Authentication (XSUAA)</a>.
     */
    @Nonnull
    public static final ServiceIdentifier XSUAA = of("xsuaa");

    /**
     * Represents the <a href="https://api.sap.com/api/SAP_CP_CF_Connectivity_Destination/overview">SAP Destination
     * Service (Cloud Foundry)</a>.
     */
    @Nonnull
    public static final ServiceIdentifier DESTINATION = of("destination");

    /**
     * Represents the <a href=
     * "https://help.sap.com/docs/connectivity/sap-btp-connectivity-cf/what-is-sap-btp-connectivity?locale=en-US">SAP
     * Connectivity Service (Cloud Foundry)</a>. This is the proxy service that enables applications to access
     * On-Premise systems from the SAP Business Technology Platform (Cloud Foundry).
     */
    @Nonnull
    public static final ServiceIdentifier CONNECTIVITY = of("connectivity");

    /**
     * Represents the <a href="https://api.sap.com/api/CFAuditLogRetrievalAPI/overview">SAP Audit Log Retrieval (Cloud
     * Foundry environment)</a> service.
     */
    @Nonnull
    public static final ServiceIdentifier AUDIT_LOG_RETRIEVAL = of("auditlog-management");

    /**
     * Represents the <a href="https://api.sap.com/package/SAPCPWorkflowAPIs/all">SAP Workflow Service for Cloud
     * Foundry</a>.
     */
    @Nonnull
    public static final ServiceIdentifier WORKFLOW = of("workflow");

    /**
     * Represents the <a href="https://api.sap.com/package/SAPCPBusinessRulesAPIs/all">SAP Business Rules Service for
     * Cloud Foundry</a>.
     */
    @Nonnull
    public static final ServiceIdentifier BUSINESS_RULES = of("business-rules");

    /**
     * Represents the <a href="https://help.sap.com/docs/identity-authentication">SAP Identity Authentication
     * Service</a> (also known as <i><b>IAS</b></i>).
     */
    @Nonnull
    public static final ServiceIdentifier IDENTITY_AUTHENTICATION = of("identity");

    /**
     * Represents the <a href="https://api.sap.com/api/AI_CORE_API">SAP AI Core Service</a>.
     */
    @Nonnull
    public static final ServiceIdentifier AI_CORE = of("aicore");

    /**
     * Returns an {@link ServiceIdentifier} based on the provided {@code id}.
     * <p>
     * The {@code id} will be modified using {@link String#trim()} and {@link String#toLowerCase()}.
     * </p>
     *
     * @param id
     *            The identifier to use.
     * @return An instance of {@link ServiceIdentifier}.
     * @throws IllegalArgumentException
     *             If the provided {@code id} is empty or blank <b>after</b> it has been modified using
     *             {@link String#trim()}.
     */
    @Nonnull
    public static ServiceIdentifier of( @Nonnull final String id )
    {
        final String trimmedId = id.trim().toLowerCase(Locale.ROOT);
        if( trimmedId.isEmpty() ) {
            throw new IllegalArgumentException(String.format("The provided id ('%s') must not be empty or blank.", id));
        }

        return INSTANCES.computeIfAbsent(trimmedId, ServiceIdentifier::new);
    }

    @Nonnull
    private final String id;

    private ServiceIdentifier( @Nonnull final String id )
    {
        this.id = id;
    }

    @Override
    public String toString()
    {
        return id;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        return obj instanceof ServiceIdentifier && id.equals(((ServiceIdentifier) obj).id);
    }
}
