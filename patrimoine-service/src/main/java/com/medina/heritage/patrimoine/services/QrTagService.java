package com.medina.heritage.patrimoine.services;

import com.medina.heritage.patrimoine.dtos.request.CreateQrTagRequestDTO;
import com.medina.heritage.patrimoine.dtos.response.BuildingResponseDTO;
import com.medina.heritage.patrimoine.dtos.response.QrTagResponseDTO;

public interface QrTagService {
  QrTagResponseDTO createQrTag(CreateQrTagRequestDTO request);

  BuildingResponseDTO scanQrCode(String qrContent);

  QrTagResponseDTO updateQrStatus(String qrContent, String newStatus);

  void deleteQrTag(String qrContent);
}