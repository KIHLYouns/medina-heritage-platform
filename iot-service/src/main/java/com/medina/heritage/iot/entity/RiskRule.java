package com.medina.heritage.iot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "risk_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "metric_type", nullable = false, length = 50)
    private String metricType; // ex: 'VIBRATION_LEVEL', 'HUMIDITY'
    
    @Column(name = "threshold_min", precision = 10, scale = 2)
    private BigDecimal thresholdMin;
    
    @Column(name = "threshold_max", precision = 10, scale = 2)
    private BigDecimal thresholdMax;
    
    @Column(name = "severity_level", nullable = false, length = 20)
    private String severityLevel; // 'WARNING', 'CRITICAL'
    
    @Column(length = 255)
    private String description;
}

