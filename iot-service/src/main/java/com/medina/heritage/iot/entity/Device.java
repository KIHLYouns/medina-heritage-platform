package com.medina.heritage.iot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "serial_number", unique = true, nullable = false, length = 100)
    private String serialNumber;
    
    @Column(nullable = false, length = 50)
    private String type; // VIBRATION, HUMIDITY, CRACK_MONITOR
    
    @Column(name = "building_id", nullable = false)
    private UUID buildingId;
    
    @Column(name = "sf_asset_id", length = 18)
    private String sfAssetId;
    
    @Column(length = 20)
    private String status = "ONLINE";
    
    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;
}

