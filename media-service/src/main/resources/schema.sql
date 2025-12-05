-- Media Service Schema
-- Table pour les fichiers médias

CREATE TABLE IF NOT EXISTS media_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID,
    bucket_name VARCHAR(255) NOT NULL,
    file_key VARCHAR(500) NOT NULL UNIQUE,
    public_url VARCHAR(1000) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    media_type VARCHAR(20) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    -- Contraintes
    CONSTRAINT chk_entity_type CHECK (entity_type IN ('REPORT', 'BUILDING', 'USER_AVATAR', 'INSPECTION')),
    CONSTRAINT chk_media_type CHECK (media_type IN ('IMAGE', 'VIDEO', 'DOCUMENT')),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'DELETED')),
    CONSTRAINT chk_file_size CHECK (file_size_bytes > 0)
);

-- Index pour les recherches fréquentes
CREATE INDEX IF NOT EXISTS idx_media_files_user_id ON media_files(user_id);
CREATE INDEX IF NOT EXISTS idx_media_files_entity ON media_files(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_media_files_status ON media_files(status);
CREATE INDEX IF NOT EXISTS idx_media_files_created_at ON media_files(created_at DESC);

-- Index composite pour les requêtes courantes
CREATE INDEX IF NOT EXISTS idx_media_files_user_status ON media_files(user_id, status);
CREATE INDEX IF NOT EXISTS idx_media_files_entity_status ON media_files(entity_type, entity_id, status);
