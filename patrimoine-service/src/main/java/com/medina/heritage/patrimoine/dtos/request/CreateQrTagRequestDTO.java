package com.medina.heritage.patrimoine.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateQrTagRequestDTO {

  @NotBlank(message = "Le contenu du QR est obligatoire")
  private String qrContent;

  @NotNull(message = "L'ID du bâtiment est obligatoire")
  private UUID buildingId; 
}