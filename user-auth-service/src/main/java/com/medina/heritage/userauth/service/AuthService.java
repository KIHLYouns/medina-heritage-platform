package com.medina.heritage.userauth.service;

import com.medina.heritage.userauth.dto.request.ChangePasswordRequest;
import com.medina.heritage.userauth.dto.request.LoginRequest;
import com.medina.heritage.userauth.dto.request.RegisterRequest;
import com.medina.heritage.userauth.dto.response.AuthResponse;
import com.medina.heritage.userauth.entity.Role;
import com.medina.heritage.userauth.entity.User;
import com.medina.heritage.userauth.enums.RoleName;
import com.medina.heritage.userauth.event.UserCreatedEvent;
import com.medina.heritage.userauth.exception.BadRequestException;
import com.medina.heritage.userauth.exception.ResourceNotFoundException;
import com.medina.heritage.userauth.integration.GamificationServiceClient;
import com.medina.heritage.userauth.integration.SalesforceServiceClient;
import com.medina.heritage.userauth.mapper.UserMapper;
import com.medina.heritage.userauth.repository.RoleRepository;
import com.medina.heritage.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SalesforceServiceClient salesforceServiceClient;
    private final GamificationServiceClient gamificationServiceClient;
    private final UserMapper userMapper;
    private final PasswordService passwordService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Get default role (CITIZEN) - utilise l'enum RoleName
        Role citizenRole = roleRepository.findByName(RoleName.CITIZEN.name())
                .orElseGet(() -> roleRepository.save(new Role(RoleName.CITIZEN.name())));

        // Create new user via mapper
        User user = userMapper.toUser(request);
        // Hash le mot de passe avec BCrypt
        user.setPasswordHash(passwordService.hashPassword(request.getPassword()));
        user.setRoles(Set.of(citizenRole));

        User savedUser = userRepository.save(user);

        // Notify external services (async - non-blocking)
        notifyExternalServices(savedUser);

        AuthResponse response = new AuthResponse();
        response.setUser(userMapper.toUserResponse(savedUser));
        response.setMessage("User registered successfully");
        return response;
    }

    /**
     * Notifie les services externes de la création d'un utilisateur.
     */
    private void notifyExternalServices(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        // Notifier Salesforce
        UserCreatedEvent event = UserCreatedEvent.fromUser(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                roleNames,
                user.getCreatedAt()
        );
        salesforceServiceClient.notifyUserCreated(event);

        // Créer un wallet dans le service de gamification
        gamificationServiceClient.createUserWallet(user.getId(), user.getEmail());
        
        // Ajouter des points de bienvenue
        gamificationServiceClient.addWelcomePoints(user.getId(), 100);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // Vérification du mot de passe avec BCrypt
        if (!passwordService.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new BadRequestException("Account is deactivated");
        }

        AuthResponse response = new AuthResponse();
        response.setUser(userMapper.toUserResponse(user));
        response.setMessage("Login successful");
        return response;
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Vérifier le mot de passe actuel avec BCrypt
        if (!passwordService.verifyPassword(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Hasher le nouveau mot de passe
        user.setPasswordHash(passwordService.hashPassword(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Gère la déconnexion de l'utilisateur.
     * Sans JWT, le logout est principalement géré côté client.
     * Ce endpoint peut être utilisé pour :
     * - Logger l'événement de déconnexion
     * - Invalider des sessions si implémenté plus tard
     * - Déclencher des événements (analytics, etc.)
     */
    public void logout(UUID userId) {
        if (userId != null) {
            // Vérifier que l'utilisateur existe (optionnel)
            userRepository.findById(userId).ifPresent(user -> {
                // Ici on pourrait ajouter des actions comme:
                // - Enregistrer l'heure de dernière déconnexion
                // - Logger l'événement
                // - Publier un événement pour analytics
                // Pour l'instant, on log simplement
                System.out.println("User logged out: " + user.getEmail());
            });
        }
    }
}
