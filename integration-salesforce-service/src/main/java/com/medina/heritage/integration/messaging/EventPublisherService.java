package com.medina.heritage.integration.messaging;

import com.medina.heritage.events.alert.CaseStatusUpdateEvent;
import com.medina.heritage.events.alert.ServiceResponseEvent;
import com.medina.heritage.integration.dtos.kafka.ClaimStatusUpdateDTO;
import com.medina.heritage.integration.dtos.kafka.ServiceResponseDTO;
import com.medina.heritage.integration.entity.IdMapping;
import com.medina.heritage.integration.repository.IdMappingRepository;
import com.medina.heritage.integration.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final KafkaProducerService kafkaProducerService;
    private final IdMappingRepository idMappingRepository;

    /**
     * Publie un événement de mise à jour de statut de Case vers Kafka.
     * 1. Récupère le claimId depuis IdMapping (caseId → claimId)
     * 2. Publie sur Kafka pour le système externe
     * 
     * @param event L'événement à publier
     */
    public void publishCaseStatusUpdate(CaseStatusUpdateEvent event) {
        try {
            log.info("Publishing CaseStatusUpdateEvent for caseId: {}", event.getCaseId());
            
            // 1. Récupérer le claimId depuis IdMapping (reverse lookup: caseId → claimId)
            log.debug("Attempting to find claimId mapping for caseId: {}", event.getCaseId());
            Optional<IdMapping> claimMapping = idMappingRepository.findBySfEntityId(event.getCaseId());
            
            if (claimMapping.isEmpty()) {
                log.warn("NO CLAIMID MAPPING FOUND for caseId: {}. Skipping Kafka publication.", event.getCaseId());
                return;
            }
            
            IdMapping mapping = claimMapping.get();
            
            String claimId = mapping.getLocalEntityId().toString();
            
            log.info("✅ Found claimId: {} (stored as UUID) for caseId: {} - Publishing to Kafka", claimId, event.getCaseId());
            
            // 2. Construire le DTO Kafka
            ClaimStatusUpdateDTO kafkaDto = buildKafkaDto(event, claimId);
            
            // 3. Publier sur Kafka
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

    /**
     * Publie un événement de réponse de service vers Kafka.
     * 1. Récupère le claimId depuis IdMapping (caseId → claimId)
     * 2. Publie sur Kafka pour le système externe
     * 
     * @param event L'événement à publier
     */
    public void publishServiceResponse(ServiceResponseEvent event) {
        try {
            log.info("Publishing ServiceResponseEvent for caseId: {}", event.getCaseId());
            
            // 1. Récupérer le claimId depuis IdMapping (reverse lookup: caseId → claimId)
            log.debug("Attempting to find claimId mapping for caseId: {}", event.getCaseId());
            Optional<IdMapping> claimMapping = idMappingRepository.findBySfEntityId(event.getCaseId());
            
            if (claimMapping.isEmpty()) {
                log.warn("NO CLAIMID MAPPING FOUND for caseId: {}. Skipping Kafka publication.", event.getCaseId());
                return;
            }
            
            IdMapping mapping = claimMapping.get();
            String claimId = mapping.getLocalEntityId().toString();
            
            log.info("✅ Found claimId: {} (stored as UUID) for caseId: {} - Publishing to Kafka", claimId, event.getCaseId());
            
            // 2. Construire le DTO Kafka
            ServiceResponseDTO kafkaDto = buildServiceResponseKafkaDto(event, claimId);
            
            // 3. Publier sur Kafka
            kafkaProducerService.publishServiceResponse(claimId, kafkaDto);
            
            log.info("Successfully published service response to Kafka for claimId: {}", claimId);
            
        } catch (Exception e) {
            log.error("Error publishing ServiceResponseEvent for caseId: {}", event.getCaseId(), e);
            throw new RuntimeException("Failed to publish service response to message bus", e);
        }
    }
    
    /**
     * Construit le DTO Kafka pour la réponse de service depuis l'événement.
     */
    private ServiceResponseDTO buildServiceResponseKafkaDto(ServiceResponseEvent event, String claimId) {
        // Mapper l'opérateur de service
        ServiceResponseDTO.ServiceOperator operator = null;
        if (event.getFrom() != null) {
            operator = ServiceResponseDTO.ServiceOperator.builder()
                .serviceType(event.getFrom().getServiceType())
                .operatorId(event.getFrom().getOperatorId())
                .operatorName(event.getFrom().getOperatorName())
                .build();
        }
        
        // Mapper les pièces jointes
        java.util.List<ServiceResponseDTO.ResponseAttachment> attachments = null;
        if (event.getAttachments() != null && !event.getAttachments().isEmpty()) {
            attachments = event.getAttachments().stream()
                .map(att -> ServiceResponseDTO.ResponseAttachment.builder()
                    .url(att.getUrl())
                    .fileName(att.getFileName())
                    .fileType(att.getFileType())
                    .build())
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Construire l'objet response
        ServiceResponseDTO.ResponseInfo response = ServiceResponseDTO.ResponseInfo.builder()
            .from(operator)
            .message(event.getMessage())
            .attachments(attachments)
            .build();
        
        // Construire le DTO final
        return ServiceResponseDTO.builder()
            .messageType("SERVICE_RESPONSE")
            .timestamp(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
            .claimId(claimId)
            .response(response)
            .serviceReference(event.getCaseId())
            .build();
    }
}
