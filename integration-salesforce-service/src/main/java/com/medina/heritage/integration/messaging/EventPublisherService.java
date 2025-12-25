package com.medina.heritage.integration.messaging;

import com.medina.heritage.events.alert.CaseStatusUpdateEvent;
import com.medina.heritage.integration.dtos.kafka.ClaimStatusUpdateDTO;
import com.medina.heritage.integration.entity.IdMapping;
import com.medina.heritage.integration.repository.IdMappingRepository;
import com.medina.heritage.integration.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour publier des événements vers le bus de messages RabbitMQ.
 * Utilise Spring Cloud Stream avec StreamBridge pour l'envoi dynamique.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final StreamBridge streamBridge;
    private final KafkaProducerService kafkaProducerService;
    private final IdMappingRepository idMappingRepository;

    /**
     * Publie un événement de mise à jour de statut de Case vers le bus de messages.
     * 1. Publie sur RabbitMQ pour les microservices internes
     * 2. Récupère le claimId depuis IdMapping (caseId → claimId)
     * 3. Publie sur Kafka pour le système externe
     * 
     * @param event L'événement à publier
     */
    public void publishCaseStatusUpdate(CaseStatusUpdateEvent event) {
        try {
            log.info("Publishing CaseStatusUpdateEvent for caseId: {}", event.getCaseId());
            
            // 1. Publier sur RabbitMQ pour les microservices internes
            boolean sent = streamBridge.send(
                "caseStatusUpdate-out-0",
                MessageBuilder
                    .withPayload(event)
                    .setHeader("event-type", CaseStatusUpdateEvent.EVENT_TYPE)
                    .setHeader("routing-key", "case.status.update")
                    .build()
            );
            
            if (sent) {
                log.info("Successfully published CaseStatusUpdateEvent to RabbitMQ for caseId: {}", event.getCaseId());
            } else {
                log.error("Failed to publish CaseStatusUpdateEvent to RabbitMQ for caseId: {}", event.getCaseId());
            }
            
            // 2. Récupérer le claimId depuis IdMapping (reverse lookup: caseId → claimId)
            log.debug("Attempting to find claimId mapping for caseId: {}", event.getCaseId());
            Optional<IdMapping> claimMapping = idMappingRepository.findBySfEntityId(event.getCaseId());
            
            if (claimMapping.isEmpty()) {
                log.warn("NO CLAIMID MAPPING FOUND for caseId: {}. Skipping Kafka publication.", event.getCaseId());
                return;
            }
            
            IdMapping mapping = claimMapping.get();
            
            String claimId = mapping.getLocalEntityId().toString();
            
            log.info("✅ Found claimId: {} (stored as UUID) for caseId: {} - Publishing to Kafka", claimId, event.getCaseId());
            
            // 3. Construire le DTO Kafka
            ClaimStatusUpdateDTO kafkaDto = buildKafkaDto(event, claimId);
            
            // 4. Publier sur Kafka
            kafkaProducerService.publishClaimStatusUpdate(claimId, kafkaDto);
            
            log.info("Successfully published claim status update to Kafka for claimId: {}", claimId);
            
        } catch (Exception e) {
            log.error("Error publishing CaseStatusUpdateEvent for caseId: {}", event.getCaseId(), e);
            throw new RuntimeException("Failed to publish event to message bus", e);
        }
    }
    
    /**
     * Construit le DTO Kafka depuis l'événement Salesforce.
     */
    private ClaimStatusUpdateDTO buildKafkaDto(CaseStatusUpdateEvent event, String claimId) {
        // Mapper le statut
        ClaimStatusUpdateDTO.StatusUpdate status = null;
        if (event.getStatus() != null) {
            ClaimStatusUpdateDTO.AssignedOperator assignedTo = null;
            if (event.getStatus().getAssignedTo() != null) {
                assignedTo = ClaimStatusUpdateDTO.AssignedOperator.builder()
                    .operatorId(event.getStatus().getAssignedTo().getOperatorId())
                    .operatorName(event.getStatus().getAssignedTo().getOperatorName())
                    .build();
            }
            
            status = ClaimStatusUpdateDTO.StatusUpdate.builder()
                .previous(event.getStatus().getPrevious())
                .newStatus(event.getStatus().getNewStatus())
                .reason(event.getStatus().getReason())
                .assignedTo(assignedTo)
                .build();
        }
        
        // Mapper la résolution
        ClaimStatusUpdateDTO.ResolutionUpdate resolution = null;
        if (event.getResolution() != null) {
            resolution = ClaimStatusUpdateDTO.ResolutionUpdate.builder()
                .summary(event.getResolution().getSummary())
                .actionsTaken(event.getResolution().getActionsTaken())
                .closingMessage(event.getResolution().getClosingMessage())
                .build();
        }
        
        // Construire le DTO final
        return ClaimStatusUpdateDTO.builder()
            .messageType(event.getMessageType())
            .timestamp(DateTimeFormatter.ISO_INSTANT.format(event.getTimestamp()))
            .claimId(claimId)
            .status(status)
            .resolution(resolution)
            .serviceReference(event.getCaseId()) // Utiliser caseId comme référence de service
            .build();
    }
}
