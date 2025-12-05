package com.medina.heritage.patrimoine.controllers;

import com.medina.heritage.patrimoine.dtos.request.CreateQrTagRequestDTO;
import com.medina.heritage.patrimoine.dtos.response.BuildingResponseDTO;
import com.medina.heritage.patrimoine.dtos.response.QrTagResponseDTO;
import com.medina.heritage.patrimoine.services.QrTagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr-tags")
@RequiredArgsConstructor
public class QrTagController {

  private final QrTagService qrTagService;

  @PostMapping
  public ResponseEntity<QrTagResponseDTO> create(@Valid @RequestBody CreateQrTagRequestDTO request) {
    return new ResponseEntity<>(qrTagService.createQrTag(request), HttpStatus.CREATED);
  }

  @GetMapping("/scan/{qrContent}")
  public ResponseEntity<BuildingResponseDTO> scan(@PathVariable String qrContent) {
    return ResponseEntity.ok(qrTagService.scanQrCode(qrContent));
  }

  @PatchMapping("/{qrContent}/status")
  public ResponseEntity<QrTagResponseDTO> updateStatus(@PathVariable String qrContent,
      @RequestBody @Valid com.medina.heritage.patrimoine.dtos.request.UpdateQrStatusDTO dto) {
    return ResponseEntity.ok(qrTagService.updateQrStatus(qrContent, dto.getStatus()));
  }

  @DeleteMapping("/{qrContent}")
  public ResponseEntity<Void> delete(@PathVariable String qrContent) {
    qrTagService.deleteQrTag(qrContent);
    return ResponseEntity.noContent().build();
  }
}