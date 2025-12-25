package com.medina.heritage.patrimoine.messaging;

import com.medina.heritage.events.alert.CitizenAlertCreatedEvent;
import com.medina.heritage.events.alert.CitizenAlertIdentifiedEvent;
import com.medina.heritage.patrimoine.entities.Building;
import com.medina.heritage.patrimoine.entities.QrTag;
import com.medina.heritage.patrimoine.exceptions.ResourceNotFoundException;
import com.medina.heritage.patrimoine.repositories.QrTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Configuration des consumers d'événements pour les alertes citoyennes du service patrimoine.
 * Écoute les alertes citoyennes et identifie le bâtiment patrimonial via le QR code.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CitizenAlertEventConsumer {

    private final QrTagRepository qrTagRepository;
    private final CitizenAlertEventPublisher citizenAlertEventPublisher;

    /**
     * Consumer pour CitizenAlertCreatedEvent.
     * Identifie le bâtiment patrimonial via le QR code et publie un événement enrichi.
     * Extrait uniquement les champs nécessaires pour la création du Case.
     */
    @Bean
    public Consumer<CitizenAlertCreatedEvent> citizenAlertCreatedConsumer() {
        return event -> {
            log.info("Received CitizenAlertCreatedEvent for QR code: {} from user: {}", 
                    event.getQrCode(), event.getUserId());
            
            try {
                // 1. Trouver le QR tag par son contenu (qrCode)
                QrTag qrTag = qrTagRepository.findByQrContent(event.getQrCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "QR Code not found: " + event.getQrCode()
                    ));
                
                Building building = qrTag.getBuilding();
                
                log.info("Building identified: {} (ID: {})", building.getName(), building.getId());
                
                
                // 3. Créer un événement enrichi avec UNIQUEMENT les infos nécessaires
                CitizenAlertIdentifiedEvent identifiedEvent = CitizenAlertIdentifiedEvent.builder()
                    .claimId(event.getClaimId())         // ID externe pour mapping Case Salesforce
                    .userId(event.getUserId())           // UUID interne de l'utilisateur
                    .buildingId(building.getId())        // UUID du building
                    .buildingCode(building.getCode())    // Code du building
                    .buildingName(building.getName())    // Nom du building
                    .imageUrl(event.getImageUrl())                  // URL de la première image
                    .description(event.getDescription()) // Description de la réclamation
                    .longitude(event.getLongitude())     // Longitude
                    .latitude(event.getLatitude())       // Latitude
                    .build();
                
                // 4. Publier l'événement pour que integration-service le traite
                citizenAlertEventPublisher.publishCitizenAlertIdentified(identifiedEvent);
                
                log.info("Published CitizenAlertIdentifiedEvent for building: {} with image: {}", 
                        building.getCode(), event.getImageUrl() != null ? "Yes" : "No");
                
            } catch (ResourceNotFoundException e) {
                log.error("QR Code not found: {}", e.getMessage());
                // TODO: Publier un événement d'erreur ou notifier l'utilisateur
            } catch (Exception e) {
                log.error("Error processing CitizenAlertCreatedEvent: {}", e.getMessage(), e);
                // L'exception va provoquer un NACK et le message sera remis en queue
                throw e;
            }
        };
    }
}
