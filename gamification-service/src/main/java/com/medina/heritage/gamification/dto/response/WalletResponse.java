package com.medina.heritage.gamification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour les informations d'un wallet.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {

    /**
     * ID de l'utilisateur.
     */
    private UUID userId;

    /**
     * Solde actuel de points.
     */
    private Integer balance;

    /**
     * Niveau actuel de l'utilisateur.
     */
    private Integer level;

    /**
     * Total des points gagnés.
     */
    private Integer totalEarned;

    /**
     * Total des points dépensés.
     */
    private Integer totalSpent;

    /**
     * Points restants pour atteindre le niveau suivant.
     */
    private Integer pointsToNextLevel;

    /**
     * Pourcentage de progression vers le niveau suivant.
     */
    private Integer progressPercentage;

    /**
     * Date de dernière mise à jour.
     */
    private OffsetDateTime lastUpdatedAt;
}
