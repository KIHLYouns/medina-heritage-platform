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
     */
    @Bean
    public Consumer<CitizenAlertCreatedEvent> citizenAlertCreatedConsumer() {
        return event -> {
            log.info("Received CitizenAlertCreatedEvent for QR code: {}", event.getQrCode());
            
            try {
                // 1. Trouver le QR tag par son contenu (qrCode)
                QrTag qrTag = qrTagRepository.findByQrContent(event.getQrCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "QR Code not found: " + event.getQrCode()
                    ));
                
                Building building = qrTag.getBuilding();
                
                log.info("Building identified: {} (ID: {})", building.getName(), building.getId());
                
                // 2. Créer un événement enrichi avec les infos du bâtiment
                CitizenAlertIdentifiedEvent identifiedEvent = CitizenAlertIdentifiedEvent.builder()
                    .userId(event.getUserId())
                    .buildingId(building.getId())
                    .buildingCode(building.getCode())
                    .buildingName(building.getName())
                    .imageUrl(event.getImageUrl())
                    .description(event.getDescription())
                    .longitude(event.getLongitude())
                    .latitude(event.getLatitude())
                    .build();
                
                // 3. Publier l'événement pour que integration-service le traite
                citizenAlertEventPublisher.publishCitizenAlertIdentified(identifiedEvent);
                
                log.info("Published CitizenAlertIdentifiedEvent for building: {}", building.getCode());
                
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
