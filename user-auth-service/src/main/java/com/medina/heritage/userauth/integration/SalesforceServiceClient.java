package com.medina.heritage.userauth.integration;

import com.medina.heritage.userauth.event.UserCreatedEvent;
import com.medina.heritage.userauth.event.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Client pour communiquer avec le service Salesforce.
 * Envoie les événements utilisateurs pour synchronisation avec Salesforce CRM.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesforceServiceClient {

    private final RestTemplate restTemplate;

    @Value("${salesforce.service.url:http://localhost:8086}")
    private String salesforceServiceUrl;

    /**
     * Notifie Salesforce de la création d'un nouvel utilisateur.
     * Crée un Contact dans Salesforce.
     */
    @Async
    public void notifyUserCreated(UserCreatedEvent event) {
        try {
            String url = salesforceServiceUrl + "/api/salesforce/contacts";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<UserCreatedEvent> request = new HttpEntity<>(event, headers);
            
            restTemplate.postForEntity(url, request, Void.class);
            log.info("Successfully notified Salesforce of user creation: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to notify Salesforce of user creation: {}", event.getEmail(), e);
            // Ne pas propager l'exception - la création utilisateur doit réussir même si SF échoue
        }
    }

    /**
     * Notifie Salesforce de la mise à jour d'un utilisateur.
     */
    @Async
    public void notifyUserUpdated(UserUpdatedEvent event) {
        try {
            String url = salesforceServiceUrl + "/api/salesforce/contacts/" + event.getUserId();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<UserUpdatedEvent> request = new HttpEntity<>(event, headers);
            
            restTemplate.put(url, request);
            log.info("Successfully notified Salesforce of user update: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to notify Salesforce of user update: {}", event.getEmail(), e);
        }
    }
}
