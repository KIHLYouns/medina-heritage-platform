-- Service: GamificationService


CREATE TABLE IF NOT EXISTS wallets (
    user_id UUID PRIMARY KEY, -- Même ID que UserAuthService
    balance INTEGER DEFAULT 0,
    level INTEGER DEFAULT 1,
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS point_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    points_delta INTEGER NOT NULL, -- peut être positif ou négatif
    reason_code VARCHAR(50) NOT NULL, -- 'REPORT_VALIDATED', 'REPORT_REJECTED'
    sf_case_id VARCHAR(18), -- Lien vers le ticket Salesforce qui a généré les points
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
