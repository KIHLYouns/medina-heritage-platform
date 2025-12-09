package com.medina.heritage.integration.repository;

import com.medina.heritage.integration.entity.IdMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour gérer les mappings entre IDs locaux et IDs Salesforce.
 */
public interface IdMappingRepository extends JpaRepository<IdMapping, UUID> {

    /**
     * Trouve le mapping Salesforce pour une entité locale donnée.
     *
     * @param entityType Type de l'entité (USER, BUILDING, etc.)
     * @param entityId   ID local de l'entité
     * @return Le mapping s'il existe
     */
    Optional<IdMapping> findByLocalEntityTypeAndLocalEntityId(String entityType, UUID entityId);
}
