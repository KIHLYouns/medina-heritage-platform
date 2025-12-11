package com.medina.heritage.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medina.heritage.integration.config.SalesforceProperties;
import com.medina.heritage.integration.dtos.request.SalesforceContactRequest;
import com.medina.heritage.integration.dtos.response.SalesforceUpsertResponse; // Import du DTO créé
import com.medina.heritage.integration.entity.IdMapping;
import com.medina.heritage.integration.repository.IdMappingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesforceContactSyncService {

    private final IdMappingRepository idMappingRepository;
    private final SalesforceAuthService authService;
    private final SalesforceProperties salesforceProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Synchronise un utilisateur vers Salesforce (Upsert).
     * Sauvegarde ensuite l'ID Salesforce reçu dans la base locale.
     */
    public void upsertContact(String userId, String email, String firstName,
            String lastName, String phoneNumber) {
        try {
            log.info("Starting Salesforce contact upsert for user: {} ({})", userId, email);

            // 1. Authentification
            String accessToken = authService.getAccessToken();
            log.debug("OAuth2 token obtained successfully");

            // 2. Construction du corps de la requête
            // Note: On n'inclut pas User_UUID__c dans le body car il est dans l'URL
            SalesforceContactRequest contactRequest = SalesforceContactRequest.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .mobilePhone(phoneNumber)
                    .accountId(salesforceProperties.getDefaultAccountId())
                    .build();

            // 3. Construction de l'URL avec l'ID externe
            String upsertUrl = buildUpsertUrl(userId);

            // 4. Préparation des headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");

            HttpEntity<SalesforceContactRequest> request = new HttpEntity<>(contactRequest, headers);

            // 5. Exécution de la requête PATCH
            // On mappe la réponse directement vers notre DTO SalesforceUpsertResponse
            ResponseEntity<SalesforceUpsertResponse> response = restTemplate.exchange(
                    upsertUrl,
                    HttpMethod.PATCH,
                    request,
                    SalesforceUpsertResponse.class);

            // 6. Traitement du succès et sauvegarde en BDD
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                SalesforceUpsertResponse sfResponse = response.getBody();

                if (sfResponse.isSuccess()) {
                    log.info("Successfully upserted Salesforce contact for user: {}", userId);

                    // --- ETAPE CRUCIALE : SAUVEGARDE DU MAPPING ---
                    // On extrait l'ID retourné par Salesforce (ex: 003g...)
                    saveIdMapping(userId, sfResponse.getId());

                } else {
                    log.warn("Salesforce returned 200 OK but success=false for user: {}", userId);
                }

            } else {
                log.error("Failed to upsert contact. Status: {}", response.getStatusCode());
                throw new RuntimeException("Salesforce upsert failed with status: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            log.error("Client error from Salesforce for user {}: {}", userId, e.getResponseBodyAsString());
            throw new RuntimeException("Client error from Salesforce: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            log.error("Server error from Salesforce: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Server error from Salesforce: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error upserting Salesforce contact for user: {}", userId, e);
            throw new RuntimeException("Failed to upsert contact: " + e.getMessage(), e);
        }
    }

    /**
     * Logique de sauvegarde ou mise à jour dans la table id_mappings.
     */
    private void saveIdMapping(String localUserIdStr, String sfId) {
        try {
            UUID localUserId = UUID.fromString(localUserIdStr);

            // Cherche un mapping existant ou en crée un nouveau (Builder pattern)
            IdMapping mapping = idMappingRepository
                    .findByLocalEntityTypeAndLocalEntityId("USER", localUserId)
                    .orElse(IdMapping.builder()
                            .localEntityType("USER")
                            .localEntityId(localUserId)
                            .build());

            // Mise à jour des champs
            mapping.setSfEntityId(sfId);
            mapping.setSyncStatus("SYNCED");
            mapping.setLastSyncAt(OffsetDateTime.now());

            // Enregistrement effectif en base
            idMappingRepository.save(mapping);
            log.info("Mapping saved in DB: User {} <-> SF ID {}", localUserId, sfId);

        } catch (Exception e) {
            // On log l'erreur DB mais on ne bloque pas le flux car l'envoi SF a réussi
            log.error("Failed to save ID mapping in database for user {}", localUserIdStr, e);
        }
    }

    private String buildUpsertUrl(String userId) {
        return String.format(
                "%s/services/data/%s/sobjects/Contact/User_UUID__c/%s",
                salesforceProperties.getInstanceUrl(),
                salesforceProperties.getApiVersion(),
                userId);
    }
}