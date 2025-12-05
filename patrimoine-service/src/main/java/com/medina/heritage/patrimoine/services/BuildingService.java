package com.medina.heritage.patrimoine.services;

import com.medina.heritage.patrimoine.dtos.request.CreateBuildingRequestDTO;
import com.medina.heritage.patrimoine.dtos.response.BuildingResponseDTO;
import java.util.List;
import java.util.UUID;
public interface BuildingService {
  List<BuildingResponseDTO> getAllBuildings();

  BuildingResponseDTO createBuilding(CreateBuildingRequestDTO request);

  BuildingResponseDTO getBuildingById(UUID id);

  BuildingResponseDTO updateBuilding(UUID id, CreateBuildingRequestDTO request);

  void deleteBuilding(UUID id);
}