package com.medina.heritage.patrimoine.dtos.response;

import lombok.Data;
import java.util.UUID;

@Data
public class BuildingResponseDTO {
  private UUID id;
  private String code;
  private String name;
  private String address;
  private String sfAssetId;
  private LocationDTO location; 
  private String imageUrl;
}