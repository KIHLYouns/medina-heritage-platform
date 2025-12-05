package com.medina.heritage.gamification.controller;

import com.medina.heritage.gamification.dto.response.ApiResponse;
import com.medina.heritage.gamification.dto.response.PointTransactionResponse;
import com.medina.heritage.gamification.enums.TransactionType;
import com.medina.heritage.gamification.service.PointTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour les opérations sur les transactions de points.
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class PointTransactionController {

    private final PointTransactionService transactionService;

    /**
     * Récupère toutes les transactions d'un utilisateur.
     * GET /api/transactions/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PointTransactionResponse>>> getTransactionsByUser(
            @PathVariable UUID userId) {
        List<PointTransactionResponse> response = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved", response));
    }

    /**
     * Récupère les transactions d'un utilisateur par type.
     * GET /api/transactions/user/{userId}/type/{type}
     */
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<ApiResponse<List<PointTransactionResponse>>> getTransactionsByUserAndType(
            @PathVariable UUID userId,
            @PathVariable TransactionType type) {
        List<PointTransactionResponse> response = transactionService
                .getTransactionsByUserAndType(userId, type);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved", response));
    }

    /**
     * Récupère les dernières transactions d'un utilisateur (10 dernières).
     * GET /api/transactions/user/{userId}/recent
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<ApiResponse<List<PointTransactionResponse>>> getRecentTransactions(
            @PathVariable UUID userId) {
        List<PointTransactionResponse> response = transactionService.getRecentTransactions(userId);
        return ResponseEntity.ok(ApiResponse.success("Recent transactions retrieved", response));
    }

    /**
     * Récupère les transactions dans une période donnée.
     * GET /api/transactions/user/{userId}/period
     */
    @GetMapping("/user/{userId}/period")
    public ResponseEntity<ApiResponse<List<PointTransactionResponse>>> getTransactionsByPeriod(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        List<PointTransactionResponse> response = transactionService
                .getTransactionsByPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved", response));
    }

    /**
     * Récupère les transactions liées à une entité.
     * GET /api/transactions/reference/{referenceType}/{referenceId}
     */
    @GetMapping("/reference/{referenceType}/{referenceId}")
    public ResponseEntity<ApiResponse<List<PointTransactionResponse>>> getTransactionsByReference(
            @PathVariable String referenceType,
            @PathVariable UUID referenceId) {
        List<PointTransactionResponse> response = transactionService
                .getTransactionsByReference(referenceId, referenceType);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved", response));
    }

    /**
     * Récupère une transaction par son ID.
     * GET /api/transactions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PointTransactionResponse>> getTransaction(@PathVariable UUID id) {
        PointTransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved", response));
    }

    /**
     * Récupère le total des points gagnés par un utilisateur.
     * GET /api/transactions/user/{userId}/total-earned
     */
    @GetMapping("/user/{userId}/total-earned")
    public ResponseEntity<ApiResponse<Integer>> getTotalPointsEarned(@PathVariable UUID userId) {
        Integer total = transactionService.getTotalPointsEarned(userId);
        return ResponseEntity.ok(ApiResponse.success("Total points earned retrieved", total));
    }

    /**
     * Récupère le total des points dépensés par un utilisateur.
     * GET /api/transactions/user/{userId}/total-spent
     */
    @GetMapping("/user/{userId}/total-spent")
    public ResponseEntity<ApiResponse<Integer>> getTotalPointsSpent(@PathVariable UUID userId) {
        Integer total = transactionService.getTotalPointsSpent(userId);
        return ResponseEntity.ok(ApiResponse.success("Total points spent retrieved", total));
    }
}
