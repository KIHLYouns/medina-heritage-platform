package com.medina.heritage.integration.service;

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
import java.util.List;
import java.util.Map;

/**
 * Service pour créer et gérer les Locations (Lieux) dans Salesforce.
 * Les Locations sont utilisées pour stocker les coordonnées GPS des signalements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceLocationService {

    private final SalesforceAuthService authService;
    private final RestTemplate restTemplate;

    @Value("${salesforce.instance-url}")
    private String instanceUrl;

    @Value("${salesforce.api-version:v64.0}")
    private String apiVersion;

    /**
     * Crée une Location (lieu) dans Salesforce avec les coordonnées GPS.
     *
     * @param buildingCode  Code du bâtiment (ex: BLDG-00123)
     * @param buildingName  Nom du bâtiment
     * @param latitude      Latitude GPS
     * @param longitude     Longitude GPS
     * @return L'ID de la Location créée dans Salesforce
     */
    public String createLocation(
            String buildingCode,
            String buildingName,
            Double latitude,
            Double longitude
    ) {
        if (latitude != null && longitude != null) {
            String existingId = findLocationByCoordinates(latitude, longitude);
            if (existingId != null) {
                log.info("Location already exists: {}", existingId);
                return existingId;
            }
        }

        try {
            log.info("Creating Location in Salesforce for building: {} ({}, {})", 
                    buildingCode, latitude, longitude);

            // 1. Construire le payload JSON pour la Location
            Map<String, Object> locationPayload = buildLocationPayload(
                    buildingCode,
                    buildingName,
                    latitude,
                    longitude
            );

            // 2. Obtenir le token d'authentification
            String accessToken = authService.getAccessToken();

            // 3. Préparer la requête HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");

            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Location";

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(locationPayload, headers);

            // 4. Envoyer la requête POST à Salesforce
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
            );

            // 5. Extraire l'ID de la Location créée
            Map<String, Object> responseBody = response.getBody();
            String locationId = (String) responseBody.get("id");

            log.info("Location created successfully: {}", locationId);
            return locationId;

        } catch (Exception e) {
            log.error("Error creating Location in Salesforce", e);
            throw new RuntimeException("Failed to create Location in Salesforce", e);
        }
    }

    /**
     * Crée une Location et retourne les informations (ID + coordonnées) pour liaison avec Case.
     *
     * @param buildingCode  Code du bâtiment
     * @param buildingName  Nom du bâtiment
     * @param latitude      Latitude GPS
     * @param longitude     Longitude GPS
     * @return Objet LocationInfo contenant l'ID et les coordonnées
     */
    public LocationInfo createLocationAndGetInfo(
            String buildingCode,
            String buildingName,
            Double latitude,
            Double longitude
    ) {
        String locationId = createLocation(buildingCode, buildingName, latitude, longitude);
        return new LocationInfo(locationId, latitude, longitude);
    }
    
    private Map<String, Object> buildLocationPayload(
            String buildingCode,
            String buildingName,
            Double latitude,
            Double longitude
    ) {
        Map<String, Object> payload = new HashMap<>();
        
        payload.put("Name", buildingName);

        payload.put("LocationType", "Building");
        
        if (latitude != null && longitude != null) {
            payload.put("Latitude", latitude);
            payload.put("Longitude", longitude);
            
            log.debug("Geolocation set: latitude={}, longitude={}", latitude, longitude);
        }
        
        log.debug("Location payload: {}", payload);
        return payload;
    }

    private String findLocationByCoordinates(Double latitude, Double longitude) {
        try {
            String query = "SELECT Id FROM Location WHERE Latitude = " + latitude + " AND Longitude = " + longitude + " LIMIT 1";
            String url = instanceUrl + "/services/data/" + apiVersion + "/query?q={query}";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authService.getAccessToken());
            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class,
                    query
            );

            Map<String, Object> body = response.getBody();
            if (body != null) {
                List<Map<String, Object>> records = (List<Map<String, Object>>) body.get("records");
                if (records != null && !records.isEmpty()) {
                    return (String) records.get(0).get("Id");
                }
            }
        } catch (Exception e) {
            log.warn("Error checking existing location: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Classe interne pour transporter les informations de la Location créée.
     */
    public static class LocationInfo {
        public String locationId;
        public Double latitude;
        public Double longitude;

        public LocationInfo(String locationId, Double latitude, Double longitude) {
            this.locationId = locationId;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return "LocationInfo{" +
                    "locationId='" + locationId + '\'' +
                    ", latitude=" + latitude +
                    ", longitude=" + longitude +
                    '}';
        }
    }
}
