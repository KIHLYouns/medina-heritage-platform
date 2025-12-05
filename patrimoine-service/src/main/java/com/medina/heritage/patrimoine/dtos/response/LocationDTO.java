package com.medina.heritage.patrimoine.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationDTO {
  private double latitude;
  private double longitude;
}