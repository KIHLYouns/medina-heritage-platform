package com.medina.heritage.integration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Salesforce OAuth2 and API access.
 * Binds to application.properties with prefix 'salesforce'.
 */
@Data
@Component
@ConfigurationProperties(prefix = "salesforce")
public class SalesforceProperties {

    /**
     * OAuth2 Token endpoint URL
     */
    private String tokenUrl;

    /**
     * Salesforce API base URL (e.g., https://instance.salesforce.com)
     */
    private String instanceUrl;

    /**
     * Salesforce API version (e.g., v59.0)
     */
    private String apiVersion = "v59.0";

    /**
     * OAuth2 Client ID
     */
    private String clientId;

    /**
     * OAuth2 Client Secret
     */
    private String clientSecret;

    /**
     * Salesforce username for OAuth2 password grant
     */
    private String username;

    /**
     * Salesforce password/security token for OAuth2 password grant
     */
    private String password;

    /**
     * Default Citizens account ID in Salesforce
     */
    private String defaultAccountId;
}
