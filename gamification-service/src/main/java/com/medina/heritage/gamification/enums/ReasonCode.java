package com.medina.heritage.gamification.enums;

/**
 * Codes de raison pour les transactions de points.
 */
public enum ReasonCode {
    
    // ==================== CREDITS (Gains) ====================
    
    /**
     * Signalement validé par les experts.
     */
    REPORT_VALIDATED("Signalement validé", 100),
    
    /**
     * Premier signalement d'un utilisateur.
     */
    FIRST_REPORT("Premier signalement", 50),
    
    /**
     * Bonus pour signalement avec photos.
     */
    REPORT_WITH_PHOTOS("Signalement avec photos", 20),
    
    /**
     * Bonus de bienvenue à l'inscription.
     */
    WELCOME_BONUS("Bonus de bienvenue", 25),
    
    /**
     * Parrainage d'un nouvel utilisateur.
     */
    REFERRAL_BONUS("Parrainage", 100),
    
    /**
     * Obtention d'un badge.
     */
    BADGE_EARNED("Badge obtenu", 50),
    
    /**
     * Participation à un événement.
     */
    EVENT_PARTICIPATION("Participation événement", 30),
    
    /**
     * Ajustement administratif positif.
     */
    ADMIN_CREDIT("Crédit administratif", 0),
    
    // ==================== DEBITS (Dépenses) ====================
    
    /**
     * Signalement rejeté (pénalité optionnelle).
     */
    REPORT_REJECTED("Signalement rejeté", -10),
    
    /**
     * Échange de points contre une récompense.
     */
    REWARD_REDEMPTION("Échange récompense", 0),
    
    /**
     * Don de points à un autre utilisateur.
     */
    POINTS_DONATION("Don de points", 0),
    
    /**
     * Ajustement administratif négatif.
     */
    ADMIN_DEBIT("Débit administratif", 0),
    
    /**
     * Expiration de points.
     */
    POINTS_EXPIRED("Points expirés", 0);

    private final String description;
    private final int defaultPoints;

    ReasonCode(String description, int defaultPoints) {
        this.description = description;
        this.defaultPoints = defaultPoints;
    }

    public String getDescription() {
        return description;
    }

    public int getDefaultPoints() {
        return defaultPoints;
    }

    /**
     * Vérifie si ce code de raison est un crédit.
     */
    public boolean isCredit() {
        return this.defaultPoints >= 0 && 
               (this == REPORT_VALIDATED || this == FIRST_REPORT || 
                this == REPORT_WITH_PHOTOS || this == WELCOME_BONUS ||
                this == REFERRAL_BONUS || this == BADGE_EARNED || 
                this == EVENT_PARTICIPATION || this == ADMIN_CREDIT);
    }

    /**
     * Vérifie si ce code de raison est un débit.
     */
    public boolean isDebit() {
        return this == REPORT_REJECTED || this == REWARD_REDEMPTION || 
               this == POINTS_DONATION || this == ADMIN_DEBIT || 
               this == POINTS_EXPIRED;
    }
}
