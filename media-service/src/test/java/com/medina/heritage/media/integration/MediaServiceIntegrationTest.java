package com.medina.heritage.media.integration;

import com.medina.heritage.media.entity.MediaFile;
import com.medina.heritage.media.enums.EntityType;
import com.medina.heritage.media.enums.MediaStatus;
import com.medina.heritage.media.repository.MediaFileRepository;
import com.medina.heritage.media.service.S3StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MediaService.
 * Uses H2 database and mocked S3 service.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MediaServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @MockitoBean
    private S3Client s3Client;

    @MockitoBean
    private S3StorageService s3StorageService;

    private UUID testUserId;
    private UUID testEntityId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        testEntityId = UUID.fromString("550e8400-e29b-41d4-a716-446655440010");

        // Mock S3 storage service
        when(s3StorageService.getBucketName()).thenReturn("test-bucket");
        when(s3StorageService.uploadFile(any(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String fileKey = invocation.getArgument(1);
                    return "https://test-bucket.s3.amazonaws.com/" + fileKey;
                });
        doNothing().when(s3StorageService).deleteFile(anyString());
    }

    private MediaFile createAndSaveMediaFile(String filename, EntityType entityType, UUID entityId) {
        MediaFile mediaFile = new MediaFile();
        mediaFile.setUserId(testUserId);
        mediaFile.setEntityType(entityType);
        mediaFile.setEntityId(entityId);
        mediaFile.setBucketName("test-bucket");
        mediaFile.setFileKey("test/" + filename);
        mediaFile.setPublicUrl("https://test-bucket.s3.amazonaws.com/test/" + filename);
        mediaFile.setOriginalFilename(filename);
        mediaFile.setMimeType("image/jpeg");
        mediaFile.setMediaType(com.medina.heritage.media.enums.MediaType.IMAGE);
        mediaFile.setFileSizeBytes(1024L);
        mediaFile.setStatus(MediaStatus.ACTIVE);
        mediaFile.setCreatedAt(OffsetDateTime.now());
        return mediaFileRepository.save(mediaFile);
    }

    @Nested
    @DisplayName("Upload Integration Tests")
    class UploadIntegrationTests {

        @Test
        @DisplayName("Should upload file and persist to database")
        void shouldUploadFileAndPersistToDatabase() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "integration-test.jpg",
                    "image/jpeg",
                    "test content".getBytes()
            );

            // When
            MvcResult result = mockMvc.perform(multipart("/api/media/upload")
                            .file(file)
                            .param("userId", testUserId.toString())
                            .param("entityType", "REPORT")
                            .param("entityId", testEntityId.toString()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.originalFilename").value("integration-test.jpg"))
                    .andExpect(jsonPath("$.data.mediaType").value("IMAGE"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andReturn();

            // Then - verify persisted in database
            long count = mediaFileRepository.countByUserIdAndStatus(testUserId, MediaStatus.ACTIVE);
            assertThat(count).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should upload PDF document")
        void shouldUploadPdfDocument() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "document.pdf",
                    "application/pdf",
                    "PDF content".getBytes()
            );

            // When/Then
            mockMvc.perform(multipart("/api/media/upload")
                            .file(file)
                            .param("userId", testUserId.toString())
                            .param("entityType", "INSPECTION")
                            .param("entityId", testEntityId.toString()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.mediaType").value("DOCUMENT"))
                    .andExpect(jsonPath("$.data.mimeType").value("application/pdf"));
        }
    }

    @Nested
    @DisplayName("Get Media Integration Tests")
    class GetMediaIntegrationTests {

        @Test
        @DisplayName("Should get media by ID from database")
        void shouldGetMediaByIdFromDatabase() throws Exception {
            // Given
            MediaFile saved = createAndSaveMediaFile("get-test.jpg", EntityType.REPORT, testEntityId);

            // When/Then
            mockMvc.perform(get("/api/media/{id}", saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(saved.getId().toString()))
                    .andExpect(jsonPath("$.data.originalFilename").value("get-test.jpg"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent media")
        void shouldReturn404ForNonExistentMedia() throws Exception {
            // Given
            UUID unknownId = UUID.randomUUID();

            // When/Then
            mockMvc.perform(get("/api/media/{id}", unknownId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should list all media for user")
        void shouldListAllMediaForUser() throws Exception {
            // Given
            createAndSaveMediaFile("user-file1.jpg", EntityType.REPORT, testEntityId);
            createAndSaveMediaFile("user-file2.jpg", EntityType.BUILDING, UUID.randomUUID());
            createAndSaveMediaFile("user-file3.jpg", EntityType.USER_AVATAR, null);

            // When/Then
            mockMvc.perform(get("/api/media/user/{userId}", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3));
        }

        @Test
        @DisplayName("Should list media for entity")
        void shouldListMediaForEntity() throws Exception {
            // Given
            createAndSaveMediaFile("entity-file1.jpg", EntityType.REPORT, testEntityId);
            createAndSaveMediaFile("entity-file2.jpg", EntityType.REPORT, testEntityId);
            createAndSaveMediaFile("other-entity.jpg", EntityType.REPORT, UUID.randomUUID());

            // When/Then
            mockMvc.perform(get("/api/media/entity/{entityType}/{entityId}", "REPORT", testEntityId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("Should count user media")
        void shouldCountUserMedia() throws Exception {
            // Given
            createAndSaveMediaFile("count1.jpg", EntityType.REPORT, testEntityId);
            createAndSaveMediaFile("count2.jpg", EntityType.BUILDING, UUID.randomUUID());

            // When/Then
            mockMvc.perform(get("/api/media/user/{userId}/count", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(2));
        }
    }

    @Nested
    @DisplayName("Delete Media Integration Tests")
    class DeleteMediaIntegrationTests {

        @Test
        @DisplayName("Should soft delete media")
        void shouldSoftDeleteMedia() throws Exception {
            // Given
            MediaFile saved = createAndSaveMediaFile("to-delete.jpg", EntityType.REPORT, testEntityId);

            // When
            mockMvc.perform(delete("/api/media/{id}", saved.getId())
                            .param("userId", testUserId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Then - verify soft deleted in database
            MediaFile deleted = mediaFileRepository.findById(saved.getId()).orElseThrow();
            assertThat(deleted.getStatus()).isEqualTo(MediaStatus.DELETED);
            assertThat(deleted.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should not allow non-owner to delete")
        void shouldNotAllowNonOwnerToDelete() throws Exception {
            // Given
            MediaFile saved = createAndSaveMediaFile("protected.jpg", EntityType.REPORT, testEntityId);
            UUID otherUserId = UUID.randomUUID();

            // When/Then
            mockMvc.perform(delete("/api/media/{id}", saved.getId())
                            .param("userId", otherUserId.toString()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should permanently delete media")
        void shouldPermanentlyDeleteMedia() throws Exception {
            // Given
            MediaFile saved = createAndSaveMediaFile("permanent-delete.jpg", EntityType.REPORT, testEntityId);
            UUID savedId = saved.getId();

            // When
            mockMvc.perform(delete("/api/media/{id}/permanent", savedId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Then - verify removed from database
            boolean exists = mediaFileRepository.existsById(savedId);
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent media")
        void shouldReturn404WhenDeletingNonExistentMedia() throws Exception {
            // Given
            UUID unknownId = UUID.randomUUID();

            // When/Then
            mockMvc.perform(delete("/api/media/{id}", unknownId)
                            .param("userId", testUserId.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Workflow Integration Tests")
    class WorkflowIntegrationTests {

        @Test
        @DisplayName("Should complete upload-get-delete workflow")
        void shouldCompleteUploadGetDeleteWorkflow() throws Exception {
            // 1. Upload
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "workflow-test.jpg",
                    "image/jpeg",
                    "workflow content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/media/upload")
                            .file(file)
                            .param("userId", testUserId.toString())
                            .param("entityType", "REPORT")
                            .param("entityId", testEntityId.toString()))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Extract media ID from response
            String responseBody = uploadResult.getResponse().getContentAsString();
            String mediaIdStr = responseBody.split("\"id\":\"")[1].split("\"")[0];
            UUID mediaId = UUID.fromString(mediaIdStr);

            // 2. Get
            mockMvc.perform(get("/api/media/{id}", mediaId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.originalFilename").value("workflow-test.jpg"));

            // 3. List by user
            mockMvc.perform(get("/api/media/user/{userId}", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[?(@.id=='" + mediaId + "')]").exists());

            // 4. Soft delete
            mockMvc.perform(delete("/api/media/{id}", mediaId)
                            .param("userId", testUserId.toString()))
                    .andExpect(status().isOk());

            // 5. Verify not accessible
            mockMvc.perform(get("/api/media/{id}", mediaId))
                    .andExpect(status().isNotFound());

            // 6. Verify not in user list
            mockMvc.perform(get("/api/media/user/{userId}", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[?(@.id=='" + mediaId + "')]").doesNotExist());
        }
    }
}
