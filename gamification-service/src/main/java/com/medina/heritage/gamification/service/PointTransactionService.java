package com.medina.heritage.gamification.service;

import com.medina.heritage.gamification.dto.response.PointTransactionResponse;
import com.medina.heritage.gamification.entity.PointTransaction;
import com.medina.heritage.gamification.enums.TransactionType;
import com.medina.heritage.gamification.mapper.PointTransactionMapper;
import com.medina.heritage.gamification.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service de gestion des transactions de points.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointTransactionService {

    private final PointTransactionRepository transactionRepository;
    private final PointTransactionMapper transactionMapper;

    /**
     * Récupère toutes les transactions d'un utilisateur.
     */
    public List<PointTransactionResponse> getTransactionsByUser(UUID userId) {
        List<PointTransaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return transactionMapper.toPointTransactionResponseList(transactions);
    }

    /**
     * Récupère les transactions d'un utilisateur par type.
     */
    public List<PointTransactionResponse> getTransactionsByUserAndType(UUID userId, TransactionType type) {
        List<PointTransaction> transactions = transactionRepository
                .findByUserIdAndTransactionTypeOrderByCreatedAtDesc(userId, type);
        return transactionMapper.toPointTransactionResponseList(transactions);
    }

    /**
     * Récupère les dernières transactions d'un utilisateur (10 dernières).
     */
    public List<PointTransactionResponse> getRecentTransactions(UUID userId) {
        List<PointTransaction> transactions = transactionRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        return transactionMapper.toPointTransactionResponseList(transactions);
    }

    /**
     * Récupère les transactions d'un utilisateur dans une période donnée.
     */
    public List<PointTransactionResponse> getTransactionsByPeriod(
            UUID userId, OffsetDateTime startDate, OffsetDateTime endDate) {
        List<PointTransaction> transactions = transactionRepository.findByUserIdAndPeriod(userId, startDate, endDate);
        return transactionMapper.toPointTransactionResponseList(transactions);
    }

    /**
     * Récupère les transactions liées à une entité.
     */
    public List<PointTransactionResponse> getTransactionsByReference(UUID referenceId, String referenceType) {
        List<PointTransaction> transactions = transactionRepository.findByReferenceIdAndReferenceType(referenceId, referenceType);
        return transactionMapper.toPointTransactionResponseList(transactions);
    }

    /**
     * Récupère une transaction par son ID.
     */
    public PointTransactionResponse getTransactionById(UUID id) {
        PointTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
        return transactionMapper.toPointTransactionResponse(transaction);
    }

    /**
     * Calcule le total des points gagnés par un utilisateur.
     */
    public Integer getTotalPointsEarned(UUID userId) {
        return transactionRepository.sumPointsEarnedByUserId(userId);
    }

    /**
     * Calcule le total des points dépensés par un utilisateur.
     */
    public Integer getTotalPointsSpent(UUID userId) {
        return transactionRepository.sumPointsSpentByUserId(userId);
    }
}
