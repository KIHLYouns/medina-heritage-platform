package com.medina.heritage.patrimoine.messaging;

import com.medina.heritage.events.alert.CitizenAlertCreatedEvent;
import com.medina.heritage.patrimoine.dtos.kafka.ClaimRequestDTO;
import com.medina.heritage.patrimoine.services.UserMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service qui consomme les réclamations depuis Kafka,
 * les transforme en CitizenAlertCreatedEvent,
 * et les publie sur RabbitMQ pour traitement interne.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimKafkaConsumer {

    private final CitizenAlertEventPublisher citizenAlertEventPublisher;
    private final UserMappingService userMappingService;

    /**
     * Écoute le topic Kafka 'heritage-claims' et traite les réclamations reçues.
     * 
     * @param claimRequest La réclamation reçue depuis Kafka
     */
    @KafkaListener(
        topics = "${kafka.topics.claims:claims.PAT}",
        groupId = "${spring.kafka.consumer.group-id:patrimoine-service-group}"
    )
    public void consumeClaim(ClaimRequestDTO claimRequest) {
        log.info("Received claim from Kafka - User: {}, QR Code: {}, Service Type: {}", 
                claimRequest.getUser().getId(),
                claimRequest.getClaim().getExtraData().getQrCode(),
                claimRequest.getClaim().getServiceType());
        
        try {
            // 1. Transformer le DTO Kafka en CitizenAlertCreatedEvent
            CitizenAlertCreatedEvent event = mapToEvent(claimRequest);
            
            // 2. Publier l'événement sur RabbitMQ pour traitement interne
            citizenAlertEventPublisher.publishCitizenAlertCreated(event);
            
            log.info("Successfully published CitizenAlertCreatedEvent to RabbitMQ for QR Code: {}", 
                    event.getQrCode());
            
        } catch (Exception e) {
            log.error("Error processing claim from Kafka: {}", e.getMessage(), e);
            // L'exception sera gérée par Kafka (retry, dead letter queue, etc.)
            throw e;
        }
    }

    /**
     * Transforme le DTO Kafka en événement CitizenAlertCreatedEvent.
     * Mappe tous les champs nécessaires de la réclamation.
     * Effectue le mapping Clerk ID -> UUID interne.
     * Fusionne les URLs d'images avec des pipes (|).
     * 
     * @param claim La réclamation reçue depuis Kafka
     * @return L'événement CitizenAlertCreatedEvent prêt à être publié sur RabbitMQ
     */
    private CitizenAlertCreatedEvent mapToEvent(ClaimRequestDTO claim) {
        // 1. Mapper Clerk ID -> UUID interne
        String clerkUserId = claim.getUser().getId();
        UUID internalUserId = userMappingService.getOrCreateUserUUID(
                clerkUserId, 
                claim.getUser().getEmail()
        );
        
        log.info("Mapped Clerk ID {} to internal UUID {}", clerkUserId, internalUserId);
        
        // 2. Fusionner les URLs d'images avec des pipes (|)
        String imageUrl = null;
        if (claim.getClaim().getAttachments() != null && !claim.getClaim().getAttachments().isEmpty()) {
            imageUrl = claim.getClaim().getAttachments().stream()
                .map(attachment -> attachment.getUrl())
                .filter(url -> url != null && !url.isEmpty())
                .collect(Collectors.joining("|"));
            
            if (imageUrl.isEmpty()) {
                imageUrl = null;
            }
            
            log.debug("Merged {} image URLs: {}", claim.getClaim().getAttachments().size(), 
                    imageUrl != null ? imageUrl.substring(0, Math.min(100, imageUrl.length())) + "..." : "null");
        }

        return CitizenAlertCreatedEvent.builder()
            // ID externe de la réclamation (pour mapping Case Salesforce)
            .claimId(claim.getClaimId())      // ID unique depuis le système externe
            
            // Informations utilisateur
            .userId(internalUserId)           // UUID interne (mappé)
            .clerkUserId(clerkUserId)         // Clerk ID original (stocké pour référence)
            .email(claim.getUser().getEmail())
            .name(claim.getUser().getName())
            .phone(claim.getUser().getPhone())
            
            // Informations de la réclamation
            .serviceType(claim.getClaim().getServiceType())
            .title(claim.getClaim().getTitle())
            .description(claim.getClaim().getDescription())
            .priority(claim.getClaim().getPriority())
            
            // Localisation
            .address(claim.getClaim().getLocation().getAddress())
            .latitude(claim.getClaim().getLocation().getLatitude())
            .longitude(claim.getClaim().getLocation().getLongitude())
            
            // QR Code et type de patrimoine
            .qrCode(claim.getClaim().getExtraData().getQrCode())
            .patrimoineType(claim.getClaim().getExtraData().getPatrimoineType())
            
            // URLs d'images fusionnées avec pipe (|)
            .imageUrl(imageUrl)
            
            .build();
    }
}
