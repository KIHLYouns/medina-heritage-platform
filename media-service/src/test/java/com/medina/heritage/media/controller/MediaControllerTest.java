package com.medina.heritage.media.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.medina.heritage.media.dto.response.MediaResponse;
import com.medina.heritage.media.enums.EntityType;
import com.medina.heritage.media.enums.MediaStatus;
import com.medina.heritage.media.exception.MediaNotFoundException;
import com.medina.heritage.media.service.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MediaController.class)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private MediaService mediaService;

    private UUID testUserId;
    private UUID testMediaId;
    private UUID testEntityId;
    private MediaResponse testMediaResponse;

    @BeforeEach
    void setUp() {
        objectMapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        objectMapper.findAndRegisterModules();

        testUserId = UUID.randomUUID();
        testMediaId = UUID.randomUUID();
        testEntityId = UUID.randomUUID();

        testMediaResponse = createTestMediaResponse();
    }

    private MediaResponse createTestMediaResponse() {
        return new MediaResponse(
                testMediaId,
                testUserId,
                EntityType.REPORT,
                testEntityId,
                "test-image.jpg",
                "image/jpeg",
                com.medina.heritage.media.enums.MediaType.IMAGE,
                1024L,
                MediaStatus.ACTIVE,
                OffsetDateTime.now(),
                "https://bucket.s3.amazonaws.com/test-image.jpg"
        );
    }

    @Nested
    @DisplayName("Upload Media Tests")
    class UploadMediaTests {

        @Test
        @DisplayName("POST /api/media/upload - Should upload file successfully")
        void shouldUploadFileSuccessfully() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test-image.jpg",
                    "image/jpeg",
                    "test content".getBytes()
            );

            when(mediaService.uploadFile(any(), any())).thenReturn(testMediaResponse);

            // When/Then
            mockMvc.perform(multipart("/api/media/upload")
                            .file(file)
                            .param("userId", testUserId.toString())
                            .param("entityType", "REPORT")
                            .param("entityId", testEntityId.toString()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Media uploaded successfully"))
                    .andExpect(jsonPath("$.data.id").value(testMediaId.toString()))
                    .andExpect(jsonPath("$.data.originalFilename").value("test-image.jpg"));
        }

        @Test
        @DisplayName("POST /api/media/upload - Should upload without entityId")
        void shouldUploadWithoutEntityId() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "avatar.png",
                    "image/png",
                    "avatar content".getBytes()
            );

            testMediaResponse = new MediaResponse(
                    testMediaId,
                    testUserId,
                    EntityType.USER_AVATAR,
                    null,
                    "avatar.png",
                    "image/png",
                    com.medina.heritage.media.enums.MediaType.IMAGE,
                    512L,
                    MediaStatus.ACTIVE,
                    OffsetDateTime.now(),
                    "https://bucket.s3.amazonaws.com/avatar.png"
            );

            when(mediaService.uploadFile(any(), any())).thenReturn(testMediaResponse);

            // When/Then
            mockMvc.perform(multipart("/api/media/upload")
                            .file(file)
                            .param("userId", testUserId.toString())
                            .param("entityType", "USER_AVATAR"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.entityId").isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Media Tests")
    class GetMediaTests {

        @Test
        @DisplayName("GET /api/media/{id} - Should return media by ID")
        void shouldReturnMediaById() throws Exception {
            // Given
            when(mediaService.getMediaById(testMediaId)).thenReturn(testMediaResponse);

            // When/Then
            mockMvc.perform(get("/api/media/{id}", testMediaId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(testMediaId.toString()))
                    .andExpect(jsonPath("$.data.originalFilename").value("test-image.jpg"));
        }

        @Test
        @DisplayName("GET /api/media/{id} - Should return 404 when media not found")
        void shouldReturn404WhenMediaNotFound() throws Exception {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(mediaService.getMediaById(unknownId))
                    .thenThrow(new MediaNotFoundException(unknownId));

            // When/Then
            mockMvc.perform(get("/api/media/{id}", unknownId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("GET /api/media/user/{userId} - Should return user media")
        void shouldReturnUserMedia() throws Exception {
            // Given
            MediaResponse secondMedia = new MediaResponse(
                    UUID.randomUUID(),
                    testUserId,
                    EntityType.REPORT,
                    UUID.randomUUID(),
                    "second.jpg",
                    "image/jpeg",
                    com.medina.heritage.media.enums.MediaType.IMAGE,
                    2048L,
                    MediaStatus.ACTIVE,
                    OffsetDateTime.now(),
                    "https://bucket.s3.amazonaws.com/second.jpg"
            );

            when(mediaService.getMediaByUser(testUserId))
                    .thenReturn(Arrays.asList(testMediaResponse, secondMedia));

            // When/Then
            mockMvc.perform(get("/api/media/user/{userId}", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("GET /api/media/user/{userId} - Should return empty list when no media")
        void shouldReturnEmptyListWhenNoMedia() throws Exception {
            // Given
            when(mediaService.getMediaByUser(testUserId)).thenReturn(Collections.emptyList());

            // When/Then
            mockMvc.perform(get("/api/media/user/{userId}", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /api/media/entity/{entityType}/{entityId} - Should return entity media")
        void shouldReturnEntityMedia() throws Exception {
            // Given
            when(mediaService.getMediaByEntity(EntityType.REPORT, testEntityId))
                    .thenReturn(List.of(testMediaResponse));

            // When/Then
            mockMvc.perform(get("/api/media/entity/{entityType}/{entityId}", "REPORT", testEntityId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].entityType").value("REPORT"));
        }

        @Test
        @DisplayName("GET /api/media/user/{userId}/count - Should return media count")
        void shouldReturnMediaCount() throws Exception {
            // Given
            when(mediaService.countUserMedia(testUserId)).thenReturn(5L);

            // When/Then
            mockMvc.perform(get("/api/media/user/{userId}/count", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(5));
        }
    }

    @Nested
    @DisplayName("Delete Media Tests")
    class DeleteMediaTests {

        @Test
        @DisplayName("DELETE /api/media/{id} - Should soft delete media")
        void shouldSoftDeleteMedia() throws Exception {
            // Given
            doNothing().when(mediaService).deleteMedia(testMediaId, testUserId);

            // When/Then
            mockMvc.perform(delete("/api/media/{id}", testMediaId)
                            .param("userId", testUserId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Media deleted successfully"));

            verify(mediaService).deleteMedia(testMediaId, testUserId);
        }

        @Test
        @DisplayName("DELETE /api/media/{id} - Should return 404 when media not found")
        void shouldReturn404WhenDeletingNonExistentMedia() throws Exception {
            // Given
            UUID unknownId = UUID.randomUUID();
            doThrow(new MediaNotFoundException(unknownId))
                    .when(mediaService).deleteMedia(eq(unknownId), any());

            // When/Then
            mockMvc.perform(delete("/api/media/{id}", unknownId)
                            .param("userId", testUserId.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("DELETE /api/media/{id}/permanent - Should permanently delete media")
        void shouldPermanentlyDeleteMedia() throws Exception {
            // Given
            doNothing().when(mediaService).permanentlyDeleteMedia(testMediaId);

            // When/Then
            mockMvc.perform(delete("/api/media/{id}/permanent", testMediaId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Media permanently deleted"));

            verify(mediaService).permanentlyDeleteMedia(testMediaId);
        }

        @Test
        @DisplayName("DELETE /api/media/{id}/permanent - Should return 404 when media not found")
        void shouldReturn404WhenPermanentlyDeletingNonExistentMedia() throws Exception {
            // Given
            UUID unknownId = UUID.randomUUID();
            doThrow(new MediaNotFoundException(unknownId))
                    .when(mediaService).permanentlyDeleteMedia(unknownId);

            // When/Then
            mockMvc.perform(delete("/api/media/{id}/permanent", unknownId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
