package com.medina.heritage.userauth.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Événement émis lors de la mise à jour d'un utilisateur.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private OffsetDateTime updatedAt;
}
