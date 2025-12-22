package com.medina.heritage.integration.messaging;

import com.medina.heritage.events.iot.RiskAlertEvent;
import com.medina.heritage.integration.service.SalesforceCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Consumer Spring Cloud Stream pour les alertes de risque IoT.
 * Écoute les événements RiskAlertEvent et crée un Case dans Salesforce.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RiskAlertEventConsumer {

    private final SalesforceCaseService salesforceCaseService;

    /**
     * Consumer pour RiskAlertEvent.
     * Crée un Case dans Salesforce lorsqu'un seuil de risque IoT est dépassé.
     */
    @Bean
    public Consumer<RiskAlertEvent> riskAlertConsumer() {
        return event -> {
            log.info("Received RiskAlertEvent: deviceId={}, metricType={}, severity={}, breachDirection={}", 
                event.getDeviceId(), event.getMetricType(), event.getSeverityLevel(), event.getBreachDirection());

            try {
                // Utiliser le sfAssetId depuis l'événement (récupéré depuis le device)
                String assetId = event.getSfAssetId();
                if (assetId == null || assetId.isBlank()) {
                    log.warn("No sfAssetId found in event for device: {}. Case will be created without Asset link.", 
                        event.getDeviceSerialNumber());
                }

                // Créer le Case dans Salesforce
                String caseId = salesforceCaseService.createIotRiskCase(
                    assetId,
                    event.getDeviceSerialNumber(),
                    event.getMetricType(),
                    event.getValue(),
                    event.getUnit(),
                    event.getSeverityLevel(),
                    event.getThresholdMin(),
                    event.getThresholdMax(),
                    event.getBreachDirection(),
                    event.getDescription()
                );

                log.info("Risk Alert Case created in Salesforce: {} for device {}", 
                    caseId, event.getDeviceSerialNumber());

            } catch (Exception e) {
                log.error("Error processing RiskAlertEvent: {}", e.getMessage(), e);
                // L'exception va provoquer un NACK et le message sera remis en queue
                throw e;
            }
        };
    }
}

