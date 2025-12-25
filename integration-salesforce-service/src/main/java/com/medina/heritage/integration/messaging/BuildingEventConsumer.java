package com.medina.heritage.integration.messaging;

import com.medina.heritage.events.building.BuildingCreatedEvent;
import com.medina.heritage.events.building.BuildingUpdatedEvent;
import com.medina.heritage.integration.service.SalesforceBuildingSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Consumer configuration for Building-related events.
 * Listens to building creation and update events from patrimoine-service
 * and synchronizes them to Salesforce Assets.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class BuildingEventConsumer {

    private final SalesforceBuildingSyncService salesforceBuildingSyncService;

    /**
     * Consumer for BuildingCreatedEvent from patrimoine-service.
     * Synchronizes newly created buildings to Salesforce Asset objects.
     */
    @Bean
    public Consumer<BuildingCreatedEvent> buildingCreatedConsumer() {
        return event -> {
            log.info("Received BuildingCreatedEvent: {} ({})", event.getCode(), event.getBuildingId());
            try {
                salesforceBuildingSyncService.syncNewBuilding(event);
                log.info("Successfully synchronized building {} to Salesforce", event.getBuildingId());
            } catch (Exception e) {
                log.error("Error processing BuildingCreatedEvent for building: {}. Error: {}",
                        event.getCode(), e.getMessage(), e);
                // Re-throw to let RabbitMQ handle retry
                throw new RuntimeException("Failed to synchronize building to Salesforce", e);
            }
        };
    }

    /**
     * Consumer for BuildingUpdatedEvent from patrimoine-service.
     * Synchronizes building updates to Salesforce Asset objects.
     */
    @Bean
    public Consumer<BuildingUpdatedEvent> buildingUpdatedConsumer() {
        return event -> {
            log.info("Received BuildingUpdatedEvent: {} ({})", event.getCode(), event.getBuildingId());
            try {
                salesforceBuildingSyncService.syncUpdatedBuilding(event);
                log.info("Successfully synchronized building update {} to Salesforce", event.getBuildingId());
            } catch (Exception e) {
                log.error("Error processing BuildingUpdatedEvent for building: {}. Error: {}",
                        event.getCode(), e.getMessage(), e);
                // Re-throw to let RabbitMQ handle retry
                throw new RuntimeException("Failed to synchronize building update to Salesforce", e);
            }
        };
    }
}
