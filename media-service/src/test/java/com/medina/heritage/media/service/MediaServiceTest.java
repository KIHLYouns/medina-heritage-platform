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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaFileRepository mediaFileRepository;

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private FileValidationService fileValidationService;

    @Mock
    private MediaMapper mediaMapper;

    @InjectMocks
    private MediaService mediaService;

    private MediaFile testMediaFile;
    private MediaResponse testMediaResponse;
    private UUID testUserId;
    private UUID testEntityId;
    private UUID testMediaId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEntityId = UUID.randomUUID();
        testMediaId = UUID.randomUUID();

        testMediaFile = new MediaFile();
        testMediaFile.setId(testMediaId);
        testMediaFile.setUserId(testUserId);
        testMediaFile.setEntityType(EntityType.REPORT);
        testMediaFile.setEntityId(testEntityId);
        testMediaFile.setBucketName("test-bucket");
        testMediaFile.setFileKey("reports/2025/12/test-image.jpg");
        testMediaFile.setPublicUrl("https://test-bucket.s3.amazonaws.com/reports/2025/12/test-image.jpg");
        testMediaFile.setOriginalFilename("test-image.jpg");
        testMediaFile.setMimeType("image/jpeg");
        testMediaFile.setMediaType(MediaType.IMAGE);
        testMediaFile.setFileSizeBytes(1024L);
        testMediaFile.setStatus(MediaStatus.ACTIVE);
        testMediaFile.setCreatedAt(OffsetDateTime.now());

        testMediaResponse = createMediaResponse(testMediaFile);
    }

    private MediaResponse createMediaResponse(MediaFile mediaFile) {
        return new MediaResponse(
                mediaFile.getId(),
                mediaFile.getUserId(),
                mediaFile.getEntityType(),
                mediaFile.getEntityId(),
                mediaFile.getOriginalFilename(),
                mediaFile.getMimeType(),
                mediaFile.getMediaType(),
                mediaFile.getFileSizeBytes(),
                mediaFile.getStatus(),
                mediaFile.getCreatedAt(),
                mediaFile.getPublicUrl()
        );
    }

    @Nested
    @DisplayName("Upload File Tests")
    class UploadFileTests {

        @Test
        @DisplayName("Should upload file successfully")
        void shouldUploadFileSuccessfully() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test-image.jpg",
                    "image/jpeg",
                    "test content".getBytes()
            );

            UploadMediaRequest request = new UploadMediaRequest();
            request.setUserId(testUserId);
            request.setEntityType(EntityType.REPORT);
            request.setEntityId(testEntityId);

            String fileKey = "reports/2025/12/uuid-test-image.jpg";
            String publicUrl = "https://test-bucket.s3.amazonaws.com/" + fileKey;

            doNothing().when(fileValidationService).validateFile(any(MultipartFile.class));
            when(fileValidationService.determineMediaType("image/jpeg")).thenReturn(MediaType.IMAGE);
            when(fileValidationService.generateFileKey(anyString(), any(EntityType.class))).thenReturn(fileKey);
            when(s3StorageService.uploadFile(any(MultipartFile.class), anyString(), anyString())).thenReturn(publicUrl);
            when(s3StorageService.getBucketName()).thenReturn("test-bucket");
            when(mediaFileRepository.save(any(MediaFile.class))).thenReturn(testMediaFile);
            when(mediaMapper.toMediaResponse(any(MediaFile.class))).thenReturn(testMediaResponse);

            // When
            MediaResponse response = mediaService.uploadFile(file, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(testMediaId);
            assertThat(response.getUrl()).isEqualTo(testMediaFile.getPublicUrl());
            verify(fileValidationService).validateFile(file);
            verify(s3StorageService).uploadFile(any(MultipartFile.class), eq(fileKey), eq("image/jpeg"));
            verify(mediaFileRepository).save(any(MediaFile.class));
        }

        @Test
        @DisplayName("Should upload video file successfully")
        void shouldUploadVideoFileSuccessfully() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test-video.mp4",
                    "video/mp4",
                    "video content".getBytes()
            );

            UploadMediaRequest request = new UploadMediaRequest();
            request.setUserId(testUserId);
            request.setEntityType(EntityType.BUILDING);
            request.setEntityId(testEntityId);

            testMediaFile.setMediaType(MediaType.VIDEO);
            testMediaFile.setMimeType("video/mp4");
            testMediaResponse = createMediaResponse(testMediaFile);

            doNothing().when(fileValidationService).validateFile(any(MultipartFile.class));
            when(fileValidationService.determineMediaType("video/mp4")).thenReturn(MediaType.VIDEO);
            when(fileValidationService.generateFileKey(anyString(), any(EntityType.class))).thenReturn("buildings/2025/12/test.mp4");
            when(s3StorageService.uploadFile(any(), anyString(), anyString())).thenReturn("https://bucket.s3.amazonaws.com/test.mp4");
            when(s3StorageService.getBucketName()).thenReturn("test-bucket");
            when(mediaFileRepository.save(any(MediaFile.class))).thenReturn(testMediaFile);
            when(mediaMapper.toMediaResponse(any(MediaFile.class))).thenReturn(testMediaResponse);

            // When
            MediaResponse response = mediaService.uploadFile(file, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMediaType()).isEqualTo(MediaType.VIDEO);
        }
    }

    @Nested
    @DisplayName("Get Media Tests")
    class GetMediaTests {

        @Test
        @DisplayName("Should get media by ID")
        void shouldGetMediaById() {
            // Given
            when(mediaFileRepository.findByIdAndStatus(testMediaId, MediaStatus.ACTIVE))
                    .thenReturn(Optional.of(testMediaFile));
            when(mediaMapper.toMediaResponse(testMediaFile)).thenReturn(testMediaResponse);

            // When
            MediaResponse response = mediaService.getMediaById(testMediaId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(testMediaId);
            assertThat(response.getOriginalFilename()).isEqualTo("test-image.jpg");
        }

        @Test
        @DisplayName("Should throw exception when media not found")
        void shouldThrowExceptionWhenMediaNotFound() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(mediaFileRepository.findByIdAndStatus(unknownId, MediaStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> mediaService.getMediaById(unknownId))
                    .isInstanceOf(MediaNotFoundException.class);
        }

        @Test
        @DisplayName("Should get media by user")
        void shouldGetMediaByUser() {
            // Given
            MediaFile secondMedia = new MediaFile();
            secondMedia.setId(UUID.randomUUID());
            secondMedia.setUserId(testUserId);
            secondMedia.setOriginalFilename("second.jpg");
            secondMedia.setStatus(MediaStatus.ACTIVE);

            List<MediaFile> mediaFiles = List.of(testMediaFile, secondMedia);
            when(mediaFileRepository.findByUserIdAndStatusOrderByCreatedAtDesc(testUserId, MediaStatus.ACTIVE))
                    .thenReturn(mediaFiles);
            when(mediaMapper.toMediaResponse(any(MediaFile.class))).thenReturn(testMediaResponse);

            // When
            List<MediaResponse> responses = mediaService.getMediaByUser(testUserId);

            // Then
            assertThat(responses).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when user has no media")
        void shouldReturnEmptyListWhenUserHasNoMedia() {
            // Given
            UUID userId = UUID.randomUUID();
            when(mediaFileRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, MediaStatus.ACTIVE))
                    .thenReturn(Collections.emptyList());

            // When
            List<MediaResponse> responses = mediaService.getMediaByUser(userId);

            // Then
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should get media by entity")
        void shouldGetMediaByEntity() {
            // Given
            when(mediaFileRepository.findByEntityTypeAndEntityIdAndStatusOrderByCreatedAtDesc(
                    EntityType.REPORT, testEntityId, MediaStatus.ACTIVE))
                    .thenReturn(List.of(testMediaFile));
            when(mediaMapper.toMediaResponse(testMediaFile)).thenReturn(testMediaResponse);

            // When
            List<MediaResponse> responses = mediaService.getMediaByEntity(EntityType.REPORT, testEntityId);

            // Then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getEntityType()).isEqualTo(EntityType.REPORT);
        }
    }

    @Nested
    @DisplayName("Delete Media Tests")
    class DeleteMediaTests {

        @Test
        @DisplayName("Should soft delete media by owner")
        void shouldSoftDeleteMediaByOwner() {
            // Given
            when(mediaFileRepository.findByIdAndStatus(testMediaId, MediaStatus.ACTIVE))
                    .thenReturn(Optional.of(testMediaFile));
            when(mediaFileRepository.save(any(MediaFile.class))).thenReturn(testMediaFile);

            // When
            mediaService.deleteMedia(testMediaId, testUserId);

            // Then
            verify(mediaFileRepository).save(argThat(media -> 
                media.getStatus() == MediaStatus.DELETED && 
                media.getDeletedAt() != null
            ));
        }

        @Test
        @DisplayName("Should throw exception when non-owner tries to delete")
        void shouldThrowExceptionWhenNonOwnerTriesToDelete() {
            // Given
            UUID otherUserId = UUID.randomUUID();
            when(mediaFileRepository.findByIdAndStatus(testMediaId, MediaStatus.ACTIVE))
                    .thenReturn(Optional.of(testMediaFile));

            // When/Then
            assertThatThrownBy(() -> mediaService.deleteMedia(testMediaId, otherUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("You can only delete your own media");
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent media")
        void shouldThrowExceptionWhenDeletingNonExistentMedia() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(mediaFileRepository.findByIdAndStatus(unknownId, MediaStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> mediaService.deleteMedia(unknownId, testUserId))
                    .isInstanceOf(MediaNotFoundException.class);
        }

        @Test
        @DisplayName("Should permanently delete media")
        void shouldPermanentlyDeleteMedia() {
            // Given
            when(mediaFileRepository.findById(testMediaId)).thenReturn(Optional.of(testMediaFile));
            doNothing().when(s3StorageService).deleteFile(testMediaFile.getFileKey());
            doNothing().when(mediaFileRepository).delete(testMediaFile);

            // When
            mediaService.permanentlyDeleteMedia(testMediaId);

            // Then
            verify(s3StorageService).deleteFile(testMediaFile.getFileKey());
            verify(mediaFileRepository).delete(testMediaFile);
        }

        @Test
        @DisplayName("Should throw exception when permanently deleting non-existent media")
        void shouldThrowExceptionWhenPermanentlyDeletingNonExistentMedia() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(mediaFileRepository.findById(unknownId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> mediaService.permanentlyDeleteMedia(unknownId))
                    .isInstanceOf(MediaNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Count Media Tests")
    class CountMediaTests {

        @Test
        @DisplayName("Should count user media")
        void shouldCountUserMedia() {
            // Given
            when(mediaFileRepository.countByUserIdAndStatus(testUserId, MediaStatus.ACTIVE))
                    .thenReturn(5L);

            // When
            long count = mediaService.countUserMedia(testUserId);

            // Then
            assertThat(count).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should return zero when user has no media")
        void shouldReturnZeroWhenUserHasNoMedia() {
            // Given
            UUID userId = UUID.randomUUID();
            when(mediaFileRepository.countByUserIdAndStatus(userId, MediaStatus.ACTIVE))
                    .thenReturn(0L);

            // When
            long count = mediaService.countUserMedia(userId);

            // Then
            assertThat(count).isZero();
        }
    }
}
