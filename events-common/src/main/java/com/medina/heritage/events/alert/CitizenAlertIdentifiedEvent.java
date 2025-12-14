package com.medina.heritage.events.alert;

import com.medina.heritage.events.base.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Événement publié par patrimoine-service après identification
 * du bâtiment patrimonial via le QR code d'une alerte citoyenne.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CitizenAlertIdentifiedEvent extends BaseEvent {
    
    public static final String EVENT_TYPE = "building.citizen.alert.identified";

    private UUID userId;
    private UUID buildingId;
    private String buildingCode;
    private String buildingName;
    private String imageUrl;
    private String description;
    private Double longitude;
    private Double latitude;
    
    public CitizenAlertIdentifiedEvent initializeDefaults() {
        initializeEvent("patrimoine-service");
        return this;
    }
}
