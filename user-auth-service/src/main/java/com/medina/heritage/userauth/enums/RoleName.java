package com.medina.heritage.userauth.enums;

/**
 * Enum représentant les rôles prédéfinis du système.
 * 
 * Utilisation simple : RoleName.CITIZEN.name() retourne "CITIZEN"
 * 
 * Les enums Java ont automatiquement:
 * - name() : retourne le nom exact de la constante (ex: "CITIZEN")
 * - toString() : par défaut identique à name()
 * - valueOf("CITIZEN") : retourne l'enum correspondant
 */
public enum RoleName {
    CITIZEN,
    TECHNICIAN_LOCAL,
    TECHNICIAN_NATIONAL,
    ADMIN
}

