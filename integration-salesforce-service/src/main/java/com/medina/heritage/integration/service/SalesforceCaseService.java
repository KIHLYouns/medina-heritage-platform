package com.medina.heritage.integration.service;

import com.medina.heritage.integration.service.SalesforceLocationService.LocationInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour créer et gérer les Cases dans Salesforce.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceCaseService {

    private final SalesforceAuthService authService;
    private final SalesforceLocationService locationService;
    private final RestTemplate restTemplate;

    @Value("${salesforce.instance-url}")
    private String instanceUrl;

    @Value("${salesforce.api-version:v64.0}")
    private String apiVersion;

    /**
     * Crée un Case dans Salesforce avec toutes les informations nécessaires.
     * Crée également une Location avec les coordonnées GPS et la lie au Case.
     *
     * @param contactId     ID Salesforce du Contact (citoyen)
     * @param assetId       ID Salesforce de l'Asset (bâtiment)
     * @param buildingCode  Code du bâtiment (ex: BLDG-00123)
     * @param buildingName  Nom du bâtiment
     * @param imageUrl      URL S3 de l'image
     * @param description   Description fournie par le citoyen
     * @param longitude     Longitude GPS
     * @param latitude      Latitude GPS
     * @return L'ID du Case créé dans Salesforce
     */
    public String createCase(
            String contactId,
            String assetId,
            String buildingCode,
            String buildingName,
            String imageUrl,
            String description,
            Double longitude,
            Double latitude
    ) {
        try {
            log.info("Creating Case in Salesforce for building: {}", buildingCode);

            // 1. Créer d'abord la Location avec les coordonnées GPS
            LocationInfo locationInfo = null;
            if (latitude != null && longitude != null) {
                log.info("Creating Location with coordinates: ({}, {})", latitude, longitude);
                locationInfo = locationService.createLocationAndGetInfo(
                    buildingCode,
                    buildingName,
                    latitude,
                    longitude
                );
                log.info("Location created successfully: {}", locationInfo.locationId);
            }

            // 2. Construire le payload JSON du Case (incluant le lien vers la Location)
            Map<String, Object> casePayload = buildCasePayload(
                contactId,
                assetId,
                buildingCode,
                buildingName,
                imageUrl,
                description,
                longitude,
                latitude,
                locationInfo != null ? locationInfo.locationId : null
            );

            // 3. Obtenir le token d'authentification
            String accessToken = authService.getAccessToken();

            // 4. Préparer la requête HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");

            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Case";

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(casePayload, headers);

            // 5. Envoyer la requête POST à Salesforce
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
            );

            // 6. Extraire l'ID du Case créé
            Map<String, Object> responseBody = response.getBody();
            String caseId = (String) responseBody.get("id");

            log.info("Case created successfully: {} (with Location: {})", 
                    caseId, 
                    locationInfo != null ? locationInfo.locationId : "N/A");
            return caseId;

        } catch (Exception e) {
            log.error("Error creating Case in Salesforce", e);
            throw new RuntimeException("Failed to create Case in Salesforce", e);
        }
    }

    /**
     * Construit le payload JSON pour créer un Case dans Salesforce.
     */
    private Map<String, Object> buildCasePayload(
            String contactId,
            String assetId,
            String buildingCode,
            String buildingName,
            String imageUrl,
            String description,
            Double longitude,
            Double latitude,
            String locationId
    ) {
        Map<String, Object> payload = new HashMap<>();

        // Champs standard
        payload.put("Subject", "Signalement Citoyen - " + buildingCode);
        payload.put("Origin", "Smart City App");

        // Relations (Lookups)
        if (contactId != null) {
            payload.put("ContactId", contactId);
        }
        if (assetId != null) {
            payload.put("AssetId", assetId);
        }

        // Liaison vers la Location (coordonnées GPS)
        if (locationId != null) {
            payload.put("Location__c", locationId); // Custom field pour lier à Location
        }

        // Description enrichie
        StringBuilder fullDescription = new StringBuilder();
        fullDescription.append("Description du citoyen: ").append(description);

        payload.put("Description", fullDescription.toString());

        // Champs personnalisés (Custom Fields)
        // Note: Ces champs doivent exister dans votre org Salesforce
        
        if (imageUrl != null) {
            payload.put("Images__c", imageUrl);
        }

        log.info("Case payload: {}", payload);
        return payload;
    }

    /**
     * Crée un Case dans Salesforce pour une alerte IoT (dépassement de seuil).
     *
     * @param assetId       ID Salesforce de l'Asset (bâtiment) - peut être null
     * @param deviceSerialNumber  Numéro de série du capteur
     * @param metricType    Type de métrique (VIBRATION, HUMIDITY, etc.)
     * @param value         Valeur mesurée
     * @param unit          Unité de mesure
     * @param severityLevel Niveau de sévérité (WARNING, CRITICAL)
     * @param thresholdMin  Seuil minimum
     * @param thresholdMax  Seuil maximum
     * @param breachDirection Direction du dépassement (ABOVE_MAX, BELOW_MIN)
     * @param description   Description de l'alerte
     * @return L'ID du Case créé dans Salesforce
     */
    public String createIotRiskCase(
            String assetId,
            String deviceSerialNumber,
            String metricType,
            java.math.BigDecimal value,
            String unit,
            String severityLevel,
            java.math.BigDecimal thresholdMin,
            java.math.BigDecimal thresholdMax,
            String breachDirection,
            String description
    ) {
        try {
            log.info("Creating IoT Risk Case in Salesforce for device: {}", deviceSerialNumber);

            // Construire le payload JSON du Case
            Map<String, Object> casePayload = new HashMap<>();

            // Sujet du Case
            String subject = String.format("Alerte IoT - %s (%s) - %s", 
                metricType, deviceSerialNumber, severityLevel);
            casePayload.put("Subject", subject);
            casePayload.put("Origin", "IoT Service");

            // Relation vers l'Asset si disponible
            if (assetId != null) {
                casePayload.put("AssetId", assetId);
            }

            // Description enrichie avec toutes les informations
            StringBuilder fullDescription = new StringBuilder();
            fullDescription.append("Alerte générée par le capteur IoT\n\n");
            fullDescription.append("Type de capteur: ").append(metricType).append("\n");
            fullDescription.append("Numéro de série: ").append(deviceSerialNumber).append("\n");
            fullDescription.append("Valeur mesurée: ").append(value).append(" ").append(unit).append("\n");
            fullDescription.append("Niveau de sévérité: ").append(severityLevel).append("\n");
            fullDescription.append("Direction du dépassement: ").append(breachDirection).append("\n");
            
            if (thresholdMin != null) {
                fullDescription.append("Seuil minimum: ").append(thresholdMin).append(" ").append(unit).append("\n");
            }
            if (thresholdMax != null) {
                fullDescription.append("Seuil maximum: ").append(thresholdMax).append(" ").append(unit).append("\n");
            }
            
            if (description != null && !description.isBlank()) {
                fullDescription.append("\nDescription: ").append(description);
            }

            casePayload.put("Description", fullDescription.toString());

            // Priorité basée sur la sévérité
            if ("CRITICAL".equalsIgnoreCase(severityLevel)) {
                casePayload.put("Priority", "High");
            } else {
                casePayload.put("Priority", "Medium");
            }

            // Obtenir le token d'authentification
            String accessToken = authService.getAccessToken();

            // Préparer la requête HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");

            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Case";

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(casePayload, headers);

            // Envoyer la requête POST à Salesforce
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
            );

            // Extraire l'ID du Case créé
            Map<String, Object> responseBody = response.getBody();
            String caseId = (String) responseBody.get("id");

            log.info("IoT Risk Case created successfully in Salesforce: {}", caseId);
            return caseId;

        } catch (Exception e) {
            log.error("Error creating IoT Risk Case in Salesforce", e);
            throw new RuntimeException("Failed to create IoT Risk Case in Salesforce", e);
        }
    }
}
