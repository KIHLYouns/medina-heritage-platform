package com.medina.heritage.events.gamification;

import com.medina.heritage.events.base.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a user earns points.
 * Consumers: notification-service (notify user of points earned)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PointsEarnedEvent extends BaseEvent {
    
    public static final String EVENT_TYPE = "points.earned";
    public static final String ROUTING_KEY = "gamification.points.earned";
    
    private Long userId;
    private Integer pointsEarned;
    private Integer newBalance;
    private Integer newTotalEarned;
    private String referenceType; // REPORT, VALIDATION, BADGE, etc.
    private Long referenceId;
    private String description;
    
    public PointsEarnedEvent initializeDefaults() {
        initializeEvent("gamification-service");
        return this;
    }
}
