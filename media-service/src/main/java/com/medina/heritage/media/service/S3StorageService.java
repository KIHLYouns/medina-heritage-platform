package com.medina.heritage.media.service;

import com.medina.heritage.media.exception.FileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

/**
 * Service de stockage S3 avec URLs publiques permanentes.
 * 
 * Le bucket doit être configuré avec une politique permettant l'accès public en lecture.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region:}")
    private String region;

    /**
     * Upload un fichier vers S3 avec accès public.
     * 
     * @param file     le fichier à uploader
     * @param fileKey  la clé (chemin) dans le bucket
     * @param mimeType le type MIME du fichier
     * @return l'URL publique permanente du fichier
     */
    public String uploadFile(MultipartFile file, String fileKey, String mimeType) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(mimeType)
                    .contentLength(file.getSize())
                    // ACL pour accès public en lecture
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String publicUrl = buildPublicUrl(fileKey);
            log.info("File uploaded successfully: {} -> {}", fileKey, publicUrl);
            
            return publicUrl;
        } catch (IOException e) {
            log.error("Failed to upload file: {}", fileKey, e);
            throw new FileUploadException("Failed to read file content", e);
        } catch (S3Exception e) {
            log.error("S3 error uploading file: {}", fileKey, e);
            throw new FileUploadException("Failed to upload to S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * Supprime un fichier de S3.
     * 
     * @param fileKey la clé du fichier à supprimer
     */
    public void deleteFile(String fileKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("File deleted successfully: {}", fileKey);
        } catch (S3Exception e) {
            log.error("S3 error deleting file: {}", fileKey, e);
            throw new FileUploadException("Failed to delete from S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * Vérifie si un fichier existe dans S3.
     * 
     * @param fileKey la clé du fichier
     * @return true si le fichier existe
     */
    public boolean fileExists(String fileKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            
            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("S3 error checking file existence: {}", fileKey, e);
            return false;
        }
    }

    /**
     * Construit l'URL publique permanente pour un fichier S3.
     * Format: https://{bucket}.s3.{region}.amazonaws.com/{fileKey}
     */
    public String buildPublicUrl(String fileKey) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileKey);
    }

    /**
     * Retourne le nom du bucket configuré.
     */
    public String getBucketName() {
        return bucketName;
    }
}
