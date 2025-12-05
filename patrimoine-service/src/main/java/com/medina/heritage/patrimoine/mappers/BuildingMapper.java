package com.medina.heritage.patrimoine.mappers;

import com.medina.heritage.patrimoine.dtos.request.CreateBuildingRequestDTO;
import com.medina.heritage.patrimoine.dtos.response.BuildingResponseDTO;
import com.medina.heritage.patrimoine.dtos.response.LocationDTO;
import com.medina.heritage.patrimoine.entities.Building;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface BuildingMapper {

  @Mapping(target = "location", source = "geom", qualifiedByName = "pointToLocation")
  BuildingResponseDTO toResponseDTO(Building building);

  @Mapping(target = "geom", source = ".", qualifiedByName = "locationToPoint")                                                             // source"
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "sfAssetId", ignore = true)
  Building toEntity(CreateBuildingRequestDTO dto);

  @Mapping(target = "geom", source = ".", qualifiedByName = "locationToPoint")
  @Mapping(target = "id", ignore = true) 
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "sfAssetId", ignore = true)
  void updateEntityFromDto(CreateBuildingRequestDTO dto, @MappingTarget Building entity);

  // --- Méthodes de conversion technique ---

  @Named("pointToLocation")
  default LocationDTO pointToLocation(Point point) {
    if (point == null)
      return null;
    return new LocationDTO(point.getY(), point.getX());
  }

  @Named("locationToPoint")
  default Point locationToPoint(CreateBuildingRequestDTO dto) {
    if (dto.getLatitude() == null || dto.getLongitude() == null)
      return null;
    GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
    return gf.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
  }

}