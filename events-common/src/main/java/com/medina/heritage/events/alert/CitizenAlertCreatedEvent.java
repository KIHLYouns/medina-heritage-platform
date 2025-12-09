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
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CitizenAlertCreatedEvent extends BaseEvent {
    
    public static final String EVENT_TYPE = "building.citizen.alert.created";

    private UUID userId;
    private String qrCode;
    private String imageUrl;
    private String description;
    private Double longitude;
    private Double latitude;
    
    public CitizenAlertCreatedEvent initializeDefaults() {
        initializeEvent("external-alert-service");
        return this;
    }
}
