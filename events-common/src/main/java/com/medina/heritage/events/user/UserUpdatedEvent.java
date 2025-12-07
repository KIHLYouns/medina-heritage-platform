package com.medina.heritage.events.user;

import com.medina.heritage.events.base.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * Event published when a user is updated.
 * Consumers: integration-salesforce-service (sync contact updates)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserUpdatedEvent extends BaseEvent {
    
    public static final String EVENT_TYPE = "user.updated";
    public static final String ROUTING_KEY = "user.updated";
    
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Set<String> roles;
    private String sfContactId; // Salesforce Contact ID if exists
    
    public UserUpdatedEvent initializeDefaults() {
        initializeEvent("user-auth-service");
        return this;
    }
}
