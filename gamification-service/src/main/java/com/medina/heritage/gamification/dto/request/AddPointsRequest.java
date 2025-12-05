package com.medina.heritage.gamification.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO pour ajouter des points à un utilisateur.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPointsRequest {

    /**
     * ID de l'utilisateur.
     */
    @NotNull(message = "User ID is required")
    private UUID userId;

    /**
     * Nombre de points à ajouter.
     */
    @NotNull(message = "Points amount is required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;

    /**
     * Code de raison pour la transaction.
     */
    @NotBlank(message = "Reason code is required")
    private String reasonCode;

    /**
     * Description optionnelle.
     */
    private String description;

    /**
     * ID de référence optionnel (ex: ID du signalement).
     */
    private UUID referenceId;

    /**
     * Type de référence optionnel (ex: "REPORT").
     */
    private String referenceType;

    /**
     * ID Salesforce optionnel.
     */
    private String sfCaseId;
}
