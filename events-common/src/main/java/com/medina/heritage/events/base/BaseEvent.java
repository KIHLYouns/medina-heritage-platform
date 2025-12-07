package com.medina.heritage.events.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events.
 * Provides common fields for event tracking and correlation.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    
    /**
     * Unique identifier for the event
     */
    private String eventId;
    
    /**
     * Timestamp when the event occurred
     */
    private Instant timestamp;
    
    /**
     * Source service that generated the event
     */
    private String source;
    
    /**
     * Correlation ID for distributed tracing
     */
    private String correlationId;
    
    /**
     * Initialize event with default values
     */
    protected void initializeEvent(String source) {
        if (this.eventId == null) {
            this.eventId = UUID.randomUUID().toString();
        }
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
        if (this.source == null) {
            this.source = source;
        }
        if (this.correlationId == null) {
            this.correlationId = UUID.randomUUID().toString();
        }
    }
}
