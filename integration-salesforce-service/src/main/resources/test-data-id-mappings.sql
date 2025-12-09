-- Script SQL pour insérer des données de test dans id_mappings
-- À exécuter après avoir synchronisé au moins un utilisateur et un bâtiment avec Salesforce

-- Exemple 1: Mapper un utilisateur vers un Contact Salesforce
INSERT INTO id_mappings (id, local_entity_type, local_entity_id, sf_entity_id, last_sync_at, sync_status)
VALUES (
    gen_random_uuid(),
    'USER',
    '550e8400-e29b-41d4-a716-446655440000', -- UUID de l'utilisateur dans user-auth-service
    '0035g00000ABCDE123',                    -- Contact ID Salesforce (18 caractères)
    CURRENT_TIMESTAMP,
    'SYNCED'
);

-- Exemple 2: Mapper un bâtiment vers un Asset Salesforce
INSERT INTO id_mappings (id, local_entity_type, local_entity_id, sf_entity_id, last_sync_at, sync_status)
VALUES (
    gen_random_uuid(),
    'BUILDING',
    '123e4567-e89b-12d3-a456-426614174000', -- UUID du bâtiment dans patrimoine-service
    '02i5g00000VWXYZ456',                    -- Asset ID Salesforce (18 caractères)
    CURRENT_TIMESTAMP,
    'SYNCED'
);

-- Exemple 3: Vérifier les mappings
SELECT 
    local_entity_type,
    local_entity_id::text as local_id,
    sf_entity_id,
    sync_status,
    last_sync_at
FROM id_mappings
ORDER BY local_entity_type, last_sync_at DESC;

-- Exemple 4: Trouver le Contact Salesforce d'un utilisateur spécifique
SELECT sf_entity_id 
FROM id_mappings 
WHERE local_entity_type = 'USER' 
  AND local_entity_id = '550e8400-e29b-41d4-a716-446655440000';

-- Exemple 5: Trouver l'Asset Salesforce d'un bâtiment spécifique
SELECT sf_entity_id 
FROM id_mappings 
WHERE local_entity_type = 'BUILDING' 
  AND local_entity_id = '123e4567-e89b-41d4-a716-446655440000';

-- Note: Remplacez les UUIDs et IDs Salesforce par vos vraies valeurs
