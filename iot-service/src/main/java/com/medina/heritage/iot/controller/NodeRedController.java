package com.medina.heritage.iot.controller;

import com.medina.heritage.iot.dto.NodeRedMeasurementDto;
import com.medina.heritage.iot.entity.Measurement;
import com.medina.heritage.iot.service.MeasurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/iot/nodered")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Permet les requêtes depuis Node-RED
public class NodeRedController {
    
    private final MeasurementService measurementService;
    
    /**
     * Endpoint pour recevoir une seule mesure de Node-RED
     * POST http://localhost:8083/api/iot/nodered/measurements
     */
    @PostMapping("/measurements")
    public ResponseEntity<?> receiveMeasurement(@Valid @RequestBody NodeRedMeasurementDto dto) {
        try {
            log.info("Received measurement from Node-RED: serialNumber={}, deviceId={}, value={}, unit={}, metricType={}", 
                dto.getSerialNumber(), dto.getDeviceId(), dto.getValue(), dto.getUnit(), dto.getMetricType());
            
            Measurement saved = measurementService.saveMeasurement(dto);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            log.error("Invalid measurement data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("BAD_REQUEST", e.getMessage()));
        } catch (Exception e) {
            log.error("Error saving measurement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "An error occurred while saving the measurement"));
        }
    }
    
    /**
     * Endpoint pour recevoir plusieurs mesures en batch
     * POST http://localhost:8083/api/iot/nodered/measurements/batch
     */
    @PostMapping("/measurements/batch")
    public ResponseEntity<?> receiveBatchMeasurements(
            @Valid @RequestBody List<@Valid NodeRedMeasurementDto> dtos) {
        try {
            log.info("Received batch of {} measurements from Node-RED", dtos.size());
            
            // Log des types de capteurs dans le batch
            dtos.forEach(dto -> {
                log.info("Batch measurement: serialNumber={}, metricType={}, value={}, unit={}", 
                    dto.getSerialNumber(), dto.getMetricType(), dto.getValue(), dto.getUnit());
            });
            
            // Traiter chaque mesure individuellement pour éviter qu'une erreur arrête tout
            List<Measurement> saved = new java.util.ArrayList<>();
            List<String> errors = new java.util.ArrayList<>();
            
            for (NodeRedMeasurementDto dto : dtos) {
                try {
                    Measurement measurement = measurementService.saveMeasurement(dto);
                    saved.add(measurement);
                    log.info("Successfully saved measurement: serialNumber={}, type={}", 
                        dto.getSerialNumber(), dto.getMetricType());
                } catch (Exception e) {
                    String errorMsg = String.format("Failed to save measurement for serialNumber=%s: %s", 
                        dto.getSerialNumber(), e.getMessage());
                    log.error(errorMsg, e);
                    errors.add(errorMsg);
                }
            }
            
            log.info("Batch processing complete: {} saved, {} errors", saved.size(), errors.size());
            
            if (saved.isEmpty() && !errors.isEmpty()) {
                // Toutes les mesures ont échoué
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("BAD_REQUEST", "All measurements failed: " + String.join("; ", errors)));
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error processing batch measurements", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "An error occurred while processing batch measurements"));
        }
    }
    
    /**
     * Health check endpoint pour Node-RED
     * GET http://localhost:8083/api/iot/nodered/health
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP", "IoT Service is running and ready to receive data from Node-RED"));
    }
    
    // Classes internes pour les réponses
    private static class ErrorResponse {
        private String error;
        private String message;
        
        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }
        
        public String getError() {
            return error;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    private static class HealthResponse {
        private String status;
        private String message;
        
        public HealthResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
        
        public String getStatus() {
            return status;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

