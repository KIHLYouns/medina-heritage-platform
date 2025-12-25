package com.medina.heritage.iot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NodeRedMeasurementDto {
    @JsonProperty("device_id")
    private UUID deviceId;
    
    @JsonProperty("serial_number")
    private String serialNumber; // Optionnel, pour identifier le device si device_id n'est pas fourni
    
    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.0", message = "Value must be positive")
    private BigDecimal value;
    
    @NotNull(message = "Unit is required")
    private String unit;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp; // Si non fourni, utiliser maintenant
    
    @JsonProperty("metric_type")
    private String metricType; // VIBRATION_LEVEL, HUMIDITY, etc.
}

