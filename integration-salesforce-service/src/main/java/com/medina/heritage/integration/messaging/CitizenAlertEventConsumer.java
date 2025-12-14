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

    /**
     * Consumer pour CitizenAlertIdentifiedEvent.
     * Enrichit avec les IDs Salesforce et crée le Case.
     */
    @Bean
    public Consumer<CitizenAlertIdentifiedEvent> citizenAlertIdentifiedConsumer() {
        return event -> {
            log.info("Received CitizenAlertIdentifiedEvent for building: {} ({})", 
                    event.getBuildingId(), event.getBuildingCode());

            try {
                // 1. Récupérer le Contact ID (User -> Contact)
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

            } catch (Exception e) {
                log.error("Error processing CitizenAlertIdentifiedEvent: {}", e.getMessage(), e);
                // L'exception va provoquer un NACK et le message sera remis en queue
                throw e;
            }
        };
    }

    /**
     * Récupère l'ID Salesforce correspondant à une entité locale.
     */
    private String getSalesforceId(String entityType, java.util.UUID entityId) {
        Optional<IdMapping> mapping = idMappingRepository
            .findByLocalEntityTypeAndLocalEntityId(entityType, entityId);

        return mapping.map(IdMapping::getSfEntityId).orElse(null);
    }
}
