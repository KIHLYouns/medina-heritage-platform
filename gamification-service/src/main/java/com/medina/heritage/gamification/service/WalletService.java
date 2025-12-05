package com.medina.heritage.gamification.service;

import com.medina.heritage.gamification.dto.request.AddPointsRequest;
import com.medina.heritage.gamification.dto.request.DeductPointsRequest;
import com.medina.heritage.gamification.dto.response.PointTransactionResponse;
import com.medina.heritage.gamification.dto.response.WalletResponse;
import com.medina.heritage.gamification.entity.PointTransaction;
import com.medina.heritage.gamification.entity.Wallet;
import com.medina.heritage.gamification.enums.TransactionType;
import com.medina.heritage.gamification.exception.DuplicateTransactionException;
import com.medina.heritage.gamification.exception.InsufficientBalanceException;
import com.medina.heritage.gamification.exception.WalletNotFoundException;
import com.medina.heritage.gamification.mapper.PointTransactionMapper;
import com.medina.heritage.gamification.mapper.WalletMapper;
import com.medina.heritage.gamification.repository.PointTransactionRepository;
import com.medina.heritage.gamification.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service principal de gestion des wallets.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final PointTransactionRepository transactionRepository;
    private final WalletMapper walletMapper;
    private final PointTransactionMapper transactionMapper;

    /**
     * Récupère le wallet d'un utilisateur.
     * Crée un nouveau wallet si l'utilisateur n'en a pas.
     */
    @Transactional
    public WalletResponse getOrCreateWallet(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new wallet for user: {}", userId);
                    Wallet newWallet = new Wallet(userId);
                    return walletRepository.save(newWallet);
                });
        return walletMapper.toWalletResponse(wallet);
    }

    /**
     * Récupère le wallet d'un utilisateur (sans création automatique).
     */
    public WalletResponse getWallet(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
        return walletMapper.toWalletResponse(wallet);
    }

    /**
     * Ajoute des points au wallet d'un utilisateur.
     */
    @Transactional
    public PointTransactionResponse addPoints(AddPointsRequest request) {
        // Vérifier les doublons si une référence est fournie
        if (request.getReferenceId() != null && request.getReferenceType() != null) {
            boolean exists = transactionRepository.existsByReferenceIdAndReferenceTypeAndReasonCode(
                    request.getReferenceId(), request.getReferenceType(), request.getReasonCode());
            if (exists) {
                throw new DuplicateTransactionException(
                        request.getReferenceId(), request.getReferenceType(), request.getReasonCode());
            }
        }

        // Récupérer ou créer le wallet
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseGet(() -> {
                    log.info("Creating new wallet for user: {}", request.getUserId());
                    return walletRepository.save(new Wallet(request.getUserId()));
                });

        // Ajouter les points
        wallet.addPoints(request.getPoints());
        walletRepository.save(wallet);

        // Créer la transaction
        PointTransaction transaction = new PointTransaction();
        transaction.setUserId(request.getUserId());
        transaction.setPoints(request.getPoints());
        transaction.setTransactionType(TransactionType.CREDIT);
        transaction.setReasonCode(request.getReasonCode());
        transaction.setDescription(request.getDescription());
        transaction.setReferenceId(request.getReferenceId());
        transaction.setReferenceType(request.getReferenceType());
        transaction.setSfCaseId(request.getSfCaseId());
        transaction.setBalanceAfter(wallet.getBalance());

        PointTransaction savedTransaction = transactionRepository.save(transaction);
        log.info("Points added: userId={}, points={}, reason={}, newBalance={}", 
                request.getUserId(), request.getPoints(), request.getReasonCode(), wallet.getBalance());

        return transactionMapper.toPointTransactionResponse(savedTransaction);
    }

    /**
     * Déduit des points du wallet d'un utilisateur.
     */
    @Transactional
    public PointTransactionResponse deductPoints(DeductPointsRequest request) {
        // Récupérer le wallet
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new WalletNotFoundException(request.getUserId()));

        // Vérifier le solde
        if (wallet.getBalance() < request.getPoints()) {
            throw new InsufficientBalanceException(request.getPoints(), wallet.getBalance());
        }

        // Déduire les points
        wallet.deductPoints(request.getPoints());
        walletRepository.save(wallet);

        // Créer la transaction
        PointTransaction transaction = new PointTransaction();
        transaction.setUserId(request.getUserId());
        transaction.setPoints(request.getPoints());
        transaction.setTransactionType(TransactionType.DEBIT);
        transaction.setReasonCode(request.getReasonCode());
        transaction.setDescription(request.getDescription());
        transaction.setReferenceId(request.getReferenceId());
        transaction.setReferenceType(request.getReferenceType());
        transaction.setSfCaseId(request.getSfCaseId());
        transaction.setBalanceAfter(wallet.getBalance());

        PointTransaction savedTransaction = transactionRepository.save(transaction);
        log.info("Points deducted: userId={}, points={}, reason={}, newBalance={}", 
                request.getUserId(), request.getPoints(), request.getReasonCode(), wallet.getBalance());

        return transactionMapper.toPointTransactionResponse(savedTransaction);
    }

    /**
     * Récupère le solde d'un utilisateur.
     */
    public Integer getBalance(UUID userId) {
        return walletRepository.findByUserId(userId)
                .map(Wallet::getBalance)
                .orElse(0);
    }

    /**
     * Récupère le niveau d'un utilisateur.
     */
    public Integer getLevel(UUID userId) {
        return walletRepository.findByUserId(userId)
                .map(Wallet::getLevel)
                .orElse(1);
    }

    /**
     * Récupère le rang d'un utilisateur.
     */
    public Long getUserRank(UUID userId) {
        if (!walletRepository.existsByUserId(userId)) {
            return null;
        }
        return walletRepository.getUserRank(userId);
    }

    /**
     * Vérifie si un wallet existe pour un utilisateur.
     */
    public boolean walletExists(UUID userId) {
        return walletRepository.existsByUserId(userId);
    }
}
