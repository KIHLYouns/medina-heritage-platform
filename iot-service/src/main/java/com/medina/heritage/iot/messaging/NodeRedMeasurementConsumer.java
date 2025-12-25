package com.medina.heritage.iot.messaging;

import com.medina.heritage.iot.dto.NodeRedMeasurementDto;
import com.medina.heritage.iot.entity.Measurement;
import com.medina.heritage.iot.service.MeasurementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Consumer Spring Cloud Stream pour recevoir les données de capteurs depuis Node-RED via RabbitMQ.
 * Spring Cloud Stream désérialisera automatiquement le JSON en NodeRedMeasurementDto.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class NodeRedMeasurementConsumer {

    private final MeasurementService measurementService;

    @Bean
    public Consumer<NodeRedMeasurementDto> receiveMeasurement() {
        return dto -> {
            try {
                log.info("Received measurement from Node-RED via RabbitMQ: serialNumber={}, deviceId={}, value={}, unit={}, metricType={}", 
                    dto.getSerialNumber(), dto.getDeviceId(), dto.getValue(), dto.getUnit(), dto.getMetricType());
                
                // Sauvegarder la mesure (cela déclenchera aussi l'évaluation des risques)
                Measurement saved = measurementService.saveMeasurement(dto);
                
                log.info("Successfully processed measurement from Node-RED: measurementId={}, deviceId={}, value={}", 
                    saved.getId(), dto.getDeviceId(), dto.getValue());
            } catch (IllegalArgumentException e) {
                log.error("Invalid measurement data from Node-RED: {}", e.getMessage());
                // Re-throw pour que Spring Cloud Stream puisse gérer l'erreur (retry/DLQ)
                throw e;
            } catch (Exception e) {
                log.error("Error processing measurement from Node-RED: {}", e.getMessage(), e);
                // Re-throw pour que Spring Cloud Stream puisse gérer l'erreur (retry/DLQ)
                throw new RuntimeException("Failed to process measurement from Node-RED", e);
            }
        };
    }
}

