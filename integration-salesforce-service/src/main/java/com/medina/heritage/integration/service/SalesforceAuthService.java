package com.medina.heritage.integration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service pour gérer l'authentification OAuth2 avec Salesforce.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceAuthService {

    private final RestTemplate restTemplate;

    @Value("${salesforce.client-id}")
    private String clientId;

    @Value("${salesforce.client-secret}")
    private String clientSecret;

    @Value("${salesforce.username}")
    private String username;

    @Value("${salesforce.password}")
    private String password;

    @Value("${salesforce.auth-url:https://login.salesforce.com/services/oauth2/token}")
    private String authUrl;

    private String cachedAccessToken;
    private long tokenExpiryTime;

    /**
     * Obtient un access token Salesforce (avec cache).
     */
    public String getAccessToken() {
        // Vérifier si le token en cache est encore valide
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return cachedAccessToken;
        }

        // Sinon, obtenir un nouveau token
        return refreshAccessToken();
    }

    /**
     * Rafraîchit le token d'accès via OAuth2 Password Flow.
     */
    private String refreshAccessToken() {
        try {
            log.info("Requesting new Salesforce access token...");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("username", username);
            body.add("password", password);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                authUrl,
                HttpMethod.POST,
                request,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            cachedAccessToken = (String) responseBody.get("access_token");

            // Token expire généralement après 2 heures, on le cache pour 1h30
            tokenExpiryTime = System.currentTimeMillis() + (90 * 60 * 1000);

            log.info("Salesforce access token obtained successfully");
            return cachedAccessToken;

        } catch (HttpClientErrorException httpEx) {
            String resp = httpEx.getResponseBodyAsString();
            log.error("Salesforce HTTP error while obtaining access token: status={}, response={}", httpEx.getStatusCode(), resp);
            // Try to extract helpful error fields if present
            String message = "Failed to authenticate with Salesforce: " + resp;
            throw new RuntimeException(message, httpEx);
        } catch (Exception e) {
            log.error("Error obtaining Salesforce access token", e);
            throw new RuntimeException("Failed to authenticate with Salesforce", e);
        }
    }
}
