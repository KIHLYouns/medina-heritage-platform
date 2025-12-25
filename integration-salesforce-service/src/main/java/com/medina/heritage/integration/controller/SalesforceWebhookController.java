package com.medina.heritage.integration.controller;

import com.medina.heritage.events.alert.CaseStatusUpdateEvent;
import com.medina.heritage.integration.dtos.request.SalesforceCaseStatusUpdateRequest;
import com.medina.heritage.integration.messaging.EventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrôleur REST pour recevoir les webhooks de Salesforce.
 * Reçoit les mises à jour de statut de Case et les publie sur le bus de messages.
 */
@Slf4j
@RestController
@RequestMapping("/api/salesforce/webhook")
@RequiredArgsConstructor
public class SalesforceWebhookController {

    private final EventPublisherService eventPublisherService;

    /**
     * Endpoint pour recevoir les mises à jour de statut de Case depuis Salesforce.
     * POST /api/salesforce/webhook/case-status-update
     * 
     * @param request Le payload JSON envoyé par Salesforce
     * @return ResponseEntity avec statut 200 si succès, 500 si erreur
     */
    @PostMapping("/case-status-update")
    public ResponseEntity<WebhookResponse> handleCaseStatusUpdate(
            @RequestBody SalesforceCaseStatusUpdateRequest request) {
        
        log.info("Received Case Status Update webhook from Salesforce - CaseId: {}, MessageType: {}", 
                request.getCaseId(), request.getMessageType());
        
        try {
            // Convertir le DTO de la requête en événement du domain
            CaseStatusUpdateEvent event = convertToEvent(request);
            
            // Publier l'événement sur le bus de messages
            eventPublisherService.publishCaseStatusUpdate(event);
            
            log.info("Successfully processed Case Status Update for CaseId: {}", request.getCaseId());
            
            return ResponseEntity.ok(
                WebhookResponse.builder()
                    .success(true)
                    .message("Case status update received and published successfully")
                    .eventId(event.getEventId().toString())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Error processing Case Status Update webhook for CaseId: {}", 
                    request.getCaseId(), e);
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(WebhookResponse.builder()
                    .success(false)
                    .message("Failed to process webhook: " + e.getMessage())
                    .build()
                );
        }
    }

    /**
     * Convertit le DTO de requête Salesforce en événement du domain.
     */
    private CaseStatusUpdateEvent convertToEvent(SalesforceCaseStatusUpdateRequest request) {
        
        // Convertir les informations de statut
        CaseStatusUpdateEvent.StatusInfo statusInfo = null;
        if (request.getStatus() != null) {
            CaseStatusUpdateEvent.AssignedTo assignedTo = null;
            if (request.getStatus().getAssignedTo() != null) {
                assignedTo = CaseStatusUpdateEvent.AssignedTo.builder()
                    .operatorId(request.getStatus().getAssignedTo().getOperatorId())
                    .operatorName(request.getStatus().getAssignedTo().getOperatorName())
                    .build();
            }
            
            statusInfo = CaseStatusUpdateEvent.StatusInfo.builder()
                .previous(request.getStatus().getPrevious())
                .newStatus(request.getStatus().getNewStatus())
                .reason(request.getStatus().getReason())
                .assignedTo(assignedTo)
                .build();
        }
        
        // Convertir les informations de résolution
        CaseStatusUpdateEvent.ResolutionInfo resolutionInfo = null;
        if (request.getResolution() != null) {
            resolutionInfo = CaseStatusUpdateEvent.ResolutionInfo.builder()
                .summary(request.getResolution().getSummary())
                .actionsTaken(request.getResolution().getActionsTaken())
                .closingMessage(request.getResolution().getClosingMessage())
                .build();
        }
        
        // Créer l'événement
        CaseStatusUpdateEvent event = CaseStatusUpdateEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .timestamp(Instant.now())
            .messageType(request.getMessageType())
            .caseId(request.getCaseId())
            .status(statusInfo)
            .resolution(resolutionInfo)
            .build();
        
        // Initialiser les valeurs par défaut (serviceName, etc.)
        event.initializeDefaults();
        
        return event;
    }

    /**
     * DTO pour la réponse du webhook.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class WebhookResponse {
        private boolean success;
        private String message;
        private String eventId;
    }
}
