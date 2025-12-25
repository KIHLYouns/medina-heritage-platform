package com.medina.heritage.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medina.heritage.integration.dtos.kafka.ClaimStatusUpdateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service pour publier des messages sur Kafka.
 * Utilisé pour envoyer les mises à jour de statut de réclamation vers le système externe.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.claim-status-updates:claims.status-updates}")
    private String claimStatusUpdatesTopic;

    /**
     * Publie une mise à jour de statut de réclamation sur Kafka.
     * 
     * @param claimId L'ID de la réclamation (utilisé comme clé Kafka)
     * @param statusUpdate Le DTO contenant les informations de mise à jour
     */
    public void publishClaimStatusUpdate(String claimId, ClaimStatusUpdateDTO statusUpdate) {
        try {
            // Convertir le DTO en JSON
            String jsonPayload = objectMapper.writeValueAsString(statusUpdate);
            
            log.info("Publishing claim status update to Kafka topic: {} for claimId: {}", 
                    claimStatusUpdatesTopic, claimId);
            log.debug("Payload: {}", jsonPayload);
            
            // Envoyer le message à Kafka (clé = claimId pour garantir l'ordre des messages par claim)
            kafkaTemplate.send(claimStatusUpdatesTopic, claimId, jsonPayload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish claim status update to Kafka for claimId: {}", 
                                claimId, ex);
                    } else {
                        log.info("Successfully published claim status update to Kafka - Topic: {}, Partition: {}, Offset: {}, ClaimId: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                claimId);
                    }
                });
            
        } catch (Exception e) {
            log.error("Error publishing claim status update to Kafka for claimId: {}", claimId, e);
            throw new RuntimeException("Failed to publish claim status update to Kafka", e);
        }
    }
}
