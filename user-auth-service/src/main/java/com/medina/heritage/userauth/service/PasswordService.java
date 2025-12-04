package com.medina.heritage.userauth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service dédié au hachage et à la vérification des mots de passe.
 * Utilise BCrypt pour un hachage sécurisé.
 * 
 * BCrypt :
 * - Génère automatiquement un salt unique pour chaque hash
 * - Le salt est inclus dans le hash résultant
 * - Résistant aux attaques par tables arc-en-ciel
 * - Configurable en termes de force (strength = nombre d'itérations)
 */
@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService() {
        // Strength 12 = 2^12 = 4096 itérations (bon équilibre sécurité/performance)
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }

    /**
     * Hash un mot de passe en clair avec BCrypt.
     * 
     * @param rawPassword le mot de passe en clair
     * @return le hash BCrypt du mot de passe
     */
    public String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Vérifie si un mot de passe en clair correspond à un hash.
     * 
     * @param rawPassword le mot de passe en clair à vérifier
     * @param encodedPassword le hash stocké en base de données
     * @return true si le mot de passe correspond, false sinon
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Vérifie si un hash doit être re-hashé (par exemple si la force a changé).
     * Utile pour les migrations progressives.
     * 
     * @param encodedPassword le hash actuel
     * @return true si le hash devrait être mis à jour
     */
    public boolean needsRehash(String encodedPassword) {
        if (encodedPassword == null) {
            return true;
        }
        // BCryptPasswordEncoder peut vérifier si le hash utilise l'ancienne force
        return ((BCryptPasswordEncoder) passwordEncoder).upgradeEncoding(encodedPassword);
    }
}
