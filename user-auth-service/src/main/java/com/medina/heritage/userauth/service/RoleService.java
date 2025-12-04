package com.medina.heritage.userauth.service;

import com.medina.heritage.userauth.entity.Role;
import com.medina.heritage.userauth.enums.RoleName;
import com.medina.heritage.userauth.exception.BadRequestException;
import com.medina.heritage.userauth.exception.ResourceNotFoundException;
import com.medina.heritage.userauth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));
    }

    /**
     * Récupère un rôle par son enum RoleName.
     */
    public Role getRoleByName(RoleName roleName) {
        return getRoleByName(roleName.name());
    }

    @Transactional
    public Role createRole(String name) {
        if (roleRepository.existsByName(name)) {
            throw new BadRequestException("Role already exists: " + name);
        }
        return roleRepository.save(new Role(name));
    }

    /**
     * Crée un rôle à partir de l'enum RoleName.
     */
    @Transactional
    public Role createRole(RoleName roleName) {
        return createRole(roleName.name());
    }

    /**
     * Vérifie si un nom de rôle est valide (existe dans l'enum).
     */
    public boolean isValidRoleName(String name) {
        try {
            RoleName.valueOf(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
