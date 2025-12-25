package com.medina.heritage.events.alert;

import com.medina.heritage.events.base.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Événement publié par le service externe de réclamation
 * quand un citoyen signale un incident sur un bâtiment patrimonial.
 * Contient toutes les informations de la réclamation reçue via Kafka.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CitizenAlertCreatedEvent extends BaseEvent {
    
    public static final String EVENT_TYPE = "building.citizen.alert.created";

    // Informations utilisateur
    private UUID userId;             // UUID interne de l'utilisateur (après mapping Clerk -> UUID)
    private String clerkUserId;      // Clerk User ID original (ex: user_36sUYTLEqPF4kWjVDbeKVUDsvgK)
    private String email;
    private String name;
    private String phone;
    
    // ID externe de la réclamation (pour mapper case Salesforce)
    private String claimId;          // ID unique depuis le système externe (ex: claim_123456)
    
    // Informations de la réclamation
    private String serviceType;      // "patrimoine"
    private String title;            // Titre de la réclamation
    private String description;      // Description détaillée
    private String priority;         // "low", "medium", "high"
    
    // Localisation
    private String address;          // Adresse complète
    private Double longitude;
    private Double latitude;
    
    // QR Code et type de patrimoine
    private String qrCode;           // Code du bâtiment (ex: PAT-001)
    private String patrimoineType;   // "monument", "building", etc.
    
    // Images (URLs fusionnées par pipe |)
    private String imageUrl;         // URLs d'images séparées par | (ex: url1|url2|url3)
    
    public CitizenAlertCreatedEvent initializeDefaults() {
        initializeEvent("external-alert-service");
        return this;
    }
}
