package com.medina.heritage.integration.service;

import com.medina.heritage.events.building.BuildingCreatedEvent;
import com.medina.heritage.events.building.BuildingUpdatedEvent;
import com.medina.heritage.integration.entity.IdMapping;
import com.medina.heritage.integration.repository.IdMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for synchronizing Building entities to Salesforce Assets.
 * Handles both creation and update of buildings as Salesforce Assets.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceBuildingSyncService {

    private final SalesforceAuthService authService;
    private final SalesforceLocationService locationService;
    private final IdMappingRepository idMappingRepository;
    private final RestTemplate restTemplate;

    @Value("${salesforce.instance-url}")
    private String instanceUrl;

    @Value("${salesforce.api-version:v64.0}")
    private String apiVersion;

    @Value("${salesforce.default-account-id}")
    private String defaultAccountId;

    private static final String ENTITY_TYPE_BUILDING = "BUILDING";
    private static final String SF_ASSET_OBJECT = "Asset";

    /**
     * Synchronizes a newly created building to Salesforce as an Asset.
     * Creates both the Location and the Asset, then stores the ID mapping.
     *
     * @param event BuildingCreatedEvent containing building details
     */
    public void syncNewBuilding(BuildingCreatedEvent event) {
        try {
            UUID buildingUUID = UUID.fromString(event.getBuildingId());
            log.info("Starting sync of new building: {} ({})", event.getCode(), buildingUUID);

            // Check if already synchronized
            Optional<IdMapping> existingMapping = idMappingRepository
                    .findByLocalEntityTypeAndLocalEntityId(ENTITY_TYPE_BUILDING, buildingUUID);

            if (existingMapping.isPresent()) {
                log.warn("Building {} already synchronized to Salesforce Asset: {}",
                        buildingUUID, existingMapping.get().getSfEntityId());
                return;
            }

            // Step 1: Create Location in Salesforce if coordinates are available
            String locationId = null;
            if (event.getLatitude() != null && event.getLongitude() != null) {
                locationId = locationService.createLocation(
                        event.getCode(),
                        event.getName(),
                        event.getLatitude(),
                        event.getLongitude()
                );
                log.info("Location created: {}", locationId);
            }

            // Step 2: Create Asset in Salesforce
            String assetId = createAsset(event, locationId);
            log.info("Asset created in Salesforce: {}", assetId);

            // Step 3: Store the ID mapping
            IdMapping mapping = IdMapping.builder()
                    .localEntityType(ENTITY_TYPE_BUILDING)
                    .localEntityId(buildingUUID)
                    .sfEntityId(assetId)
                    .lastSyncAt(OffsetDateTime.now())
                    .syncStatus("SYNCED")
                    .build();

            idMappingRepository.save(mapping);
            log.info("ID mapping saved: Local {} -> Salesforce {}", buildingUUID, assetId);

        } catch (Exception e) {
            log.error("Error synchronizing new building {} to Salesforce: {}",
                    event.getBuildingId(), e.getMessage(), e);
            throw new RuntimeException("Failed to sync building to Salesforce", e);
        }
    }

    /**
     * Synchronizes an updated building to Salesforce.
     * Updates the existing Asset or creates one if mapping doesn't exist.
     *
     * @param event BuildingUpdatedEvent containing updated building details
     */
    public void syncUpdatedBuilding(BuildingUpdatedEvent event) {
        try {
            UUID buildingUUID = UUID.fromString(event.getBuildingId());
            log.info("Starting sync of updated building: {} ({})", event.getCode(), buildingUUID);

            // Find existing mapping
            Optional<IdMapping> mappingOpt = idMappingRepository
                    .findByLocalEntityTypeAndLocalEntityId(ENTITY_TYPE_BUILDING, buildingUUID);

            if (mappingOpt.isEmpty()) {
                log.warn("No Salesforce mapping found for building {}. Creating new Asset.", buildingUUID);
                // Convert UpdateEvent to CreateEvent format for initial sync
                BuildingCreatedEvent createEvent = convertToCreateEvent(event);
                syncNewBuilding(createEvent);
                return;
            }

            IdMapping mapping = mappingOpt.get();
            String assetId = mapping.getSfEntityId();

            // Update the Asset in Salesforce
            updateAsset(assetId, event);
            log.info("Asset updated in Salesforce: {}", assetId);

            // Update the mapping timestamp
            mapping.setLastSyncAt(OffsetDateTime.now());
            mapping.setSyncStatus("SYNCED");
            idMappingRepository.save(mapping);

        } catch (Exception e) {
            log.error("Error synchronizing updated building {} to Salesforce: {}",
                    event.getBuildingId(), e.getMessage(), e);
            throw new RuntimeException("Failed to sync building update to Salesforce", e);
        }
    }

    /**
     * Creates an Asset in Salesforce.
     *
     * @param event BuildingCreatedEvent with building details
     * @param locationId Salesforce Location ID (can be null)
     * @return Salesforce Asset ID
     */
    @SuppressWarnings("unchecked")
    private String createAsset(BuildingCreatedEvent event, String locationId) {
        try {
            Map<String, Object> assetPayload = buildAssetPayload(
                    event.getName(),
                    event.getCode(),
                    event.getDescription(),
                    event.getAddress(),
                    locationId,
                    event.getImageUrl()
            );

            String accessToken = authService.getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");

            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/" + SF_ASSET_OBJECT;

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(assetPayload, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    (Class<Map<String, Object>>)(Class<?>)Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            return (String) responseBody.get("id");

        } catch (Exception e) {
            log.error("Error creating Asset in Salesforce", e);
            throw new RuntimeException("Failed to create Asset in Salesforce", e);
        }
    }

    /**
     * Updates an existing Asset in Salesforce.
     *
     * @param assetId Salesforce Asset ID
     * @param event BuildingUpdatedEvent with updated details
     */
    private void updateAsset(String assetId, BuildingUpdatedEvent event) {
        try {
            Map<String, Object> updatePayload = new HashMap<>();
            updatePayload.put("Name", event.getName());
            
            // Merge description and address into Salesforce Asset Description field
            String mergedDescription = buildMergedDescription(event.getDescription(), event.getAddress());
            if (mergedDescription != null && !mergedDescription.isEmpty()) {
                updatePayload.put("Description", mergedDescription);
            }

            if (event.getImageUrl() != null) {
                updatePayload.put("Image_Url__c", event.getImageUrl());
            }

            String accessToken = authService.getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Content-Type", "application/json");

            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/" + SF_ASSET_OBJECT + "/" + assetId;

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(updatePayload, headers);

            restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    request,
                    Void.class
            );

            log.debug("Asset {} updated successfully", assetId);

        } catch (Exception e) {
            log.error("Error updating Asset {} in Salesforce", assetId, e);
            throw new RuntimeException("Failed to update Asset in Salesforce", e);
        }
    }

    /**
     * Builds the Asset payload for Salesforce.
     */
    private Map<String, Object> buildAssetPayload(
            String name,
            String code,
            String description,
            String address,
            String locationId,
            String imageUrl
    ) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("Name", name);
        payload.put("SerialNumber", code); // Using code as serial number
        
        // Set AccountId (required field for Asset)
        if (defaultAccountId != null && !defaultAccountId.isEmpty()) {
            payload.put("AccountId", defaultAccountId);
        }
        
        // Merge description and address into Salesforce Asset Description field
        String mergedDescription = buildMergedDescription(description, address);
        if (mergedDescription != null && !mergedDescription.isEmpty()) {
            payload.put("Description", mergedDescription);
        }
        
        if (locationId != null) {
            payload.put("Location__c", locationId);
        }

        if (imageUrl != null) {
            payload.put("Image_Url__c", imageUrl);
        }

        // Asset status - Active by default
        payload.put("Status", "Registered");

        log.debug("Asset payload: {}", payload);
        return payload;
    }
    
    /**
     * Merges description and address into a single string for Salesforce Description field.
     * Format: "Description: {description}\nAddress: {address}"
     */
    private String buildMergedDescription(String description, String address) {
        StringBuilder merged = new StringBuilder();
        
        if (description != null && !description.isEmpty()) {
            merged.append(description);
        }
        
        if (address != null && !address.isEmpty()) {
            if (merged.length() > 0) {
                merged.append("\n\nAdresse: ");
            }
            merged.append(address);
        }
        
        return merged.length() > 0 ? merged.toString() : null;
    }

    /**
     * Converts BuildingUpdatedEvent to BuildingCreatedEvent format for initial sync.
     */
    private BuildingCreatedEvent convertToCreateEvent(BuildingUpdatedEvent event) {
        return BuildingCreatedEvent.builder()
                .buildingId(event.getBuildingId())
                .code(event.getCode())
                .name(event.getName())
                .address(event.getAddress())
                .description(event.getDescription())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .imageUrl(event.getImageUrl())
                .eventId(event.getEventId())
                .timestamp(event.getTimestamp())
                .source(event.getSource())
                .correlationId(event.getCorrelationId())
                .build();
    }
}
