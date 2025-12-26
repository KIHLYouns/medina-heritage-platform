package com.medina.heritage.integration.dtos.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour publier les réponses de service vers Kafka.
 * Format JSON exactement comme attendu par le système externe sur le topic claims.responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponseDTO {

    private String messageType;
    private String timestamp;
    private String claimId;
    private ResponseInfo response;
    private String serviceReference;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseInfo {
        private ServiceOperator from;
        private String message;
        private List<ResponseAttachment> attachments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceOperator {
        private String serviceType;
        private String operatorId;
        private String operatorName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseAttachment {
        private String url;
        private String fileName;
        private String fileType;
    }
}
