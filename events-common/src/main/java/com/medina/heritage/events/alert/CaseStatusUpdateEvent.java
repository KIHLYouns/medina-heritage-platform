package com.medina.heritage.events.alert;

import com.medina.heritage.events.base.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Événement publié quand Salesforce envoie une mise à jour du statut d'un Case.
 * Cet événement est publié par le service d'intégration Salesforce
 * lors de la réception d'un webhook de Salesforce.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CaseStatusUpdateEvent extends BaseEvent {
    
    public static final String EVENT_TYPE = "salesforce.case.status.update";

    private String messageType;
    private String caseId;
    private StatusInfo status;
    private ResolutionInfo resolution;
    
    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class StatusInfo {
        private String previous;
        private String newStatus;
        private String reason;
        private AssignedTo assignedTo;
    }
    
    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class AssignedTo {
        private String operatorId;
        private String operatorName;
    }
    
    @Data
    @NoArgsConstructor
    @SuperBuilder
    public static class ResolutionInfo {
        private String summary;
        private List<String> actionsTaken;
        private String closingMessage;
    }
    
    public CaseStatusUpdateEvent initializeDefaults() {
        initializeEvent("integration-salesforce-service");
        return this;
    }
}
