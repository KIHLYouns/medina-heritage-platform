package com.medina.heritage.media.repository;

import com.medina.heritage.media.entity.MediaFile;
import com.medina.heritage.media.enums.EntityType;
import com.medina.heritage.media.enums.MediaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {

    /**
     * Trouve un média actif par son ID.
     */
    Optional<MediaFile> findByIdAndStatus(UUID id, MediaStatus status);

    /**
     * Liste les médias d'un utilisateur.
     */
    List<MediaFile> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, MediaStatus status);

    /**
     * Liste les médias associés à une entité.
     */
    List<MediaFile> findByEntityTypeAndEntityIdAndStatusOrderByCreatedAtDesc(
            EntityType entityType, UUID entityId, MediaStatus status);

    /**
     * Vérifie si une clé de fichier existe déjà.
     */
    boolean existsByFileKey(String fileKey);

    /**
     * Compte les médias d'un utilisateur.
     */
    long countByUserIdAndStatus(UUID userId, MediaStatus status);
}
