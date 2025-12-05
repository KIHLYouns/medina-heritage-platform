package com.medina.heritage.userauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.medina.heritage.userauth.dto.request.UpdateProfileRequest;
import com.medina.heritage.userauth.dto.response.UserResponse;
import com.medina.heritage.userauth.enums.RoleName;
import com.medina.heritage.userauth.exception.ResourceNotFoundException;
import com.medina.heritage.userauth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        objectMapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        objectMapper.findAndRegisterModules();
    }

    private UserResponse createTestUserResponse() {
        UserResponse response = new UserResponse();
        response.setId(UUID.randomUUID());
        response.setEmail("test@example.com");
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setPhoneNumber("+212600000000");
        response.setRoles(Set.of(RoleName.CITIZEN.name()));
        response.setIsActive(true);
        response.setCreatedAt(OffsetDateTime.now());
        response.setUpdatedAt(OffsetDateTime.now());
        return response;
    }

    @Test
    @DisplayName("GET /api/users - Should return all users")
    void shouldReturnAllUsers() throws Exception {
        // Given
        UserResponse user1 = createTestUserResponse();
        UserResponse user2 = createTestUserResponse();
        user2.setEmail("user2@example.com");

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        // When/Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return user by ID")
    void shouldReturnUserById() throws Exception {
        // Given
        UserResponse userResponse = createTestUserResponse();
        when(userService.getUserById(userResponse.getId())).thenReturn(userResponse);

        // When/Then
        mockMvc.perform(get("/api/users/{id}", userResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(userResponse.getEmail()));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        // Given
        UUID unknownId = UUID.randomUUID();
        when(userService.getUserById(unknownId))
                .thenThrow(new ResourceNotFoundException("User", "id", unknownId));

        // When/Then
        mockMvc.perform(get("/api/users/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Should update user profile")
    void shouldUpdateUserProfile() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UpdateProfileRequest request = new UpdateProfileRequest("Jane", "Smith", "+212699999999");
        UserResponse updatedUser = createTestUserResponse();
        updatedUser.setId(userId);
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Smith");

        when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class))).thenReturn(updatedUser);

        // When/Then
        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Jane"));
    }

    @Test
    @DisplayName("PUT /api/users/{id}/roles - Should update user roles")
    void shouldUpdateUserRoles() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        Set<String> newRoles = Set.of(RoleName.ADMIN.name(), RoleName.CITIZEN.name());
        UserResponse updatedUser = createTestUserResponse();
        updatedUser.setId(userId);
        updatedUser.setRoles(newRoles);

        when(userService.updateUserRoles(eq(userId), any())).thenReturn(updatedUser);

        // When/Then
        mockMvc.perform(put("/api/users/{id}/roles", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRoles)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should deactivate user")
    void shouldDeactivateUser() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).deactivateUser(userId);

        // When/Then
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deactivated successfully"));
    }

    @Test
    @DisplayName("PUT /api/users/{id}/activate - Should activate user")
    void shouldActivateUser() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).activateUser(userId);

        // When/Then
        mockMvc.perform(put("/api/users/{id}/activate", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User activated successfully"));
    }
}
