package com.medina.heritage.patrimoine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medina.heritage.patrimoine.entities.Building;

import java.util.Optional;
import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {
    Optional<Building> findByCode(String code);
}