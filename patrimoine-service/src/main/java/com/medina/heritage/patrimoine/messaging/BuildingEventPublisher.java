package com.medina.heritage.patrimoine.messaging;

import com.medina.heritage.events.building.BuildingCreatedEvent;
import com.medina.heritage.events.building.BuildingUpdatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingEventPublisher {

  private final StreamBridge streamBridge;

  // Nous définissons deux "tuyaux" de sortie différents (Bindings)
  private static final String CREATED_BINDING = "buildingCreatedSupplier-out-0";
  private static final String UPDATED_BINDING = "buildingUpdatedSupplier-out-0";

  /**
   * Publie l'événement de création
   */
  public void publishBuildingCreated(BuildingCreatedEvent event) {
    log.info("Publication de l'événement CREATED pour le bâtiment : {}", event.getCode());

    boolean sent = streamBridge.send(CREATED_BINDING, event);

    if (!sent) {
      log.error("Erreur lors de l'envoi de l'événement CREATED pour {}", event.getCode());
    }
  }

  /**
   * Publie l'événement de mise à jour
   */
  public void publishBuildingUpdated(BuildingUpdatedEvent event) {
    log.info("Publication de l'événement UPDATED pour le bâtiment : {}", event.getCode());

    boolean sent = streamBridge.send(UPDATED_BINDING, event);

    if (!sent) {
      log.error("Erreur lors de l'envoi de l'événement UPDATED pour {}", event.getCode());
    }
  }
}