package com.medina.heritage.media.dto.request;

import com.medina.heritage.media.enums.EntityType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO pour la requête d'upload de média.
 * Le fichier est envoyé séparément en MultipartFile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadMediaRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    /**
     * Type d'entité associée (optionnel).
     */
    private EntityType entityType;

    /**
     * ID de l'entité associée (optionnel).
     */
    private UUID entityId;
}
