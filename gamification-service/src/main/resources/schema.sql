-- ============================================================
-- Gamification Service - Schema SQL
-- ============================================================
-- Ce service gère le système de points et de récompenses.
-- Les wallets sont liés aux utilisateurs via l'ID de UserAuthService.
-- ============================================================

-- Table des portefeuilles utilisateurs
CREATE TABLE IF NOT EXISTS wallets (
    user_id UUID PRIMARY KEY,                                    -- ID de l'utilisateur (correspond à UserAuthService)
    balance INTEGER NOT NULL DEFAULT 0,                          -- Solde actuel de points
    level INTEGER NOT NULL DEFAULT 1,                            -- Niveau actuel
    total_earned INTEGER NOT NULL DEFAULT 0,                     -- Total des points gagnés
    total_spent INTEGER NOT NULL DEFAULT 0,                      -- Total des points dépensés
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table des transactions de points
CREATE TABLE IF NOT EXISTS point_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,                                       -- ID de l'utilisateur
    points INTEGER NOT NULL,                                     -- Montant de la transaction
    transaction_type VARCHAR(10) NOT NULL,                       -- 'CREDIT' ou 'DEBIT'
    reason_code VARCHAR(50) NOT NULL,                            -- Code de raison (ex: 'REPORT_VALIDATED')
    description VARCHAR(500),                                    -- Description lisible
    reference_id UUID,                                           -- ID de l'entité liée (optionnel)
    reference_type VARCHAR(50),                                  -- Type d'entité liée (ex: 'REPORT')
    sf_case_id VARCHAR(18),                                      -- ID Salesforce associé (optionnel)
    balance_after INTEGER NOT NULL,                              -- Solde après la transaction
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('CREDIT', 'DEBIT')),
    CONSTRAINT chk_points_positive CHECK (points >= 0)
);

-- Index pour améliorer les performances des requêtes fréquentes
CREATE INDEX IF NOT EXISTS idx_point_transactions_user_id ON point_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_point_transactions_created_at ON point_transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_point_transactions_reason_code ON point_transactions(reason_code);
CREATE INDEX IF NOT EXISTS idx_point_transactions_reference ON point_transactions(reference_id, reference_type);

-- Index sur wallets pour le classement
CREATE INDEX IF NOT EXISTS idx_wallets_total_earned ON wallets(total_earned DESC);
CREATE INDEX IF NOT EXISTS idx_wallets_level ON wallets(level DESC);
