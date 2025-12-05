package com.medina.heritage.gamification.dto.response;

import com.medina.heritage.gamification.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour une transaction de points.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointTransactionResponse {

    /**
     * ID de la transaction.
     */
    private UUID id;

    /**
     * ID de l'utilisateur.
     */
    private UUID userId;

    /**
     * Montant de la transaction.
     */
    private Integer points;

    /**
     * Type de transaction (CREDIT ou DEBIT).
     */
    private TransactionType transactionType;

    /**
     * Code de raison.
     */
    private String reasonCode;

    /**
     * Description de la transaction.
     */
    private String description;

    /**
     * ID de référence.
     */
    private UUID referenceId;

    /**
     * Type de référence.
     */
    private String referenceType;

    /**
     * Solde après la transaction.
     */
    private Integer balanceAfter;

    /**
     * Date de création.
     */
    private OffsetDateTime createdAt;
}
