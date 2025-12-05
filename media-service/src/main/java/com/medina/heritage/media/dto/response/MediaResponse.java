package com.medina.heritage.media.dto.response;

import com.medina.heritage.media.enums.EntityType;
import com.medina.heritage.media.enums.MediaStatus;
import com.medina.heritage.media.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour un média.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponse {

    private UUID id;
    private UUID userId;
    private EntityType entityType;
    private UUID entityId;
    private String originalFilename;
    private String mimeType;
    private MediaType mediaType;
    private Long fileSizeBytes;
    private MediaStatus status;
    private OffsetDateTime createdAt;

    /**
     * URL publique permanente pour accéder au fichier.
     */
    private String url;
}
