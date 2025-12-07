package com.medina.heritage.gamification.messaging;

import com.medina.heritage.events.gamification.PointsEarnedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

/**
 * Service for publishing gamification-related events to the message broker.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationEventPublisher {

    private final StreamBridge streamBridge;

    private static final String POINTS_EARNED_BINDING = "pointsEarnedSupplier-out-0";

    /**
     * Publishes a PointsEarnedEvent when a user earns points.
     */
    public void publishPointsEarned(PointsEarnedEvent event) {
        event.initializeDefaults();
        log.info("Publishing PointsEarnedEvent for user: {}, points: {}", event.getUserId(), event.getPointsEarned());
        boolean sent = streamBridge.send(POINTS_EARNED_BINDING, event);
        if (sent) {
            log.debug("PointsEarnedEvent sent successfully for user: {}", event.getUserId());
        } else {
            log.error("Failed to send PointsEarnedEvent for user: {}", event.getUserId());
        }
    }
    
}
