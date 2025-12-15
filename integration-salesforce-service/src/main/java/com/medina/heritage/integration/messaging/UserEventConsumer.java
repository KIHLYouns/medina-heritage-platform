package com.medina.heritage.integration.messaging;

import com.medina.heritage.events.user.UserCreatedEvent;
import com.medina.heritage.integration.service.SalesforceContactSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;

/**
 * Consumer configuration for User-related events.
 * NOTE: Building events are now handled in BuildingEventConsumer.java
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

  private final SalesforceContactSyncService salesforceContactSyncService;

  /**
   * Consumer for UserCreatedEvent from user-auth-service.
   * Synchronizes newly created users to Salesforce Contact objects.
   */
  @Bean
  public Consumer<UserCreatedEvent> userCreatedConsumer() {
    return event -> {
      log.info("Received UserCreatedEvent for user: {} ({})", event.getUserId(), event.getEmail());
      try {
        String userId = event.getUserId();
        String email = event.getEmail();
        String firstName = event.getFirstName();
        String lastName = event.getLastName();
        String phoneNumber = event.getPhoneNumber();

        // Upsert contact in Salesforce
        salesforceContactSyncService.upsertContact(
            userId,
            email,
            firstName,
            lastName,
            phoneNumber);

        log.info("Successfully synchronized user {} to Salesforce", event.getUserId());

      } catch (Exception e) {
        log.error("Error processing UserCreatedEvent for user: {}. Error: {}",
            event.getUserId(), e.getMessage(), e);
        // Rethrow to let RabbitMQ handle retry
        throw new RuntimeException("Failed to synchronize user to Salesforce", e);
      }
    };
  }
}