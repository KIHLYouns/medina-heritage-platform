package com.medina.heritage.integration.service;

import com.medina.heritage.integration.events.BuildingCreatedEvent;
import com.medina.heritage.integration.events.BuildingUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IntegrationService {

  public void processNewBuilding(BuildingCreatedEvent event) {
    log.debug("Processing creation logic for ID: {}", event.getId());
  }

  public void processUpdatedBuilding(BuildingUpdatedEvent event) {
    log.debug("Processing update logic for code: {}", event.getCode());
  }
}