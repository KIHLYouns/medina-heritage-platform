package com.medina.heritage.iot.service;

import com.medina.heritage.iot.dto.NodeRedMeasurementDto;
import com.medina.heritage.iot.entity.Device;
import com.medina.heritage.iot.entity.Measurement;
import com.medina.heritage.iot.repository.DeviceRepository;
import com.medina.heritage.iot.repository.MeasurementRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeasurementService {
    
    private final MeasurementRepository measurementRepository;
    private final DeviceRepository deviceRepository;
    private final RiskEvaluationService riskEvaluationService;
    
    @Transactional
    public Measurement saveMeasurement(NodeRedMeasurementDto dto) {
        // Vérifier ou obtenir le device_id
        UUID deviceId = dto.getDeviceId();
        
        // Si device_id n'est pas fourni, essayer de le trouver via serial_number
        if (deviceId == null && dto.getSerialNumber() != null) {
            deviceId = deviceRepository.findBySerialNumber(dto.getSerialNumber())
                .map(Device::getId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Device not found with serial number: " + dto.getSerialNumber()
                ));
        }
        
        // Vérifier que device_id est disponible
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID or Serial Number is required");
        }
        
        // Utiliser une variable finale pour la lambda
        final UUID finalDeviceId = deviceId;
        
        // Vérifier que le device existe
        Device device = deviceRepository.findById(finalDeviceId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Device not found with ID: " + finalDeviceId
            ));
        
        // Validation explicite : accepter uniquement HUMIDITY et VIBRATION
        String deviceType = device.getType();
        if (deviceType != null) {
            deviceType = deviceType.trim().toUpperCase();
        }
        
        // Log pour déboguer
        log.debug("Processing measurement for device type: '{}', serialNumber: '{}'", 
            deviceType, device.getSerialNumber());
        
        if (deviceType == null || (!deviceType.equals("HUMIDITY") && !deviceType.equals("VIBRATION"))) {
            log.error("Unsupported device type: '{}' for device: {}", deviceType, device.getSerialNumber());
            throw new IllegalArgumentException(
                "Unsupported device type: " + deviceType + ". Only HUMIDITY and VIBRATION are supported."
            );
        }
        
        // Mettre à jour last_seen_at du device
        device.setLastSeenAt(LocalDateTime.now());
        device.setStatus("ONLINE");
        deviceRepository.save(device);
        
        // Créer et sauvegarder la mesure
        Measurement measurement = new Measurement();
        measurement.setDeviceId(finalDeviceId);
        measurement.setValue(dto.getValue());
        measurement.setUnit(dto.getUnit());
        measurement.setTime(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
        
        Measurement saved = measurementRepository.save(measurement);
        log.info("Measurement saved: type={}, serialNumber={}, deviceId={}, value={}, unit={}, time={}", 
            deviceType, device.getSerialNumber(), finalDeviceId, dto.getValue(), dto.getUnit(), measurement.getTime());

        // Déclencher l'évaluation du risque et l'envoi éventuel d'un événement RabbitMQ
        String metricType = normalizeMetricType(dto.getMetricType(), deviceType);
        riskEvaluationService.evaluateAndPublish(metricType, saved, device);

        return saved;
    }
    
    public Measurement findById(Long id) {
        return measurementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Measurement not found with ID: " + id));
    }

    private String normalizeMetricType(String rawMetricType, @NonNull String fallbackDeviceType) {
        if (rawMetricType != null && !rawMetricType.isBlank()) {
            String normalized = rawMetricType.trim().toUpperCase();
            // Mapper les types spécifiques vers les types de règles de risque
            // Node-RED peut envoyer "VIBRATION_LEVEL" mais les règles utilisent "VIBRATION"
            if (normalized.startsWith("VIBRATION")) {
                return "VIBRATION";
            }
            // HUMIDITY est généralement envoyé tel quel
            return normalized;
        }
        return fallbackDeviceType != null ? fallbackDeviceType.trim().toUpperCase() : null;
    }
}

