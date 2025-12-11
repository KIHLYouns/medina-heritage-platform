package com.medina.heritage.integration.service;

import com.medina.heritage.integration.config.SalesforceOAuth2Response;
import com.medina.heritage.integration.config.SalesforceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Service for managing Salesforce OAuth2 authentication.
 * Handles token acquisition and refresh.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalesforceAuthService {

    private final SalesforceProperties salesforceProperties;
    private final RestTemplate restTemplate;

    private volatile String cachedAccessToken;
    private volatile long tokenExpiryTime;

    /**
     * Get a valid OAuth2 access token.
     * Uses cached token if valid, otherwise requests a new one.
     *
     * @return Bearer access token
     */
    public String getAccessToken() {
        // Check if cached token is still valid
        if (isCachedTokenValid()) {
            log.debug("Using cached Salesforce access token");
            return cachedAccessToken;
        }

        log.info("Requesting new Salesforce OAuth2 access token");
        return requestNewToken();
    }

    /**
     * Request a new OAuth2 access token from Salesforce.
     * Uses password grant flow (username + password).
     *
     * @return New access token
     */
    private String requestNewToken() {
        try {
            // Prepare OAuth2 request parameters
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", salesforceProperties.getClientId());
            body.add("client_secret", salesforceProperties.getClientSecret());
            body.add("username", salesforceProperties.getUsername());
            body.add("password", salesforceProperties.getPassword());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // Call Salesforce OAuth2 endpoint
            SalesforceOAuth2Response response = restTemplate.postForObject(
                    salesforceProperties.getTokenUrl(),
                    request,
                    SalesforceOAuth2Response.class
            );

            if (response == null || response.getAccessToken() == null) {
                throw new RuntimeException("Failed to obtain Salesforce access token: invalid response");
            }

            // Cache the token (valid for ~2 hours, refresh after 1.5 hours)
            cachedAccessToken = response.getAccessToken();
            tokenExpiryTime = System.currentTimeMillis() + (90 * 60 * 1000); // 90 minutes

            log.info("Successfully obtained new Salesforce access token");
            return cachedAccessToken;

        } catch (Exception e) {
            log.error("Failed to obtain Salesforce access token", e);
            throw new RuntimeException("OAuth2 authentication failed: " + e.getMessage(), e);
        }
    }

    /**
     * Check if the cached token is still valid.
     *
     * @return true if cached token exists and hasn't expired
     */
    private boolean isCachedTokenValid() {
        return cachedAccessToken != null && 
               System.currentTimeMillis() < tokenExpiryTime;
    }
}
