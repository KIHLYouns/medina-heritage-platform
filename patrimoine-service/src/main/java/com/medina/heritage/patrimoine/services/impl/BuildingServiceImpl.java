package com.medina.heritage.patrimoine.services.impl;

import com.medina.heritage.patrimoine.dtos.request.CreateBuildingRequestDTO;
import com.medina.heritage.patrimoine.dtos.request.CreateQrTagRequestDTO;
import com.medina.heritage.patrimoine.dtos.response.BuildingResponseDTO;
import com.medina.heritage.patrimoine.entities.Building;
import com.medina.heritage.events.building.BuildingCreatedEvent;
import com.medina.heritage.events.building.BuildingUpdatedEvent;
import com.medina.heritage.patrimoine.exceptions.ResourceNotFoundException;
import com.medina.heritage.patrimoine.mappers.BuildingMapper;
import com.medina.heritage.patrimoine.messaging.BuildingEventPublisher;
import com.medina.heritage.patrimoine.repositories.BuildingRepository;
import com.medina.heritage.patrimoine.services.BuildingService;
import com.medina.heritage.patrimoine.services.QrTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BuildingServiceImpl implements BuildingService {

  private final BuildingRepository repository;
  private final BuildingMapper mapper;
  private final BuildingEventPublisher eventPublisher;
  private final QrTagService qrTagService;

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

    // Create QR Tag for the building
    try {
      CreateQrTagRequestDTO qrTagRequest = new CreateQrTagRequestDTO();
      qrTagRequest.setQrContent(saved.getCode());
      qrTagRequest.setBuildingId(saved.getId());
      
      qrTagService.createQrTag(qrTagRequest);
      log.info("QR Tag created successfully for building: {} with content: {}", saved.getCode(), saved.getCode());
    } catch (Exception e) {
      log.error("Failed to create QR Tag for building {}: {}", saved.getId(), e.getMessage(), e);
      // Continue even if QR tag creation fails - don't block building creation
    }

    // Publish building created event
    try {
      BuildingCreatedEvent event = BuildingCreatedEvent.builder()
          .buildingId(saved.getId().toString())
          .code(saved.getCode())
          .name(saved.getName())
          .address(saved.getAddress())
          .description(saved.getDescription())
          .latitude(saved.getGeom() != null ? saved.getGeom().getY() : null)
          .longitude(saved.getGeom() != null ? saved.getGeom().getX() : null)
          .imageUrl(saved.getImageUrl())
          .build();
      
      // Initialize event metadata
      event.initializeDefaults();

      eventPublisher.publishBuildingCreated(event);

    } catch (Exception e) {
      log.error("Erreur publication event CREATED pour id {}: {}", saved.getId(), e.getMessage());
    }

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

    try {
      BuildingUpdatedEvent event = BuildingUpdatedEvent.builder()
          .buildingId(updated.getId().toString())
          .code(updated.getCode())
          .name(updated.getName())
          .address(updated.getAddress())
          .description(updated.getDescription())
          .latitude(updated.getGeom() != null ? updated.getGeom().getY() : null)
          .longitude(updated.getGeom() != null ? updated.getGeom().getX() : null)
          .imageUrl(updated.getImageUrl())
          .build();
      
      // Initialize event metadata
      event.initializeDefaults();

      eventPublisher.publishBuildingUpdated(event);

    } catch (Exception e) {
      log.error("Erreur publication event UPDATED pour id {}: {}", updated.getId(), e.getMessage());
    }

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