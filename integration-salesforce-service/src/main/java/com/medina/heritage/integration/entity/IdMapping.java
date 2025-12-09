package com.medina.heritage.integration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Table de mapping entre les IDs locaux (nos microservices)
 * et les IDs Salesforce.
 */
@Entity
@Table(name = "id_mappings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Type d'entité locale: USER, BUILDING, IOT_ALERT, etc.
     */
    @Column(name = "local_entity_type", nullable = false, length = 50)
    private String localEntityType;

    /**
     * L'ID de l'entité dans notre système (userId, buildingId, etc.)
     */
    @Column(name = "local_entity_id", nullable = false)
    private UUID localEntityId;

    /**
     * L'ID Salesforce correspondant (ContactId, AssetId, CaseId, etc.)
     */
    @Column(name = "sf_entity_id", nullable = false, length = 18)
    private String sfEntityId;

    @Column(name = "last_sync_at")
    private OffsetDateTime lastSyncAt;

    @Column(name = "sync_status", length = 20)
    private String syncStatus;

    @PrePersist
    public void prePersist() {
        if (lastSyncAt == null) {
            lastSyncAt = OffsetDateTime.now();
        }
        if (syncStatus == null) {
            syncStatus = "SYNCED";
        }
    }
}
