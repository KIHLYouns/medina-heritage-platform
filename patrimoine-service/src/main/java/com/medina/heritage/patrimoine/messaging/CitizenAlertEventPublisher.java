package com.medina.heritage.patrimoine.messaging;

import com.medina.heritage.events.alert.CitizenAlertIdentifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

/**
 * Service pour publier les événements liés aux alertes citoyennes sur les bâtiments patrimoniaux.
 * Utilise Spring Cloud Stream pour envoyer les messages à RabbitMQ.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CitizenAlertEventPublisher {

    private final StreamBridge streamBridge;
    
    private static final String CITIZEN_ALERT_IDENTIFIED_BINDING = "citizenAlertIdentifiedSupplier-out-0";

    /**
     * Publie un événement quand un bâtiment patrimonial est identifié via QR code
     * suite à une alerte citoyenne.
     * 
     * @param event L'événement à publier contenant les informations du bâtiment et de l'alerte
     */
    public void publishCitizenAlertIdentified(CitizenAlertIdentifiedEvent event) {
        event.initializeDefaults();
        log.info("Publishing CitizenAlertIdentifiedEvent for building: {} ({})", 
                event.getBuildingId(), event.getBuildingCode());
        
        boolean sent = streamBridge.send(CITIZEN_ALERT_IDENTIFIED_BINDING, event);
        
        if (sent) {
            log.debug("CitizenAlertIdentifiedEvent sent successfully for building: {}", 
                    event.getBuildingId());
        } else {
            log.error("Failed to send CitizenAlertIdentifiedEvent for building: {}", 
                    event.getBuildingId());
        }
    }
}
