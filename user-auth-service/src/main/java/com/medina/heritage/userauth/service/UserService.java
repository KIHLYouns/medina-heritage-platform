package com.medina.heritage.userauth.service;

import com.medina.heritage.events.user.UserDeletedEvent;
import com.medina.heritage.events.user.UserUpdatedEvent;
import com.medina.heritage.userauth.dto.request.UpdateProfileRequest;
import com.medina.heritage.userauth.dto.response.UserResponse;
import com.medina.heritage.userauth.entity.Role;
import com.medina.heritage.userauth.entity.User;
import com.medina.heritage.userauth.exception.ResourceNotFoundException;
import com.medina.heritage.userauth.mapper.UserMapper;
import com.medina.heritage.userauth.messaging.UserEventPublisher;
import com.medina.heritage.userauth.repository.RoleRepository;
import com.medina.heritage.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final UserEventPublisher userEventPublisher;

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toUserResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        
        // Publish UserUpdatedEvent
        publishUserUpdatedEvent(updatedUser);
        
        return userMapper.toUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse updateUserRoles(UUID userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        
        // Publish UserUpdatedEvent
        publishUserUpdatedEvent(updatedUser);
        
        return userMapper.toUserResponse(updatedUser);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsActive(false);
        userRepository.save(user);
        
        // Publish UserDeletedEvent (soft delete)
        publishUserDeletedEvent(user);
    }

    @Transactional
    public void activateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsActive(true);
        userRepository.save(user);
    }
    
    /**
     * Publish UserUpdatedEvent to the message broker.
     */
    private void publishUserUpdatedEvent(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        UserUpdatedEvent event = UserUpdatedEvent.builder()
                .userId(user.getId().getMostSignificantBits())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .roles(roleNames)
                .sfContactId(user.getSfContactId())
                .build();

        try {
            userEventPublisher.publishUserUpdated(event);
            log.info("Published UserUpdatedEvent for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish UserUpdatedEvent for user: {}. Error: {}", user.getEmail(), e.getMessage());
        }
    }
    
    /**
     * Publish UserDeletedEvent to the message broker.
     */
    private void publishUserDeletedEvent(User user) {
        UserDeletedEvent event = UserDeletedEvent.builder()
                .userId(user.getId().getMostSignificantBits())
                .email(user.getEmail())
                .sfContactId(user.getSfContactId())
                .build();

        try {
            userEventPublisher.publishUserDeleted(event);
            log.info("Published UserDeletedEvent for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish UserDeletedEvent for user: {}. Error: {}", user.getEmail(), e.getMessage());
        }
    }
}
