-- Service: IntegrationSalesforceService


CREATE TABLE IF NOT EXISTS id_mappings (
    id UUID PRIMARY KEY,
    local_entity_type VARCHAR(50) NOT NULL, -- 'USER', 'BUILDING', 'IOT_ALERT'
    local_entity_id UUID NOT NULL,
    sf_entity_id VARCHAR(18) NOT NULL, -- ID Salesforce (CaseId, ContactId...)
    last_sync_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    sync_status VARCHAR(20) DEFAULT 'SYNCED'
);

CREATE TABLE IF NOT EXISTS webhook_logs (
    id UUID PRIMARY KEY,
    sf_event_type VARCHAR(100), -- ex: 'Case.StatusChanged'
    payload JSONB, -- Le contenu brut envoyé par Salesforce
    processed BOOLEAN DEFAULT FALSE,
    received_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
