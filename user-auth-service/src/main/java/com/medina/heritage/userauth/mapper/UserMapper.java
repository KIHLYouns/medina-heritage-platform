package com.medina.heritage.userauth.mapper;

import com.medina.heritage.userauth.dto.request.RegisterRequest;
import com.medina.heritage.userauth.dto.response.UserResponse;
import com.medina.heritage.userauth.entity.Role;
import com.medina.heritage.userauth.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper manuel pour convertir entre User Entity et DTOs.
 * Alternative à MapStruct pour garder les choses simples.
 */
@Component
public class UserMapper {

    /**
     * Convertit une entité User vers un UserResponse DTO.
     */
    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        // Convertir les rôles en Set de noms
        if (user.getRoles() != null) {
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            response.setRoles(roleNames);
        }

        return response;
    }

    /**
     * Convertit un RegisterRequest vers une entité User.
     * Note: Le password doit être hashé séparément.
     */
    public User toUser(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        // passwordHash doit être défini séparément après hashage
        return user;
    }

    /**
     * Met à jour une entité User existante avec les données du RegisterRequest.
     */
    public void updateUserFromRequest(User user, RegisterRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
    }
}
