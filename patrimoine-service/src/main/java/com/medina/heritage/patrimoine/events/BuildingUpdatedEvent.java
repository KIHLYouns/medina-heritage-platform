package com.medina.heritage.patrimoine.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement local : Bâtiment Mis à jour.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingUpdatedEvent {

  private UUID id;
  private String code;
  private String name;
  private String address;
  private Double latitude;
  private Double longitude;
  private LocalDateTime updatedAt; 
}