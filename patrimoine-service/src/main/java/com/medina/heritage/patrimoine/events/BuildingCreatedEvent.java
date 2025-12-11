package com.medina.heritage.patrimoine.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement local : Bâtiment Créé.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingCreatedEvent {

  private UUID id;
  private String code;
  private String name;
  private String address;
  private Double latitude;
  private Double longitude;
  private String imageUrl;
  private LocalDateTime createdAt;
}