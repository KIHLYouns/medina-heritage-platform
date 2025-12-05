package com.medina.heritage.patrimoine.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateQrStatusDTO {
  @NotBlank
  private String status; 
}