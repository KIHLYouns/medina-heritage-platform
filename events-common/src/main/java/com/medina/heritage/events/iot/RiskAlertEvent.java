package com.medina.heritage.events.iot;

import com.medina.heritage.events.base.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a risk threshold is breached by an IoT measurement.
 * Consumers: integration-salesforce-service (create Salesforce case)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RiskAlertEvent extends BaseEvent {
    
    public static final String EVENT_TYPE = "iot.risk.alert";
    
    private Long measurementId;
    private UUID deviceId;
    private String deviceSerialNumber;
    private UUID buildingId; // Référence vers le bâtiment
    private String sfAssetId; // ID Salesforce de l'Asset (bâtiment) si disponible
    private String metricType; // VIBRATION, HUMIDITY, etc.
    private BigDecimal value;
    private String unit;
    private Instant measuredAt;
    private BigDecimal thresholdMin;
    private BigDecimal thresholdMax;
    private String severityLevel; // WARNING, CRITICAL
    private String breachDirection; // ABOVE_MAX, BELOW_MIN
    private String description;
    
    public RiskAlertEvent initializeDefaults() {
        initializeEvent("iot-service");
        return this;
    }
}

