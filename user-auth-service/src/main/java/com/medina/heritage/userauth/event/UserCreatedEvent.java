package com.medina.heritage.userauth.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Événement émis lors de la création d'un utilisateur.
 * Utilisé pour synchroniser avec d'autres services (Salesforce, Gamification).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Set<String> roles;
    private OffsetDateTime createdAt;

    public static UserCreatedEvent fromUser(UUID userId, String email, String firstName, 
                                            String lastName, String phoneNumber, 
                                            Set<String> roles, OffsetDateTime createdAt) {
        return new UserCreatedEvent(userId, email, firstName, lastName, phoneNumber, roles, createdAt);
    }
}
