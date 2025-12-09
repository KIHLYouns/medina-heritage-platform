package com.medina.heritage.integration.messaging;

import com.medina.heritage.integration.events.BuildingCreatedEvent;
import com.medina.heritage.integration.events.BuildingUpdatedEvent;
import com.medina.heritage.integration.service.IntegrationService;
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

  @Bean
  public Consumer<BuildingCreatedEvent> buildingCreatedConsumer() {
    return event -> {
      log.info("📩 REÇU : Nouveau bâtiment détecté !");
      log.info("   -> Code : {}", event.getCode());
      try {
        integrationService.processNewBuilding(event);
      } catch (Exception e) {
        log.error("Erreur traitement creation", e);
      }
    };
  }

  @Bean
  public Consumer<BuildingUpdatedEvent> buildingUpdatedConsumer() {
    return event -> {
      log.info("♻️ REÇU : Mise à jour détectée pour {}", event.getCode());
      try {
        integrationService.processUpdatedBuilding(event);
      } catch (Exception e) {
        log.error("Erreur traitement update", e);
      }
    };
  }
}