package com.medina.heritage.patrimoine.services.impl;

import com.medina.heritage.patrimoine.dtos.request.CreateQrTagRequestDTO;
import com.medina.heritage.patrimoine.dtos.response.BuildingResponseDTO;
import com.medina.heritage.patrimoine.dtos.response.QrTagResponseDTO;
import com.medina.heritage.patrimoine.entities.Building;
import com.medina.heritage.patrimoine.entities.QrTag;
import com.medina.heritage.patrimoine.exceptions.ResourceNotFoundException;
import com.medina.heritage.patrimoine.mappers.BuildingMapper;
import com.medina.heritage.patrimoine.mappers.QrTagMapper;
import com.medina.heritage.patrimoine.repositories.BuildingRepository;
import com.medina.heritage.patrimoine.repositories.QrTagRepository;
import com.medina.heritage.patrimoine.services.QrTagService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QrTagServiceImpl implements QrTagService {

  private final QrTagRepository qrTagRepository;
  private final BuildingRepository buildingRepository;
  private final QrTagMapper qrTagMapper;
  private final BuildingMapper buildingMapper; // Pour renvoyer le bâtiment après scan

  @Override
  public QrTagResponseDTO createQrTag(CreateQrTagRequestDTO request) {
    Building building = buildingRepository.findById(request.getBuildingId())
        .orElseThrow(() -> new EntityNotFoundException("Bâtiment introuvable avec l'ID : " + request.getBuildingId()));

    QrTag qrTag = qrTagMapper.toEntity(request);

    qrTag.setBuilding(building);

    QrTag savedTag = qrTagRepository.save(qrTag);

    return qrTagMapper.toResponseDTO(savedTag);
  }

  @Override
  @Transactional(readOnly = true)
  public BuildingResponseDTO scanQrCode(String qrContent) {
    QrTag tag = qrTagRepository.findByQrContent(qrContent)
        .orElseThrow(() -> new EntityNotFoundException("Aucun monument ne correspond à ce QR Code : " + qrContent));

    return buildingMapper.toResponseDTO(tag.getBuilding());
  }

  @Override
  public QrTagResponseDTO updateQrStatus(String qrContent, String newStatus) {
    QrTag tag = qrTagRepository.findByQrContent(qrContent)
        .orElseThrow(() -> new ResourceNotFoundException("QR Code introuvable : " + qrContent));

    tag.setStatus(newStatus);
    return qrTagMapper.toResponseDTO(qrTagRepository.save(tag));
  }

  @Override
  public void deleteQrTag(String qrContent) {
    QrTag tag = qrTagRepository.findByQrContent(qrContent)
        .orElseThrow(() -> new ResourceNotFoundException("Impossible de supprimer : QR Code introuvable"));
    qrTagRepository.delete(tag);
  }
}