package com.medina.heritage.userauth.controller;

import com.medina.heritage.userauth.dto.response.ApiResponse;
import com.medina.heritage.userauth.entity.Role;
import com.medina.heritage.userauth.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Role>> createRole(@RequestBody String name) {
        Role role = roleService.createRole(name);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", role));
    }
}
