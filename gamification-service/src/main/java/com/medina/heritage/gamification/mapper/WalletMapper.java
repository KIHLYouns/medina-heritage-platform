package com.medina.heritage.gamification.mapper;

import com.medina.heritage.gamification.dto.response.WalletResponse;
import com.medina.heritage.gamification.entity.Wallet;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre Wallet Entity et DTOs.
 */
@Component
public class WalletMapper {

    private static final int POINTS_PER_LEVEL = 1000;

    /**
     * Convertit une entité Wallet vers un WalletResponse DTO.
     */
    public WalletResponse toWalletResponse(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        WalletResponse response = new WalletResponse();
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());
        response.setLevel(wallet.getLevel());
        response.setTotalEarned(wallet.getTotalEarned());
        response.setTotalSpent(wallet.getTotalSpent());
        response.setLastUpdatedAt(wallet.getLastUpdatedAt());

        // Calculer la progression vers le niveau suivant
        int pointsInCurrentLevel = wallet.getTotalEarned() % POINTS_PER_LEVEL;
        int pointsToNextLevel = POINTS_PER_LEVEL - pointsInCurrentLevel;
        int progressPercentage = (pointsInCurrentLevel * 100) / POINTS_PER_LEVEL;

        response.setPointsToNextLevel(pointsToNextLevel);
        response.setProgressPercentage(progressPercentage);

        return response;
    }
}
