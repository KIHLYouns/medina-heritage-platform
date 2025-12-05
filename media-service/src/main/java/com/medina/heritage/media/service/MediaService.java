package com.medina.heritage.media.service;

import com.medina.heritage.media.dto.request.UploadMediaRequest;
import com.medina.heritage.media.dto.response.MediaResponse;
import com.medina.heritage.media.entity.MediaFile;
import com.medina.heritage.media.enums.EntityType;
import com.medina.heritage.media.enums.MediaStatus;
import com.medina.heritage.media.enums.MediaType;
import com.medina.heritage.media.exception.MediaNotFoundException;
import com.medina.heritage.media.mapper.MediaMapper;
import com.medina.heritage.media.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service principal de gestion des médias.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaFileRepository mediaFileRepository;
    private final S3StorageService s3StorageService;
    private final FileValidationService fileValidationService;
    private final MediaMapper mediaMapper;

    /**
     * Upload un fichier et l'enregistre en base de données.
     * 
     * @param file    le fichier à uploader
     * @param request les métadonnées de la requête
     * @return les informations du média créé avec l'URL publique
     */
    @Transactional
    public MediaResponse uploadFile(MultipartFile file, UploadMediaRequest request) {
        // 1. Valider le fichier
        fileValidationService.validateFile(file);

        String mimeType = file.getContentType();
        MediaType mediaType = fileValidationService.determineMediaType(mimeType);

        // 2. Générer la clé S3
        String fileKey = fileValidationService.generateFileKey(
                file.getOriginalFilename(), request.getEntityType());

        // 3. Uploader vers S3 (avec ACL public-read)
        String publicUrl = s3StorageService.uploadFile(file, fileKey, mimeType);

        // 4. Créer l'entité en base de données
        MediaFile mediaFile = new MediaFile();
        mediaFile.setUserId(request.getUserId());
        mediaFile.setEntityType(request.getEntityType());
        mediaFile.setEntityId(request.getEntityId());
        mediaFile.setBucketName(s3StorageService.getBucketName());
        mediaFile.setFileKey(fileKey);
        mediaFile.setPublicUrl(publicUrl);
        mediaFile.setOriginalFilename(file.getOriginalFilename());
        mediaFile.setMimeType(mimeType);
        mediaFile.setMediaType(mediaType);
        mediaFile.setFileSizeBytes(file.getSize());
        mediaFile.setStatus(MediaStatus.ACTIVE);

        MediaFile savedMedia = mediaFileRepository.save(mediaFile);
        log.info("Media uploaded: id={}, fileKey={}", savedMedia.getId(), fileKey);

        return mediaMapper.toMediaResponse(savedMedia);
    }

    /**
     * Récupère un média par son ID.
     */
    public MediaResponse getMediaById(UUID id) {
        MediaFile mediaFile = mediaFileRepository.findByIdAndStatus(id, MediaStatus.ACTIVE)
                .orElseThrow(() -> new MediaNotFoundException(id));
        return mediaMapper.toMediaResponse(mediaFile);
    }

    /**
     * Liste les médias d'un utilisateur.
     */
    public List<MediaResponse> getMediaByUser(UUID userId) {
        return mediaFileRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, MediaStatus.ACTIVE)
                .stream()
                .map(mediaMapper::toMediaResponse)
                .collect(Collectors.toList());
    }

    /**
     * Liste les médias associés à une entité.
     */
    public List<MediaResponse> getMediaByEntity(EntityType entityType, UUID entityId) {
        return mediaFileRepository
                .findByEntityTypeAndEntityIdAndStatusOrderByCreatedAtDesc(entityType, entityId, MediaStatus.ACTIVE)
                .stream()
                .map(mediaMapper::toMediaResponse)
                .collect(Collectors.toList());
    }

    /**
     * Suppression logique (soft delete).
     * Le fichier reste dans S3 mais est marqué comme supprimé en base.
     */
    @Transactional
    public void deleteMedia(UUID id, UUID requestingUserId) {
        MediaFile mediaFile = mediaFileRepository.findByIdAndStatus(id, MediaStatus.ACTIVE)
                .orElseThrow(() -> new MediaNotFoundException(id));

        // Vérifier que l'utilisateur est le propriétaire
        if (!mediaFile.getUserId().equals(requestingUserId)) {
            throw new IllegalArgumentException("You can only delete your own media");
        }

        mediaFile.setStatus(MediaStatus.DELETED);
        mediaFile.setDeletedAt(OffsetDateTime.now());
        mediaFileRepository.save(mediaFile);
        
        log.info("Media soft-deleted: id={}, by user={}", id, requestingUserId);
    }

    /**
     * Suppression physique (admin seulement).
     * Supprime le fichier de S3 et de la base de données.
     */
    @Transactional
    public void permanentlyDeleteMedia(UUID id) {
        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new MediaNotFoundException(id));

        // Supprimer de S3
        s3StorageService.deleteFile(mediaFile.getFileKey());

        // Supprimer de la base de données
        mediaFileRepository.delete(mediaFile);
        
        log.info("Media permanently deleted: id={}, fileKey={}", id, mediaFile.getFileKey());
    }

    /**
     * Compte le nombre de médias d'un utilisateur.
     */
    public long countUserMedia(UUID userId) {
        return mediaFileRepository.countByUserIdAndStatus(userId, MediaStatus.ACTIVE);
    }
}
