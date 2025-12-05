package com.medina.heritage.patrimoine.dtos.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class QrTagResponseDTO {
  private UUID id;
  private String qrContent;
  private String status;
  private LocalDateTime installedAt;

  private UUID buildingId;
  private String buildingName;
}