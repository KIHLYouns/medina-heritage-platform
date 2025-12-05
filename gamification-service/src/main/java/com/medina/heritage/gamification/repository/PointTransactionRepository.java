package com.medina.heritage.gamification.repository;

import com.medina.heritage.gamification.entity.PointTransaction;
import com.medina.heritage.gamification.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour l'entité PointTransaction.
 */
@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, UUID> {

    /**
     * Récupère toutes les transactions d'un utilisateur.
     */
    List<PointTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Récupère les transactions d'un utilisateur par type.
     */
    List<PointTransaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(
            UUID userId, TransactionType transactionType);

    /**
     * Récupère les transactions par code de raison.
     */
    List<PointTransaction> findByUserIdAndReasonCode(UUID userId, String reasonCode);

    /**
     * Récupère les transactions liées à une entité spécifique.
     */
    List<PointTransaction> findByReferenceIdAndReferenceType(UUID referenceId, String referenceType);

    /**
     * Récupère les transactions dans une période donnée.
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.userId = :userId " +
           "AND pt.createdAt >= :startDate AND pt.createdAt <= :endDate " +
           "ORDER BY pt.createdAt DESC")
    List<PointTransaction> findByUserIdAndPeriod(
            @Param("userId") UUID userId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

    /**
     * Calcule la somme des points gagnés par un utilisateur.
     */
    @Query("SELECT COALESCE(SUM(pt.points), 0) FROM PointTransaction pt " +
           "WHERE pt.userId = :userId AND pt.transactionType = 'CREDIT'")
    Integer sumPointsEarnedByUserId(@Param("userId") UUID userId);

    /**
     * Calcule la somme des points dépensés par un utilisateur.
     */
    @Query("SELECT COALESCE(SUM(pt.points), 0) FROM PointTransaction pt " +
           "WHERE pt.userId = :userId AND pt.transactionType = 'DEBIT'")
    Integer sumPointsSpentByUserId(@Param("userId") UUID userId);

    /**
     * Compte le nombre de transactions par code de raison pour un utilisateur.
     */
    @Query("SELECT pt.reasonCode, COUNT(pt) FROM PointTransaction pt " +
           "WHERE pt.userId = :userId GROUP BY pt.reasonCode")
    List<Object[]> countTransactionsByReasonCode(@Param("userId") UUID userId);

    /**
     * Récupère les dernières transactions d'un utilisateur.
     */
    List<PointTransaction> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Vérifie si une transaction existe pour une référence donnée.
     */
    boolean existsByReferenceIdAndReferenceTypeAndReasonCode(
            UUID referenceId, String referenceType, String reasonCode);
}
