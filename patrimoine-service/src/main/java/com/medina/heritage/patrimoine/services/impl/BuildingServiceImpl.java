package com.medina.heritage.patrimoine.services.impl;

import com.medina.heritage.patrimoine.dtos.request.CreateBuildingRequestDTO;
import com.medina.heritage.patrimoine.dtos.response.BuildingResponseDTO;
import com.medina.heritage.patrimoine.entities.Building;
import com.medina.heritage.patrimoine.exceptions.ResourceNotFoundException;
import com.medina.heritage.patrimoine.mappers.BuildingMapper;
import com.medina.heritage.patrimoine.repositories.BuildingRepository;
import com.medina.heritage.patrimoine.services.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BuildingServiceImpl implements BuildingService {

  private final BuildingRepository repository;
  private final BuildingMapper mapper;

  @Override
  @Transactional(readOnly = true)
  public List<BuildingResponseDTO> getAllBuildings() {
    return repository.findAll().stream()
        .map(mapper::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Override
  public BuildingResponseDTO createBuilding(CreateBuildingRequestDTO request) {
    Building building = mapper.toEntity(request);
    Building saved = repository.save(building);

    // ICI: Plus tard, on ajoutera l'envoi d'event RabbitMQ "BuildingCreatedEvent"
    // eventPublisher.publish(new BuildingCreatedEvent(saved.getId(),
    // saved.getCode()));

    return mapper.toResponseDTO(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public BuildingResponseDTO getBuildingById(UUID id) {
    return repository.findById(id)
        .map(mapper::toResponseDTO)
        .orElseThrow(() -> new ResourceNotFoundException("Bâtiment introuvable avec l'ID : " + id));
  }

  @Override
  public BuildingResponseDTO updateBuilding(UUID id, CreateBuildingRequestDTO request) {
    Building existingBuilding = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Impossible de mettre à jour : Bâtiment introuvable"));

    mapper.updateEntityFromDto(request, existingBuilding);

    Building updated = repository.save(existingBuilding);
    return mapper.toResponseDTO(updated);
  }

  @Override
  public void deleteBuilding(UUID id) {
    if (!repository.existsById(id)) {
      throw new ResourceNotFoundException("Impossible de supprimer : Bâtiment introuvable");
    }
    repository.deleteById(id);
  }
}