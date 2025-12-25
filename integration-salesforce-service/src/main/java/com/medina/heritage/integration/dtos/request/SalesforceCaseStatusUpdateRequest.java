package com.medina.heritage.integration.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour recevoir les mises à jour de statut de Case depuis Salesforce via webhook.
 * Structure JSON exacte attendue de Salesforce.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesforceCaseStatusUpdateRequest {

    private String messageType;
    private String caseId;
    private String timestamp;
    private Status status;
    private Resolution resolution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private String previous;
        
        @JsonProperty("new")
        private String newStatus;
        
        private String reason;
        private AssignedTo assignedTo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignedTo {
        private String operatorId;
        private String operatorName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Resolution {
        private String summary;
        private List<String> actionsTaken;
        private String closingMessage;
    }
}
