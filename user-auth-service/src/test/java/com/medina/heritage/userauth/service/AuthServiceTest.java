package com.medina.heritage.userauth.service;

import com.medina.heritage.userauth.dto.request.ChangePasswordRequest;
import com.medina.heritage.userauth.dto.request.LoginRequest;
import com.medina.heritage.userauth.dto.request.RegisterRequest;
import com.medina.heritage.userauth.dto.response.AuthResponse;
import com.medina.heritage.userauth.dto.response.UserResponse;
import com.medina.heritage.userauth.entity.Role;
import com.medina.heritage.userauth.entity.User;
import com.medina.heritage.userauth.enums.RoleName;
import com.medina.heritage.userauth.exception.BadRequestException;
import com.medina.heritage.userauth.exception.ResourceNotFoundException;
import com.medina.heritage.userauth.integration.GamificationServiceClient;
import com.medina.heritage.userauth.integration.SalesforceServiceClient;
import com.medina.heritage.userauth.mapper.UserMapper;
import com.medina.heritage.userauth.repository.RoleRepository;
import com.medina.heritage.userauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private SalesforceServiceClient salesforceServiceClient;

    @Mock
    private GamificationServiceClient gamificationServiceClient;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private AuthService authService;

    private Role citizenRole;
    private User testUser;
    private UserResponse testUserResponse;
    
    private static final String RAW_PASSWORD = "password123";
    private static final String HASHED_PASSWORD = "$2a$12$hashedPasswordExample";

    @BeforeEach
    void setUp() {
        citizenRole = new Role(1, RoleName.CITIZEN.name());
        
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(HASHED_PASSWORD); // Mot de passe hashé
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhoneNumber("+212600000000");
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(citizenRole));
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());
        
        testUserResponse = new UserResponse(
                testUser.getId(),
                testUser.getEmail(),
                testUser.getFirstName(),
                testUser.getLastName(),
                testUser.getPhoneNumber(),
                testUser.getIsActive(),
                testUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                testUser.getCreatedAt(),
                testUser.getUpdatedAt()
        );
    }
    
    private UserResponse createUserResponseFromUser(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getIsActive(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register a new user successfully")
        void shouldRegisterNewUser() {
            // Given
            RegisterRequest request = new RegisterRequest(
                    "newuser@example.com",
                    RAW_PASSWORD,
                    "Jane",
                    "Doe",
                    "+212611111111"
            );
            
            User newUser = new User();
            newUser.setEmail(request.getEmail());
            newUser.setFirstName(request.getFirstName());
            newUser.setLastName(request.getLastName());
            newUser.setPhoneNumber(request.getPhoneNumber());
            
            when(userMapper.toUser(request)).thenReturn(newUser);
            when(passwordService.hashPassword(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(roleRepository.findByName(RoleName.CITIZEN.name())).thenReturn(Optional.of(citizenRole));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(UUID.randomUUID());
                user.setCreatedAt(OffsetDateTime.now());
                user.setUpdatedAt(OffsetDateTime.now());
                user.setRoles(Set.of(citizenRole));
                return user;
            });
            when(userMapper.toUserResponse(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return createUserResponseFromUser(user);
            });

            // When
            AuthResponse response = authService.register(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isEqualTo("User registered successfully");
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getEmail()).isEqualTo(request.getEmail());

            verify(passwordService).hashPassword(RAW_PASSWORD);
            verify(userRepository).save(any(User.class));
            verify(salesforceServiceClient).notifyUserCreated(any());
            verify(gamificationServiceClient).createUserWallet(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            RegisterRequest request = new RegisterRequest(
                    "existing@example.com",
                    "password123",
                    "John",
                    "Doe",
                    null
            );

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Email already registered");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create CITIZEN role if not exists")
        void shouldCreateCitizenRoleIfNotExists() {
            // Given
            RegisterRequest request = new RegisterRequest(
                    "newuser@example.com",
                    RAW_PASSWORD,
                    "Jane",
                    "Doe",
                    null
            );
            
            User newUser = new User();
            newUser.setEmail(request.getEmail());
            newUser.setFirstName(request.getFirstName());
            newUser.setLastName(request.getLastName());
            
            when(userMapper.toUser(request)).thenReturn(newUser);
            when(passwordService.hashPassword(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(roleRepository.findByName(RoleName.CITIZEN.name())).thenReturn(Optional.empty());
            when(roleRepository.save(any(Role.class))).thenReturn(citizenRole);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(UUID.randomUUID());
                user.setCreatedAt(OffsetDateTime.now());
                user.setUpdatedAt(OffsetDateTime.now());
                user.setRoles(Set.of(citizenRole));
                return user;
            });
            when(userMapper.toUserResponse(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return createUserResponseFromUser(user);
            });

            // When
            authService.register(request);

            // Then
            verify(roleRepository).save(any(Role.class));
            verify(passwordService).hashPassword(RAW_PASSWORD);
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            // Given
            LoginRequest request = new LoginRequest("test@example.com", RAW_PASSWORD);
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
            when(userMapper.toUserResponse(testUser)).thenReturn(testUserResponse);

            // When
            AuthResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMessage()).isEqualTo("Login successful");
            assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());
            verify(passwordService).verifyPassword(RAW_PASSWORD, HASHED_PASSWORD);
        }

        @Test
        @DisplayName("Should throw exception with invalid email")
        void shouldThrowExceptionWithInvalidEmail() {
            // Given
            LoginRequest request = new LoginRequest("unknown@example.com", RAW_PASSWORD);
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw exception with invalid password")
        void shouldThrowExceptionWithInvalidPassword() {
            // Given
            LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword("wrongpassword", HASHED_PASSWORD)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw exception when account is deactivated")
        void shouldThrowExceptionWhenAccountDeactivated() {
            // Given
            testUser.setIsActive(false);
            LoginRequest request = new LoginRequest("test@example.com", RAW_PASSWORD);
            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Account is deactivated");
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Given
            UUID userId = testUser.getId();
            String newPassword = "newPassword456";
            String newHashedPassword = "$2a$12$newHashedPassword";
            ChangePasswordRequest request = new ChangePasswordRequest(RAW_PASSWORD, newPassword);
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
            when(passwordService.hashPassword(newPassword)).thenReturn(newHashedPassword);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            authService.changePassword(userId, request);

            // Then
            verify(passwordService).verifyPassword(RAW_PASSWORD, HASHED_PASSWORD);
            verify(passwordService).hashPassword(newPassword);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            UUID unknownUserId = UUID.randomUUID();
            ChangePasswordRequest request = new ChangePasswordRequest(RAW_PASSWORD, "newPassword456");
            when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.changePassword(unknownUserId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when current password is incorrect")
        void shouldThrowExceptionWhenCurrentPasswordIncorrect() {
            // Given
            UUID userId = testUser.getId();
            ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newPassword456");
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(passwordService.verifyPassword("wrongPassword", HASHED_PASSWORD)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> authService.changePassword(userId, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Current password is incorrect");
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully with valid userId")
        void shouldLogoutSuccessfully() {
            // Given
            UUID userId = testUser.getId();
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When - No exception should be thrown
            authService.logout(userId);

            // Then
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("Should handle logout with null userId")
        void shouldHandleLogoutWithNullUserId() {
            // When - No exception should be thrown
            authService.logout(null);

            // Then
            verify(userRepository, never()).findById(any());
        }
    }
}
