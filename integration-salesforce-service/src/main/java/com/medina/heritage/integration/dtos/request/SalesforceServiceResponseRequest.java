package com.medina.heritage.integration.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour recevoir les webhooks de réponses de service depuis Salesforce.
 * Structure JSON exactement comme envoyée par Salesforce.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesforceServiceResponseRequest {

    private String messageType;
    private String timestamp;
    private String caseId;
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
