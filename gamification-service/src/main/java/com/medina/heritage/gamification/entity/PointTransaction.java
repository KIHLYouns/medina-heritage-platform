package com.medina.heritage.gamification.entity;

import com.medina.heritage.gamification.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entité représentant une transaction de points (gain ou dépense).
 */
@Entity
@Table(name = "point_transactions", indexes = {
    @Index(name = "idx_point_transactions_user_id", columnList = "user_id"),
    @Index(name = "idx_point_transactions_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointTransaction {

    @Id
    private UUID id;

    /**
     * ID de l'utilisateur concerné.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Montant de la transaction (positif = gain, négatif = dépense).
     */
    @Column(name = "points", nullable = false)
    private Integer points;

    /**
     * Type de transaction (CREDIT ou DEBIT).
     */
    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    /**
     * Code de raison de la transaction.
     */
    @Column(name = "reason_code", nullable = false, length = 50)
    private String reasonCode;

    /**
     * Description lisible de la transaction.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * ID de l'entité liée (optionnel).
     * Par exemple: ID du signalement qui a généré les points.
     */
    @Column(name = "reference_id")
    private UUID referenceId;

    /**
     * Type d'entité liée (optionnel).
     * Par exemple: "REPORT", "BADGE", "REDEMPTION".
     */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * ID Salesforce associé (optionnel).
     */
    @Column(name = "sf_case_id", length = 18)
    private String sfCaseId;

    /**
     * Solde après la transaction.
     */
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    /**
     * Date de création de la transaction.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
