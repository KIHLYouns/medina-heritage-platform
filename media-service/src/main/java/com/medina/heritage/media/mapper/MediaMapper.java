package com.medina.heritage.media.mapper;

import com.medina.heritage.media.dto.response.MediaResponse;
import com.medina.heritage.media.entity.MediaFile;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre MediaFile Entity et DTOs.
 */
@Component
public class MediaMapper {

    /**
     * Convertit une entité MediaFile vers un MediaResponse DTO.
     */
    public MediaResponse toMediaResponse(MediaFile mediaFile) {
        if (mediaFile == null) {
            return null;
        }

        MediaResponse response = new MediaResponse();
        response.setId(mediaFile.getId());
        response.setUserId(mediaFile.getUserId());
        response.setEntityType(mediaFile.getEntityType());
        response.setEntityId(mediaFile.getEntityId());
        response.setOriginalFilename(mediaFile.getOriginalFilename());
        response.setMimeType(mediaFile.getMimeType());
        response.setMediaType(mediaFile.getMediaType());
        response.setFileSizeBytes(mediaFile.getFileSizeBytes());
        response.setStatus(mediaFile.getStatus());
        response.setCreatedAt(mediaFile.getCreatedAt());
        // URL publique permanente
        response.setUrl(mediaFile.getPublicUrl());

        return response;
    }
}
