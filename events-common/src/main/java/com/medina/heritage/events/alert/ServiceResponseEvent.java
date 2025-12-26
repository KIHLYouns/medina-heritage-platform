package com.medina.heritage.events.alert;

import com.medina.heritage.events.base.BaseEvent;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Event pour les réponses de service provenant de Salesforce.
 * Publié sur RabbitMQ pour notification aux autres microservices.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServiceResponseEvent extends BaseEvent {

    private String caseId;
    private ServiceOperator from;
    private String message;
    private List<ResponseAttachment> attachments;
    private String serviceReference;

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

    /**
     * Initialise les champs par défaut de l'événement.
     */
    public void initializeDefaults() {
        initializeEvent("integration-salesforce-service");
    }
}
