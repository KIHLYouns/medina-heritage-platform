
CREATE DATABASE media_db;
CREATE DATABASE user_db;
CREATE DATABASE patrimoine_db;
CREATE DATABASE integration_salesforce_db;
CREATE DATABASE gamification_db;
CREATE DATABASE iot_db;
CREATE DATABASE notification_db;


\c patrimoine_db;

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS buildings (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    geom GEOMETRY(Point, 4326),
    sf_asset_id VARCHAR(18),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    image_url TEXT,
    description TEXT
);

CREATE TABLE IF NOT EXISTS qr_tags (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    qr_content VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    installed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO buildings (id, code, name, address, geom, created_at, updated_at, image_url, description)
VALUES (
    'a8fa33c8-eb76-4359-80ed-4a68b9733994'::uuid,
    'PAT-001',
    'Bab Oqla - Oqla Gate',
    'Bab Oqla, Medina of Tetouan, Tetouan 93000, Morocco',
    ST_GeomFromText('POINT(-5.3639 35.5697)', 4326),
    NOW(),
    NOW(),
    'https://medina-heritage-media.s3.us-east-1.amazonaws.com/buildings/2025/12/54403123-screen_shot_2025-12-10_at_22.03.41.png',
    'Historic gate of the Medina of Tetouan, also known as Bab al-Aqla or the Gate of Ugliness. Built during the Moroccan period...'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO qr_tags (id, building_id, qr_content, status, installed_at)
VALUES (
    gen_random_uuid(),
    'a8fa33c8-eb76-4359-80ed-4a68b9733994'::uuid,
    'PAT-001',
    'ACTIVE',
    NOW()
) ON CONFLICT (qr_content) DO NOTHING;

\c user_db;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    sf_contact_id VARCHAR(18),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    clerk_id VARCHAR(255)
);

INSERT INTO users (id, clerk_id, email, first_name, last_name, is_active, created_at, updated_at)
VALUES (
    'a05798fe-70a3-4966-a5c2-bdb6ca357ece'::uuid,
    'user_37SgxerKKktLY8dTCsN91J1fwMK',
    'nounstouns2@gmail.com',
    'Citoyen',
    'Tétouan',
    true,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

\c integration_salesforce_db;

CREATE TABLE IF NOT EXISTS id_mappings (
    id UUID PRIMARY KEY,
    local_entity_type VARCHAR(50) NOT NULL,
    local_entity_id UUID NOT NULL,
    sf_entity_id VARCHAR(18) NOT NULL,
    last_sync_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    sync_status VARCHAR(20) DEFAULT 'SYNCED'
);

INSERT INTO id_mappings (id, last_sync_at, local_entity_id, local_entity_type, sf_entity_id, sync_status)
VALUES (
    gen_random_uuid(),
    NOW(),
    'a8fa33c8-eb76-4359-80ed-4a68b9733994'::uuid,
    'BUILDING',
    '02ig500000017p3AAA',
    'SYNCED'
),
(
    gen_random_uuid(),
    NOW(),
    'a05798fe-70a3-4966-a5c2-bdb6ca357ece'::uuid,
    'USER',
    '003g50000025mWEAAY',
    'SYNCED'
) ON CONFLICT (id) DO NOTHING;