package com.medina.heritage.iot.service;

import com.medina.heritage.events.iot.RiskAlertEvent;
import com.medina.heritage.iot.entity.Device;
import com.medina.heritage.iot.entity.Measurement;
import com.medina.heritage.iot.entity.RiskRule;
import com.medina.heritage.iot.repository.RiskRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskEvaluationService {

    private final RiskRuleRepository riskRuleRepository;
    private final StreamBridge streamBridge;

    private static final String RISK_ALERT_BINDING = "riskAlertSupplier-out-0";

    public void evaluateAndPublish(String rawMetricType, Measurement measurement, Device device) {
        String metricType = normalize(rawMetricType);
        if (metricType == null) {
            log.warn("Metric type missing; cannot evaluate risk for device {}", device.getSerialNumber());
            return;
        }

        List<RiskRule> rules = riskRuleRepository.findByMetricType(metricType);
        if (rules.isEmpty()) {
            log.info("No risk rules configured for metricType={}, device={}, value={} - skipping risk evaluation", 
                metricType, device.getSerialNumber(), measurement.getValue());
            return;
        }
        
        log.debug("Found {} risk rule(s) for metricType={}, evaluating thresholds...", rules.size(), metricType);

        rules.forEach(rule -> checkRuleAndPublish(rule, measurement, device, metricType));
    }

    private void checkRuleAndPublish(RiskRule rule, Measurement measurement, Device device, String metricType) {
        BigDecimal value = measurement.getValue();
        boolean belowMin = rule.getThresholdMin() != null && value.compareTo(rule.getThresholdMin()) < 0;
        boolean aboveMax = rule.getThresholdMax() != null && value.compareTo(rule.getThresholdMax()) > 0;

        if (!belowMin && !aboveMax) {
            log.debug("Value {} is within thresholds [min={}, max={}] for rule id={}, metricType={}", 
                value, rule.getThresholdMin(), rule.getThresholdMax(), rule.getId(), metricType);
            return;
        }

        String direction = aboveMax ? "ABOVE_MAX" : "BELOW_MIN";

        // Convertir LocalDateTime en Instant pour l'événement
        Instant measuredAtInstant = measurement.getTime()
            .atZone(ZoneId.systemDefault())
            .toInstant();

        RiskAlertEvent event = RiskAlertEvent.builder()
            .measurementId(measurement.getId())
            .deviceId(device.getId())
            .deviceSerialNumber(device.getSerialNumber())
            .buildingId(device.getBuildingId())
            .sfAssetId(device.getSfAssetId()) // ID Salesforce de l'Asset si disponible
            .metricType(metricType)
            .value(value)
            .unit(measurement.getUnit())
            .measuredAt(measuredAtInstant)
            .thresholdMin(rule.getThresholdMin())
            .thresholdMax(rule.getThresholdMax())
            .severityLevel(rule.getSeverityLevel())
            .breachDirection(direction)
            .description(rule.getDescription())
            .build();

        // Initialiser les valeurs par défaut de l'événement
        event.initializeDefaults();

        try {
            boolean sent = streamBridge.send(RISK_ALERT_BINDING, event);
            if (sent) {
                log.warn("Risk rule triggered for device {} metricType={} value={} direction={} severity={} -> published via Spring Cloud Stream",
                    device.getSerialNumber(), metricType, value, direction, rule.getSeverityLevel());
            } else {
                log.error("Failed to publish risk alert event via Spring Cloud Stream for device {}", device.getSerialNumber());
            }
        } catch (Exception e) {
            log.error("Failed to publish risk alert event via Spring Cloud Stream: {}", e.getMessage(), e);
            // Continue processing despite messaging failure - measurement is already saved
        }
    }

    private String normalize(String metricType) {
        if (metricType == null || metricType.isBlank()) {
            return null;
        }
        String normalized = metricType.trim().toUpperCase(Locale.ROOT);
        // Mapper les types spécifiques vers les types de règles de risque
        // Node-RED peut envoyer "VIBRATION_LEVEL" mais les règles utilisent "VIBRATION"
        if (normalized.startsWith("VIBRATION")) {
            return "VIBRATION";
        }
        // HUMIDITY est généralement envoyé tel quel
        return normalized;
    }
}

