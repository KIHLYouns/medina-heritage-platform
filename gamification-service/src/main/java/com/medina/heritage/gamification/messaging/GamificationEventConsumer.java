package com.medina.heritage.gamification.messaging;

import com.medina.heritage.events.gamification.PointsEarnedEvent;
import com.medina.heritage.events.user.UserCreatedEvent;
import com.medina.heritage.events.user.UserDeletedEvent;
import com.medina.heritage.gamification.dto.request.AddPointsRequest;
import com.medina.heritage.gamification.dto.response.PointTransactionResponse;
import com.medina.heritage.gamification.dto.response.WalletResponse;
import com.medina.heritage.gamification.entity.Wallet;
import com.medina.heritage.gamification.repository.WalletRepository;
import com.medina.heritage.gamification.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Configuration class for gamification event consumers.
 * Listens to events from other services and processes them.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class GamificationEventConsumer {

    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final GamificationEventPublisher eventPublisher;

    // Points configuration
    private static final int WELCOME_POINTS = 100;
    private static final int REPORT_CREATED_POINTS = 50;
    private static final int REPORT_VALIDATED_BONUS_POINTS = 100;

    /**
     * Consumer for UserCreatedEvent.
     * Creates a wallet and awards welcome points when a new user registers.
     */
    @Bean
    public Consumer<UserCreatedEvent> userCreatedConsumer() {
        return event -> {
            log.info("Received UserCreatedEvent for user: {} ({})", event.getUserId(), event.getEmail());
            try {
                // Convert userId string from event (UUID string) to UUID
                UUID userId = UUID.fromString(event.getUserId());
                
                // Create wallet for the new user
                WalletResponse wallet = walletService.getOrCreateWallet(userId);
                log.info("Wallet created for user: {}", userId);
                
                // Award welcome points
                AddPointsRequest welcomePoints = createAddPointsRequest(
                        userId, WELCOME_POINTS, "WELCOME_BONUS",
                        "Points de bienvenue pour l'inscription",
                        "USER_REGISTRATION", userId);
                
                var transaction = walletService.addPoints(welcomePoints);
                log.info("Welcome points awarded: {} points to user {}", WELCOME_POINTS, userId);
                
                // Publish PointsEarnedEvent (no numeric referenceId available for user registration)
                publishPointsEarned(userId, WELCOME_POINTS, transaction.getBalanceAfter(), 
                    "USER_REGISTRATION", null, "Points de bienvenue");
                
            } catch (Exception e) {
                log.error("Error processing UserCreatedEvent for user: {}. Error: {}", 
                        event.getUserId(), e.getMessage(), e);
            }
        };
    }
    
    /**
     * Helper method to create AddPointsRequest.
     */
    private AddPointsRequest createAddPointsRequest(UUID userId, int points, String reasonCode,
            String description, String referenceType, UUID referenceId) {
        AddPointsRequest request = new AddPointsRequest();
        request.setUserId(userId);
        request.setPoints(points);
        request.setReasonCode(reasonCode);
        request.setDescription(description);
        request.setReferenceType(referenceType);
        request.setReferenceId(referenceId);
        return request;
    }
    
    /**
     * Helper method to publish PointsEarnedEvent.
     */
    private void publishPointsEarned(UUID userId, int points, int newBalance, 
            String referenceType, Long referenceId, String description) {
        try {
            Wallet wallet = walletRepository.findByUserId(userId).orElse(null);
            if (wallet != null) {
                PointsEarnedEvent event = PointsEarnedEvent.builder()
                        .userId(userId.getMostSignificantBits())
                        .pointsEarned(points)
                        .newBalance(newBalance)
                        .newTotalEarned(wallet.getTotalEarned())
                        .referenceType(referenceType)
                        .referenceId(referenceId)
                        .description(description)
                        .build();
                
                eventPublisher.publishPointsEarned(event);
            }
        } catch (Exception e) {
            log.error("Error publishing PointsEarnedEvent: {}", e.getMessage());
        }
    }
    
}
