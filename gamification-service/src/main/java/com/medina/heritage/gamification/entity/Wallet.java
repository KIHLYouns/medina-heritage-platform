package com.medina.heritage.gamification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entité représentant le portefeuille de points d'un utilisateur.
 * L'ID du wallet correspond à l'ID de l'utilisateur de UserAuthService.
 */
@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    /**
     * ID de l'utilisateur (correspond à user_id dans UserAuthService).
     */
    @Id
    @Column(name = "user_id")
    private UUID userId;

    /**
     * Solde actuel de points.
     */
    @Column(name = "balance", nullable = false)
    private Integer balance = 0;

    /**
     * Niveau actuel de l'utilisateur.
     */
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    /**
     * Total des points gagnés (historique).
     */
    @Column(name = "total_earned", nullable = false)
    private Integer totalEarned = 0;

    /**
     * Total des points dépensés (historique).
     */
    @Column(name = "total_spent", nullable = false)
    private Integer totalSpent = 0;

    /**
     * Date de dernière mise à jour.
     */
    @UpdateTimestamp
    @Column(name = "last_updated_at")
    private OffsetDateTime lastUpdatedAt;

    /**
     * Constructeur pour créer un nouveau wallet avec un userId.
     */
    public Wallet(UUID userId) {
        this.userId = userId;
        this.balance = 0;
        this.level = 1;
        this.totalEarned = 0;
        this.totalSpent = 0;
    }

    /**
     * Ajoute des points au wallet.
     */
    public void addPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points to add cannot be negative");
        }
        this.balance += points;
        this.totalEarned += points;
        updateLevel();
    }

    /**
     * Retire des points du wallet.
     */
    public void deductPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points to deduct cannot be negative");
        }
        if (this.balance < points) {
            throw new IllegalStateException("Insufficient balance");
        }
        this.balance -= points;
        this.totalSpent += points;
    }

    /**
     * Met à jour le niveau en fonction du total des points gagnés.
     * Formule simple: 1 niveau tous les 1000 points gagnés.
     */
    private void updateLevel() {
        this.level = Math.max(1, (this.totalEarned / 1000) + 1);
    }
}
