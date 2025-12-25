package com.medina.heritage.iot.messaging;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RiskEventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long measurementId;
    private UUID deviceId;
    private String deviceSerialNumber;
    private String metricType;
    private BigDecimal value;
    private String unit;
    private LocalDateTime measuredAt;
    private BigDecimal thresholdMin;
    private BigDecimal thresholdMax;
    private String severityLevel;
    private String breachDirection; // ABOVE_MAX or BELOW_MIN
    private String description;
}
