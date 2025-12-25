package com.medina.heritage.iot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "measurements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime time;
    
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;
    
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal value;
    
    @Column(nullable = false, length = 20)
    private String unit;
}

