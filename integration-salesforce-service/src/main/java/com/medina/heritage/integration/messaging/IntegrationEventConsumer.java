package com.medina.heritage.integration.messaging;

import com.medina.heritage.events.user.UserCreatedEvent;
import com.medina.heritage.integration.events.BuildingCreatedEvent;
import com.medina.heritage.integration.events.BuildingUpdatedEvent;
import com.medina.heritage.integration.service.IntegrationService;
import com.medina.heritage.integration.service.SalesforceContactSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class IntegrationEventConsumer {

  private final IntegrationService integrationService;
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
        // --- MODIFICATION ICI ---
        // On suppose que event.getUserId() est maintenant un String (grâce à votre
        // modification du DTO)
        String userId = event.getUserId();

        // Si vous avez choisi UUID dans le DTO, faites : String userId =
        // event.getUserId().toString();

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

  // ... (Le reste des consumers pour BuildingCreatedEvent ne change pas)

  @Bean
  public Consumer<BuildingCreatedEvent> buildingCreatedConsumer() {
    return event -> {
      log.info("Event Received: Building Created - {}", event.getCode());
      try {
        integrationService.processNewBuilding(event);
      } catch (Exception e) {
        log.error("Error processing creation", e);
      }
    };
  }

  @Bean
  public Consumer<BuildingUpdatedEvent> buildingUpdatedConsumer() {
    return event -> {
      log.info("Event Received: Building Updated - {}", event.getCode());
      try {
        integrationService.processUpdatedBuilding(event);
      } catch (Exception e) {
        log.error("Error processing update", e);
      }
    };
  }
}