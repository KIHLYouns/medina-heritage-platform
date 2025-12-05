package com.medina.heritage.gamification.repository;

import com.medina.heritage.gamification.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'entité Wallet.
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    /**
     * Recherche un wallet par l'ID de l'utilisateur.
     */
    Optional<Wallet> findByUserId(UUID userId);

    /**
     * Vérifie si un wallet existe pour un utilisateur.
     */
    boolean existsByUserId(UUID userId);

    /**
     * Récupère les wallets avec le plus de points (leaderboard).
     */
    @Query("SELECT w FROM Wallet w ORDER BY w.totalEarned DESC")
    List<Wallet> findTopByTotalEarnedOrderByTotalEarnedDesc();

    /**
     * Récupère les wallets avec le niveau le plus élevé.
     */
    @Query("SELECT w FROM Wallet w ORDER BY w.level DESC, w.totalEarned DESC")
    List<Wallet> findTopByLevelOrderByLevelDescTotalEarnedDesc();

    /**
     * Compte le nombre d'utilisateurs par niveau.
     */
    @Query("SELECT w.level, COUNT(w) FROM Wallet w GROUP BY w.level ORDER BY w.level")
    List<Object[]> countUsersByLevel();

    /**
     * Récupère le rang d'un utilisateur basé sur les points totaux gagnés.
     */
    @Query("SELECT COUNT(w) + 1 FROM Wallet w WHERE w.totalEarned > (SELECT w2.totalEarned FROM Wallet w2 WHERE w2.userId = :userId)")
    Long getUserRank(@Param("userId") UUID userId);

    /**
     * Récupère les wallets d'un niveau spécifique.
     */
    List<Wallet> findByLevelOrderByTotalEarnedDesc(Integer level);
}
