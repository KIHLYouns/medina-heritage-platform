package com.medina.heritage.userauth.service;

import com.medina.heritage.userauth.dto.request.UpdateProfileRequest;
import com.medina.heritage.userauth.dto.response.UserResponse;
import com.medina.heritage.userauth.entity.Role;
import com.medina.heritage.userauth.entity.User;
import com.medina.heritage.userauth.enums.RoleName;
import com.medina.heritage.userauth.exception.ResourceNotFoundException;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private Role citizenRole;
    private Role adminRole;
    private User testUser;
    private UserResponse testUserResponse;
    
    // Constantes pour les mots de passe hashés (simulation BCrypt)
    private static final String HASHED_PASSWORD = "$2a$12$hashedPasswordExample";

    @BeforeEach
    void setUp() {
        citizenRole = new Role(1, RoleName.CITIZEN.name());
        adminRole = new Role(2, RoleName.ADMIN.name());

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
        
        testUserResponse = createUserResponseFromUser(testUser);
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
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should get user by ID")
        void shouldGetUserById() {
            // Given
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userMapper.toUserResponse(testUser)).thenReturn(testUserResponse);

            // When
            UserResponse response = userService.getUserById(testUser.getId());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(testUser.getId());
            assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should throw exception when user not found by ID")
        void shouldThrowExceptionWhenUserNotFoundById() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.getUserById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should get user by email")
        void shouldGetUserByEmail() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(userMapper.toUserResponse(testUser)).thenReturn(testUserResponse);

            // When
            UserResponse response = userService.getUserByEmail(testUser.getEmail());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should get all users")
        void shouldGetAllUsers() {
            // Given
            User user2 = new User();
            user2.setId(UUID.randomUUID());
            user2.setEmail("user2@example.com");
            user2.setPasswordHash("pass");
            user2.setRoles(Set.of(citizenRole));
            user2.setIsActive(true);
            user2.setFirstName("Jane");
            user2.setLastName("Smith");
            user2.setCreatedAt(OffsetDateTime.now());
            user2.setUpdatedAt(OffsetDateTime.now());
            
            UserResponse user2Response = createUserResponseFromUser(user2);

            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));
            when(userMapper.toUserResponse(testUser)).thenReturn(testUserResponse);
            when(userMapper.toUserResponse(user2)).thenReturn(user2Response);

            // When
            List<UserResponse> responses = userService.getAllUsers();

            // Then
            assertThat(responses).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update user profile")
        void shouldUpdateUserProfile() {
            // Given
            UpdateProfileRequest request = new UpdateProfileRequest("Jane", "Smith", "+212699999999");
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toUserResponse(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return createUserResponseFromUser(user);
            });

            // When
            UserResponse response = userService.updateProfile(testUser.getId(), request);

            // Then
            assertThat(response).isNotNull();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            // Given
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFirstName("NewName");
            // lastName and phoneNumber are null

            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toUserResponse(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return createUserResponseFromUser(user);
            });

            // When
            userService.updateProfile(testUser.getId(), request);

            // Then
            assertThat(testUser.getFirstName()).isEqualTo("NewName");
            assertThat(testUser.getLastName()).isEqualTo("Doe"); // unchanged
        }
    }

    @Nested
    @DisplayName("Role Management Tests")
    class RoleManagementTests {

        @Test
        @DisplayName("Should update user roles")
        void shouldUpdateUserRoles() {
            // Given
            Set<String> newRoles = Set.of(RoleName.ADMIN.name(), RoleName.CITIZEN.name());
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName(RoleName.ADMIN.name())).thenReturn(Optional.of(adminRole));
            when(roleRepository.findByName(RoleName.CITIZEN.name())).thenReturn(Optional.of(citizenRole));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toUserResponse(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return createUserResponseFromUser(user);
            });

            // When
            UserResponse response = userService.updateUserRoles(testUser.getId(), newRoles);

            // Then
            assertThat(response).isNotNull();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when role not found")
        void shouldThrowExceptionWhenRoleNotFound() {
            // Given
            Set<String> newRoles = Set.of("UNKNOWN_ROLE");
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName("UNKNOWN_ROLE")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.updateUserRoles(testUser.getId(), newRoles))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("User Activation Tests")
    class UserActivationTests {

        @Test
        @DisplayName("Should deactivate user")
        void shouldDeactivateUser() {
            // Given
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.deactivateUser(testUser.getId());

            // Then
            assertThat(testUser.getIsActive()).isFalse();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should activate user")
        void shouldActivateUser() {
            // Given
            testUser.setIsActive(false);
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.activateUser(testUser.getId());

            // Then
            assertThat(testUser.getIsActive()).isTrue();
            verify(userRepository).save(testUser);
        }
    }
}
