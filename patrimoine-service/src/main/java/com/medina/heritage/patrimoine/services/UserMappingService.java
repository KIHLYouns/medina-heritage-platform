package com.medina.heritage.patrimoine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMappingService {

    private final RestTemplate restTemplate;

    // Inject the user service base URL from the application configuration
    @Value("${application.user-service.url:http://localhost:8081}")
    private String userServiceBaseUrl;

    public UUID getOrCreateUserUUID(String clerkId, String email) {
        try {
            // Ensure the base URL does not end with a trailing slash
            String baseUrl = userServiceBaseUrl.endsWith("/") 
                ? userServiceBaseUrl.substring(0, userServiceBaseUrl.length() - 1) 
                : userServiceBaseUrl;

            // Construct the full URL dynamically
            String url = baseUrl + "/api/users/by-clerk-id/" + clerkId;

            log.debug("Calling User Service at: {}", url);

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

    public Optional<UUID> getUserUUID(String clerkId) {
        try {
            return Optional.of(getOrCreateUserUUID(clerkId, null));
        } catch (RuntimeException e) {
            log.debug("User not found for clerkId: {}", clerkId);
            return Optional.empty();
        }
    }
}
