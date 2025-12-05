package com.medina.heritage.userauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.medina.heritage.userauth.dto.request.LoginRequest;
import com.medina.heritage.userauth.dto.request.RegisterRequest;
import com.medina.heritage.userauth.dto.response.AuthResponse;
import com.medina.heritage.userauth.dto.response.UserResponse;
import com.medina.heritage.userauth.enums.RoleName;
import com.medina.heritage.userauth.exception.BadRequestException;
import com.medina.heritage.userauth.service.AuthService;
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
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        objectMapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("POST /api/auth/register - Should register successfully")
    void shouldRegisterSuccessfully() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "newuser@example.com",
                "password123",
                "John",
                "Doe",
                "+212600000000"
        );

        UserResponse userResponse = new UserResponse();
        userResponse.setId(UUID.randomUUID());
        userResponse.setEmail(request.getEmail());
        userResponse.setFirstName(request.getFirstName());
        userResponse.setLastName(request.getLastName());
        userResponse.setRoles(Set.of(RoleName.CITIZEN.name()));
        userResponse.setIsActive(true);
        userResponse.setCreatedAt(OffsetDateTime.now());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUser(userResponse);
        authResponse.setMessage("User registered successfully");

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.email").value(request.getEmail()));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 when email exists")
    void shouldReturn400WhenEmailExists() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "existing@example.com",
                "password123",
                "John",
                "Doe",
                null
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new BadRequestException("Email already registered"));

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for invalid email")
    void shouldReturn400ForInvalidEmail() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "invalid-email",
                "password123",
                "John",
                "Doe",
                null
        );

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should login successfully")
    void shouldLoginSuccessfully() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        UserResponse userResponse = new UserResponse();
        userResponse.setId(UUID.randomUUID());
        userResponse.setEmail(request.getEmail());
        userResponse.setFirstName("John");
        userResponse.setLastName("Doe");
        userResponse.setRoles(Set.of(RoleName.CITIZEN.name()));
        userResponse.setIsActive(true);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUser(userResponse);
        authResponse.setMessage("Login successful");

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Login successful"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 400 for invalid credentials")
    void shouldReturn400ForInvalidCredentials() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("user@example.com", "wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadRequestException("Invalid email or password"));

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/logout - Should logout successfully")
    void shouldLogoutSuccessfully() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }
}
