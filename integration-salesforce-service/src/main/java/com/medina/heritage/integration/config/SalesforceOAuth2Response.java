package com.medina.heritage.integration.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2 token response from Salesforce.
 * Used to store the Bearer token for API calls.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesforceOAuth2Response {

    /**
     * OAuth2 access token for API calls
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Token type (typically "Bearer")
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * Salesforce instance URL
     */
    @JsonProperty("instance_url")
    private String instanceUrl;

    /**
     * OAuth2 authorization ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * Token scope
     */
    @JsonProperty("scope")
    private String scope;
}
