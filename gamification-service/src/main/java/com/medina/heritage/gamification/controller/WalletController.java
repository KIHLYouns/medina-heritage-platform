package com.medina.heritage.gamification.controller;

import com.medina.heritage.gamification.dto.request.AddPointsRequest;
import com.medina.heritage.gamification.dto.request.DeductPointsRequest;
import com.medina.heritage.gamification.dto.response.ApiResponse;
import com.medina.heritage.gamification.dto.response.PointTransactionResponse;
import com.medina.heritage.gamification.dto.response.WalletResponse;
import com.medina.heritage.gamification.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Contrôleur REST pour les opérations sur les wallets.
 */
@Slf4j
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Récupère le wallet d'un utilisateur.
     * GET /api/wallets/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(@PathVariable UUID userId) {
        WalletResponse response = walletService.getOrCreateWallet(userId);
        return ResponseEntity.ok(ApiResponse.success("Wallet retrieved", response));
    }

    /**
     * Récupère le rang d'un utilisateur.
     * GET /api/wallets/{userId}/rank
     */
    @GetMapping("/{userId}/rank")
    public ResponseEntity<ApiResponse<Long>> getRank(@PathVariable UUID userId) {
        Long rank = walletService.getUserRank(userId);
        if (rank == null) {
            return ResponseEntity.ok(ApiResponse.success("User has no rank yet", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Rank retrieved", rank));
    }

    /**
     * Ajoute des points à un utilisateur.
     * POST /api/wallets/add-points
     */
    @PostMapping("/add-points")
    public ResponseEntity<ApiResponse<PointTransactionResponse>> addPoints(
            @Valid @RequestBody AddPointsRequest request) {
        PointTransactionResponse response = walletService.addPoints(request);
        log.info("Points added: userId={}, points={}", request.getUserId(), request.getPoints());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Points added successfully", response));
    }

    /**
     * Déduit des points d'un utilisateur.
     * POST /api/wallets/deduct-points
     */
    @PostMapping("/deduct-points")
    public ResponseEntity<ApiResponse<PointTransactionResponse>> deductPoints(
            @Valid @RequestBody DeductPointsRequest request) {
        PointTransactionResponse response = walletService.deductPoints(request);
        log.info("Points deducted: userId={}, points={}", request.getUserId(), request.getPoints());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Points deducted successfully", response));
    }

    /**
     * Vérifie si un wallet existe pour un utilisateur.
     * GET /api/wallets/{userId}/exists
     */
    @GetMapping("/{userId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> walletExists(@PathVariable UUID userId) {
        boolean exists = walletService.walletExists(userId);
        return ResponseEntity.ok(ApiResponse.success("Wallet existence checked", exists));
    }
}
