package com.medina.heritage.iot.service;

import com.medina.heritage.iot.entity.Device;
import com.medina.heritage.iot.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    
    public List<Device> findAll() {
        return deviceRepository.findAll();
    }
    
    public Device findById(UUID id) {
        return deviceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + id));
    }
    
    public Device findBySerialNumber(String serialNumber) {
        return deviceRepository.findBySerialNumber(serialNumber)
            .orElseThrow(() -> new IllegalArgumentException("Device not found with serial number: " + serialNumber));
    }
    
    @Transactional
    public Device save(Device device) {
        return deviceRepository.save(device);
    }
    
    @Transactional
    public Device updateStatus(UUID deviceId, String status) {
        Device device = findById(deviceId);
        device.setStatus(status);
        device.setLastSeenAt(LocalDateTime.now());
        return deviceRepository.save(device);
    }
}

