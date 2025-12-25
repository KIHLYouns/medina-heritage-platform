package com.medina.heritage.iot.controller;

import com.medina.heritage.iot.entity.Device;
import com.medina.heritage.iot.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/iot/devices")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DeviceController {
    
    private final DeviceService deviceService;
    
    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        List<Device> devices = deviceService.findAll();
        return ResponseEntity.ok(devices);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable UUID id) {
        try {
            Device device = deviceService.findById(id);
            return ResponseEntity.ok(device);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/serial/{serialNumber}")
    public ResponseEntity<Device> getDeviceBySerialNumber(@PathVariable String serialNumber) {
        try {
            Device device = deviceService.findBySerialNumber(serialNumber);
            return ResponseEntity.ok(device);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Device> createDevice(@RequestBody Device device) {
        Device saved = deviceService.save(device);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Device> updateDeviceStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        try {
            Device device = deviceService.updateStatus(id, status);
            return ResponseEntity.ok(device);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

