package com.medina.heritage.integration.messaging;

import com.medina.heritage.events.alert.CitizenAlertIdentifiedEvent;
import com.medina.heritage.integration.entity.IdMapping;
import com.medina.heritage.integration.repository.IdMappingRepository;
import com.medina.heritage.integration.service.SalesforceCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Configuration des consumers d'événements pour l'intégration Salesforce.
 * Écoute les événements d'alertes citoyennes identifiées et crée le Case dans Salesforce.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CitizenAlertEventConsumer {

    private final IdMappingRepository idMappingRepository;
    private final SalesforceCaseService salesforceCaseService;
    
    // UUID namespace pour générer des UUIDs déterministes à partir de String claimIds
    private static final UUID CLAIM_NAMESPACE = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

    /**
     * Consumer pour CitizenAlertIdentifiedEvent.
     * Enrichit avec les IDs Salesforce et crée le Case.
     */
    @Bean
    public Consumer<CitizenAlertIdentifiedEvent> citizenAlertIdentifiedConsumer() {
        return event -> {
            log.info("Received CitizenAlertIdentifiedEvent for building: {} ({}) from user UUID: {}", 
                    event.getBuildingId(), event.getBuildingCode(), event.getUserId());

            try {
                // 1. Récupérer le Contact ID (User UUID -> Contact)
                String contactId = getSalesforceId("USER", event.getUserId());
                if (contactId == null) {
                    log.warn("No Salesforce Contact found for userId: {}", event.getUserId());
                    // TODO: Créer le Contact dans Salesforce ou skip
                }

                // 2. Récupérer l'Asset ID (Building -> Asset)
                String assetId = getSalesforceId("BUILDING", event.getBuildingId());
                if (assetId == null) {
                    log.warn("No Salesforce Asset found for buildingId: {}", event.getBuildingId());
                    // TODO: Créer l'Asset dans Salesforce ou skip
                }

                // 3. Créer le Case dans Salesforce
                String caseId = salesforceCaseService.createCase(
                    contactId,
                    assetId,
                    event.getBuildingCode(),
                    event.getBuildingName(),
                    event.getImageUrl(),
                    event.getDescription(),
                    event.getLongitude(),
                    event.getLatitude()
                );

                log.info("Case created in Salesforce: {}", caseId);
                
                // 4. Mapper le claimId avec le caseId pour suivi
                if (event.getClaimId() != null && !event.getClaimId().trim().isEmpty()) {
                    try {
                        // Créer un UUID déterministe à partir du String claimId
                        UUID claimUUID = generateDeterministicUUID(event.getClaimId());
                        
                        IdMapping claimMapping = IdMapping.builder()
                            .localEntityType("CLAIM")
                            .localEntityId(claimUUID)
                            .sfEntityId(caseId)
                            .syncStatus("SYNCED")
                            .build();
                        idMappingRepository.save(claimMapping);
                        
                        log.info("Mapped claimId {} (UUID: {}) to Salesforce caseId {}", 
                                event.getClaimId(), claimUUID, caseId);
                    } catch (Exception e) {
                        log.warn("Failed to save claimId mapping: {} - continuing anyway", 
                                event.getClaimId(), e);
                    }
                }

            } catch (Exception e) {
                log.error("Error processing CitizenAlertIdentifiedEvent: {}", e.getMessage(), e);
                // L'exception va provoquer un NACK et le message sera remis en queue
                throw e;
            }
        };
    }

    /**
     * Récupère l'ID Salesforce correspondant à une entité locale via UUID.
     */
    private String getSalesforceId(String entityType, java.util.UUID entityId) {
        Optional<IdMapping> mapping = idMappingRepository
            .findByLocalEntityTypeAndLocalEntityId(entityType, entityId);

        return mapping.map(IdMapping::getSfEntityId).orElse(null);
    }
    
    /**
     * Génère un UUID v5 déterministe basé sur un String claimId.
     * Cela garantit que le même claimId génère toujours le même UUID.
     */
    private UUID generateDeterministicUUID(String claimId) {
        return UUID.nameUUIDFromBytes((CLAIM_NAMESPACE.toString() + ":" + claimId).getBytes());
    }
}

