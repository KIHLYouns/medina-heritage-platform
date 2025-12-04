-- Service: PatrimoineService
-- 1. Activer les extensions nécessaires (UUID et Géographie)

CREATE EXTENSION IF NOT EXISTS "postgis";

-- 2. Créer la table des bâtiments (Monuments)
CREATE TABLE IF NOT EXISTS buildings (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    geom GEOMETRY(Point, 4326),
    sf_asset_id VARCHAR(18),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Créer les index pour la performance (Géographie et Salesforce)
CREATE INDEX IF NOT EXISTS idx_buildings_geom ON buildings USING GIST (geom);
CREATE INDEX IF NOT EXISTS idx_buildings_sf_id ON buildings(sf_asset_id);

-- 4. Créer la table des QR Codes liée aux bâtiments
CREATE TABLE IF NOT EXISTS qr_tags (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    qr_content VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    installed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
