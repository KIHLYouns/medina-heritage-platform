-- Service: MediaService


CREATE TABLE IF NOT EXISTS media_files (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL, -- Auteur (UserAuthService)
    storage_provider VARCHAR(20) DEFAULT 'S3',
    bucket_name VARCHAR(100) NOT NULL,
    file_key VARCHAR(255) NOT NULL, -- Chemin dans le bucket
    public_url TEXT NOT NULL, -- URL accessible par Salesforce Einstein
    mime_type VARCHAR(50),
    file_size_bytes BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
