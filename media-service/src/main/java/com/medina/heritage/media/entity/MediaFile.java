package com.medina.heritage.media.entity;

import com.medina.heritage.media.enums.EntityType;
import com.medina.heritage.media.enums.MediaStatus;
import com.medina.heritage.media.enums.MediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "media_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaFile {

    @Id
    private UUID id;

    /**
     * Propriétaire du fichier (ID utilisateur de UserAuthService).
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Type d'entité associée (optionnel).
     */
    @Column(name = "entity_type")
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    /**
     * ID de l'entité associée (optionnel).
     */
    @Column(name = "entity_id")
    private UUID entityId;

    /**
     * Nom du bucket S3.
     */
    @Column(name = "bucket_name", nullable = false, length = 100)
    private String bucketName;

    /**
     * Clé du fichier dans S3.
     * Exemple: reports/2025/12/uuid-filename.jpg
     */
    @Column(name = "file_key", nullable = false, length = 500, unique = true)
    private String fileKey;

    /**
     * URL publique permanente du fichier.
     * Format: https://{bucket}.s3.{region}.amazonaws.com/{fileKey}
     */
    @Column(name = "public_url", nullable = false, length = 1000)
    private String publicUrl;

    /**
     * Nom original du fichier uploadé.
     */
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /**
     * Type MIME du fichier.
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * Type de média (IMAGE, VIDEO, DOCUMENT).
     */
    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    /**
     * Taille du fichier en octets.
     */
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    /**
     * Statut du fichier.
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MediaStatus status = MediaStatus.ACTIVE;

    /**
     * Date de création.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Date de suppression logique.
     */
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = MediaStatus.ACTIVE;
        }
    }
}
