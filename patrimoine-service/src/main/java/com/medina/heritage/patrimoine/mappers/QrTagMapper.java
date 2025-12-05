package com.medina.heritage.patrimoine.mappers;

import com.medina.heritage.patrimoine.dtos.request.CreateQrTagRequestDTO;
import com.medina.heritage.patrimoine.dtos.response.QrTagResponseDTO;
import com.medina.heritage.patrimoine.entities.QrTag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QrTagMapper {

  // Entity -> Response DTO
  @Mapping(source = "building.id", target = "buildingId")
  @Mapping(source = "building.name", target = "buildingName")
  QrTagResponseDTO toResponseDTO(QrTag qrTag);

  // Request DTO -> Entity
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "installedAt", ignore = true)
  @Mapping(target = "status", constant = "ACTIVE") // Par défaut
  @Mapping(target = "building", ignore = true) // On le settera manuellement dans le Service
  QrTag toEntity(CreateQrTagRequestDTO dto);
}