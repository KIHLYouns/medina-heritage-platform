package com.medina.heritage.patrimoine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * Service pour récupérer l'UUID interne d'un utilisateur via son Clerk ID.
 * 
 * Appelle le user-auth-service pour faire la correspondance Clerk ID → UUID
 * puisque la table users est gérée par user-auth-service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserMappingService {

    private final RestTemplate restTemplate;
    private static final String USER_AUTH_SERVICE_URL = "http://localhost:8081/api/users";

    /**
     * Récupère l'UUID interne correspondant à un Clerk ID.
     * Appelle le user-auth-service pour rechercher dans la table users.
     * 
     * @param clerkId Le Clerk User ID (ex: user_36sUYTLEqPF4kWjVDbeKVUDsvgK)
     * @param email L'email de l'utilisateur (pour logging)
     * @return L'UUID interne de l'utilisateur
     * @throws RuntimeException si l'utilisateur n'existe pas dans le système
     */
    public UUID getOrCreateUserUUID(String clerkId, String email) {
        try {
            // Appeller le user-auth-service pour rechercher par Clerk ID
            String url = USER_AUTH_SERVICE_URL + "/by-clerk-id/" + clerkId;
            
            var response = restTemplate.getForObject(url, java.util.Map.class);
            
            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> data = (java.util.Map<String, Object>) response.get("data");
                
                if (data != null && data.containsKey("id")) {
                    UUID userId = UUID.fromString((String) data.get("id"));
                    log.debug("Found existing user for clerkId: {} -> UUID: {}", clerkId, userId);
                    return userId;
                }
            }
            
            throw new RuntimeException("User not found for clerkId: " + clerkId);
            
        } catch (Exception e) {
            log.error("Error finding user for clerkId {}: {}", clerkId, e.getMessage());
            throw new RuntimeException("Failed to find user with Clerk ID: " + clerkId, e);
        }
    }

    /**
     * Récupère l'UUID interne sans lever d'exception si non existant.
     * 
     * @param clerkId Le Clerk User ID
     * @return Optional contenant l'UUID si trouvé, sinon Optional.empty()
     */
    public Optional<UUID> getUserUUID(String clerkId) {
        try {
            return Optional.of(getOrCreateUserUUID(clerkId, null));
        } catch (RuntimeException e) {
            log.debug("User not found for clerkId: {}", clerkId);
            return Optional.empty();
        }
    }
}
