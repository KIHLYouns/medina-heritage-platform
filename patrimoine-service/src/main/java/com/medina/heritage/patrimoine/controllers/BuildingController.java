package com.medina.heritage.patrimoine.controllers;

import com.medina.heritage.patrimoine.dtos.request.CreateBuildingRequestDTO;
import com.medina.heritage.patrimoine.dtos.response.BuildingResponseDTO;
import com.medina.heritage.patrimoine.services.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

  private final BuildingService service;

  @GetMapping
  public ResponseEntity<List<BuildingResponseDTO>> getAll() {
    return ResponseEntity.ok(service.getAllBuildings());
  }

  @PostMapping
  public ResponseEntity<BuildingResponseDTO> create(@Valid @RequestBody CreateBuildingRequestDTO request) {
    return new ResponseEntity<>(service.createBuilding(request), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BuildingResponseDTO> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(service.getBuildingById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BuildingResponseDTO> update(@PathVariable UUID id,
      @Valid @RequestBody CreateBuildingRequestDTO request) {
    return ResponseEntity.ok(service.updateBuilding(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.deleteBuilding(id);
    return ResponseEntity.noContent().build(); // Renvoie 204 No Content
  }
}