package com.medina.heritage.events.building;

import com.medina.heritage.events.base.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event emitted when a building is updated.
 * Used for synchronizing building updates to external systems (e.g., Salesforce).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BuildingUpdatedEvent extends BaseEvent {

    // Constant for event routing
    public static final String EVENT_TYPE = "building.updated";

    // Business data for Salesforce sync
    private String buildingId; // UUID as String
    private String code;
    private String name;
    private String address;
    private String description;
    private Double latitude;
    private Double longitude;
    private String imageUrl;

    /**
     * Utility method to initialize event defaults
     */
    public BuildingUpdatedEvent initializeDefaults() {
        this.initializeEvent("patrimoine-service");
        return this;
    }
}
