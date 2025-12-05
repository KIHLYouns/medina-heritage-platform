package com.medina.heritage.patrimoine.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBuildingRequestDTO {
  @NotBlank(message = "Le code est obligatoire")
  private String code;

  @NotBlank(message = "Le nom est obligatoire")
  private String name;

  private String address;

  @NotNull(message = "La latitude est requise")
  private Double latitude;

  @NotNull(message = "La longitude est requise")
  private Double longitude;
}