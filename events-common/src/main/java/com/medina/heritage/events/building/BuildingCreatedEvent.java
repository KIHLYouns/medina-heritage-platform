package com.medina.heritage.events.building;

import com.medina.heritage.events.base.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BuildingCreatedEvent extends BaseEvent {

    // Constantes utiles pour le routage RabbitMQ (optionnel mais propre)
    public static final String EVENT_TYPE = "building.created";

    // Données métier nécessaires pour Salesforce
    private String buildingId; // UUID en String
    private String code;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;

    // Méthode utilitaire pour initialiser rapidement
    public BuildingCreatedEvent initializeDefaults() {
        this.initializeEvent("patrimoine-service"); // On définit la source ici
        return this;
    }
}