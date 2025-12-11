package com.medina.heritage.events.user;

import com.medina.heritage.events.base.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;
// import java.util.UUID; // Optionnel si vous préférez le type UUID strict

/**
 * Event published when a new user is created.
 * Consumers: gamification-service (create wallet),
 * integration-salesforce-service (sync contact)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserCreatedEvent extends BaseEvent {

    public static final String EVENT_TYPE = "user.created";
    public static final String ROUTING_KEY = "user.created";

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Set<String> roles;

    public UserCreatedEvent initializeDefaults() {
        initializeEvent("user-auth-service");
        return this;
    }
}