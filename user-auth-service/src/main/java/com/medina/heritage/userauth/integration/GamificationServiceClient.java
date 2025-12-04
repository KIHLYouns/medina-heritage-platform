package com.medina.heritage.userauth.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client pour communiquer avec le service de Gamification.
 * Crée un wallet pour les nouveaux utilisateurs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GamificationServiceClient {

    private final RestTemplate restTemplate;

    @Value("${gamification.service.url:http://localhost:8082}")
    private String gamificationServiceUrl;

    /**
     * Crée un wallet pour un nouvel utilisateur.
     * Appelé lors de l'inscription.
     */
    @Async
    public void createUserWallet(UUID userId, String email) {
        try {
            String url = gamificationServiceUrl + "/api/wallets";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId.toString());
            body.put("email", email);
            body.put("initialBalance", 0);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            
            restTemplate.postForEntity(url, request, Void.class);
            log.info("Successfully created wallet for user: {}", email);
        } catch (Exception e) {
            log.error("Failed to create wallet for user: {}", email, e);
            // Ne pas propager l'exception - l'inscription doit réussir même si gamification échoue
        }
    }

    /**
     * Ajoute des points de bienvenue pour un nouvel utilisateur.
     */
    @Async
    public void addWelcomePoints(UUID userId, int points) {
        try {
            String url = gamificationServiceUrl + "/api/wallets/" + userId + "/points";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> body = new HashMap<>();
            body.put("points", points);
            body.put("reason", "WELCOME_BONUS");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            
            restTemplate.postForEntity(url, request, Void.class);
            log.info("Successfully added welcome points for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to add welcome points for user: {}", userId, e);
        }
    }
}
