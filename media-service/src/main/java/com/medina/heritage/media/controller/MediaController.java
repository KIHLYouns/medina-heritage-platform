package com.medina.heritage.media.controller;

import com.medina.heritage.media.dto.request.UploadMediaRequest;
import com.medina.heritage.media.dto.response.ApiResponse;
import com.medina.heritage.media.dto.response.MediaResponse;
import com.medina.heritage.media.enums.EntityType;
import com.medina.heritage.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour les opérations sur les médias.
 */
@Slf4j
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    /**
     * Upload d'un fichier média.
     * POST /api/media/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaResponse>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") UUID userId,
            @RequestParam("entityType") EntityType entityType,
            @RequestParam(value = "entityId", required = false) UUID entityId) {

        UploadMediaRequest request = new UploadMediaRequest();
        request.setUserId(userId);
        request.setEntityType(entityType);
        request.setEntityId(entityId);

        MediaResponse response = mediaService.uploadFile(file, request);
        
        log.info("File uploaded: id={}, filename={}", response.getId(), response.getOriginalFilename());
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Media uploaded successfully", response));
    }

    /**
     * Récupère un média par son ID.
     * GET /api/media/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaResponse>> getMedia(@PathVariable UUID id) {
        MediaResponse response = mediaService.getMediaById(id);
        return ResponseEntity.ok(ApiResponse.success("Media retrieved", response));
    }

    /**
     * Liste les médias d'un utilisateur.
     * GET /api/media/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<MediaResponse>>> getMediaByUser(@PathVariable UUID userId) {
        List<MediaResponse> responses = mediaService.getMediaByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User media retrieved", responses));
    }

    /**
     * Liste les médias associés à une entité.
     * GET /api/media/entity/{entityType}/{entityId}
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<MediaResponse>>> getMediaByEntity(
            @PathVariable EntityType entityType,
            @PathVariable UUID entityId) {
        List<MediaResponse> responses = mediaService.getMediaByEntity(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success("Entity media retrieved", responses));
    }

    /**
     * Suppression logique d'un média (soft delete).
     * DELETE /api/media/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(
            @PathVariable UUID id,
            @RequestParam("userId") UUID userId) {
        mediaService.deleteMedia(id, userId);
        log.info("Media soft-deleted: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Media deleted successfully", null));
    }

    /**
     * Suppression physique d'un média (admin seulement).
     * DELETE /api/media/{id}/permanent
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteMedia(@PathVariable UUID id) {
        mediaService.permanentlyDeleteMedia(id);
        log.info("Media permanently deleted: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Media permanently deleted", null));
    }

    /**
     * Compte le nombre de médias d'un utilisateur.
     * GET /api/media/user/{userId}/count
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<ApiResponse<Long>> countUserMedia(@PathVariable UUID userId) {
        long count = mediaService.countUserMedia(userId);
        return ResponseEntity.ok(ApiResponse.success("Media count retrieved", count));
    }
}
