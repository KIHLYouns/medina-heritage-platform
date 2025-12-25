package com.medina.heritage.integration.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour publier les mises à jour de statut de réclamation vers Kafka.
 * Format JSON exactement comme attendu par le système externe sur le topic claims.status-updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimStatusUpdateDTO {

    private String messageType;
    private String timestamp;
    private String claimId;
    private StatusUpdate status;
    private ResolutionUpdate resolution;
    private String serviceReference;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdate {
        private String previous;
        
        @JsonProperty("new")
        private String newStatus;
        
        private String reason;
        private AssignedOperator assignedTo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignedOperator {
        private String operatorId;
        private String operatorName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResolutionUpdate {
        private String summary;
        private List<String> actionsTaken;
        private String closingMessage;
    }
}
