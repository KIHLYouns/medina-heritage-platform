package com.medina.heritage.media.service;

import com.medina.heritage.media.enums.EntityType;
import com.medina.heritage.media.enums.MediaType;
import com.medina.heritage.media.exception.FileSizeExceededException;
import com.medina.heritage.media.exception.InvalidFileTypeException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Service de validation des fichiers.
 */
@Service
public class FileValidationService {

    // Tailles maximales par type (en octets)
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;    // 10 MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;   // 100 MB
    private static final long MAX_DOCUMENT_SIZE = 20 * 1024 * 1024; // 20 MB

    // Types MIME autorisés
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg","image/jpg", "image/png", "image/webp", "image/gif"
    );

    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/quicktime", "video/webm"
    );

    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * Valide un fichier (type MIME et taille).
     * 
     * @param file le fichier à valider
     * @throws InvalidFileTypeException  si le type n'est pas autorisé
     * @throws FileSizeExceededException si la taille dépasse la limite
     */
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("File is empty or null");
        }

        String mimeType = file.getContentType();
        if (mimeType == null || mimeType.isBlank()) {
            throw new InvalidFileTypeException("Cannot determine file type");
        }

        MediaType mediaType = determineMediaType(mimeType);
        long maxSize = getMaxSize(mediaType);

        if (file.getSize() > maxSize) {
            throw new FileSizeExceededException(file.getSize(), maxSize);
        }
    }

    /**
     * Détermine le MediaType à partir du type MIME.
     * 
     * @param mimeType le type MIME
     * @return le MediaType correspondant
     * @throws InvalidFileTypeException si le type MIME n'est pas supporté
     */
    public MediaType determineMediaType(String mimeType) {
        if (mimeType == null) {
            throw new InvalidFileTypeException("null");
        }

        String lowerMimeType = mimeType.toLowerCase();

        if (ALLOWED_IMAGE_TYPES.contains(lowerMimeType)) {
            return MediaType.IMAGE;
        }
        if (ALLOWED_VIDEO_TYPES.contains(lowerMimeType)) {
            return MediaType.VIDEO;
        }
        if (ALLOWED_DOCUMENT_TYPES.contains(lowerMimeType)) {
            return MediaType.DOCUMENT;
        }

        throw new InvalidFileTypeException(mimeType);
    }

    /**
     * Retourne la taille maximale autorisée pour un type de média.
     */
    private long getMaxSize(MediaType mediaType) {
        return switch (mediaType) {
            case IMAGE -> MAX_IMAGE_SIZE;
            case VIDEO -> MAX_VIDEO_SIZE;
            case DOCUMENT -> MAX_DOCUMENT_SIZE;
        };
    }

    /**
     * Génère une clé S3 unique et organisée.
     * Format: {entityType}/{année}/{mois}/{uuid}-{filename}
     * Exemple: reports/2025/12/a1b2c3d4-photo.jpg
     * 
     * @param originalFilename le nom original du fichier
     * @param entityType       le type d'entité (optionnel)
     * @return la clé S3
     */
    public String generateFileKey(String originalFilename, EntityType entityType) {
        LocalDate now = LocalDate.now();
        String folder = entityType != null ? entityType.name().toLowerCase() + "s" : "misc";
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String sanitizedFilename = sanitizeFilename(originalFilename);

        return String.format("%s/%s/%s/%s-%s", folder, year, month, uuid, sanitizedFilename);
    }

    /**
     * Nettoie un nom de fichier pour le rendre valide pour S3.
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }
        // Remplacer les caractères non-alphanumériques (sauf . - _) par _
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
    }

    /**
     * Vérifie si un type MIME est supporté.
     */
    public boolean isSupportedMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        String lowerMimeType = mimeType.toLowerCase();
        return ALLOWED_IMAGE_TYPES.contains(lowerMimeType)
                || ALLOWED_VIDEO_TYPES.contains(lowerMimeType)
                || ALLOWED_DOCUMENT_TYPES.contains(lowerMimeType);
    }
}
