package com.medina.heritage.userauth.messaging;

import com.medina.heritage.events.user.UserCreatedEvent;
import com.medina.heritage.events.user.UserCreatedByClerkEvent;
import com.medina.heritage.userauth.entity.Role;
import com.medina.heritage.userauth.entity.User;
import com.medina.heritage.userauth.enums.RoleName;
import com.medina.heritage.userauth.repository.RoleRepository;
import com.medina.heritage.userauth.repository.UserRepository;
import com.medina.heritage.userauth.service.PasswordService; // Tu peux supprimer cet import si tu enlèves la génération de mot de passe
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserAuthEventConsumer {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  // private final PasswordService passwordService; // Plus besoin de ça pour les
  // users Clerk
  private final UserEventPublisher userEventPublisher;

  @Bean("userCreatedByClerkConsumer")
  public Consumer<UserCreatedByClerkEvent> userCreatedByClerkConsumer() {
    return this::processUserCreated;
  }

  @Transactional
  public void processUserCreated(UserCreatedByClerkEvent event) {
    log.info("🚀 [CONSUMER] Received UserCreatedByClerkEvent for Clerk user: {}", event.getClerkUserId());

    if (userRepository.existsByEmail(event.getEmail())) {
      log.warn("User already exists with email: {}", event.getEmail());
      return;
    }

    log.info("🚀 [CONSUMER] Creating user with email: {}", event.getEmail());

    Role citizenRole = roleRepository.findByName(RoleName.CITIZEN.name())
        .orElseGet(() -> {
          log.info("🚀 [CONSUMER] Creating CITIZEN role");
          Role newRole = new Role();
          newRole.setName(RoleName.CITIZEN.name());
          return roleRepository.save(newRole);
        });

    User user = new User();
    user.setClerkId(event.getClerkUserId());
    user.setEmail(event.getEmail());
    user.setFirstName(event.getFirstName());
    user.setLastName(event.getLastName());
    user.setPhoneNumber(event.getPhoneNumber());
    user.setIsActive(true);
    user.setRoles(Set.of(citizenRole));

    User savedUser = userRepository.save(user);

    log.info("✅ [CONSUMER] User saved in DB. Internal UUID: {} | Clerk ID: {}", savedUser.getId(),
        savedUser.getClerkId());

    Set<String> roles = savedUser.getRoles().stream()
        .map(Role::getName)
        .collect(Collectors.toSet());

    UserCreatedEvent userCreatedEvent = UserCreatedEvent.builder()
        .userId(savedUser.getId().toString()) 
        .email(savedUser.getEmail())
        .firstName(savedUser.getFirstName())
        .lastName(savedUser.getLastName())
        .phoneNumber(savedUser.getPhoneNumber())
        .roles(roles)
        .build();

    userCreatedEvent.initializeDefaults();

    log.info("🚀 [CONSUMER] Publishing UserCreatedEvent to Salesforce (userId: {})", userCreatedEvent.getUserId());
    userEventPublisher.publishUserCreated(userCreatedEvent);
    log.info("✅ [CONSUMER] Event published successfully");
  }
}