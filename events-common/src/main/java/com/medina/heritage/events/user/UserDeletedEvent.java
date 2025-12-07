package com.medina.heritage.events.user;

import com.medina.heritage.events.base.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a user is deleted.
 * Consumers: gamification-service (soft-delete wallet), integration-salesforce-service (deactivate contact)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDeletedEvent extends BaseEvent {
    
    public static final String EVENT_TYPE = "user.deleted";
    public static final String ROUTING_KEY = "user.deleted";
    
    private Long userId;
    private String email;
    private String sfContactId; // Salesforce Contact ID if exists
    
    public UserDeletedEvent initializeDefaults() {
        initializeEvent("user-auth-service");
        return this;
    }
}
