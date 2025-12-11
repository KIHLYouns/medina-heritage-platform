package com.medina.heritage.integration.events;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
public class BuildingCreatedEvent {
    private UUID id;
    private String code;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
}