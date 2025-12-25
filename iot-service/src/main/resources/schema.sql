-- Service: IoTService

CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY,
    serial_number VARCHAR(100) UNIQUE NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'VIBRATION', 'HUMIDITY', 'CRACK_MONITOR'
    building_id UUID NOT NULL, -- Référence logique vers PatrimoineService
    sf_asset_id VARCHAR(18), -- Lien Salesforce pour remonter l'alerte au bon endroit
    status VARCHAR(20) DEFAULT 'ONLINE',
    last_seen_at TIMESTAMP
    WITH
        TIME ZONE
);

CREATE TABLE IF NOT EXISTS risk_rules (
    id SERIAL PRIMARY KEY,
    metric_type VARCHAR(50) NOT NULL, -- ex: 'VIBRATION_LEVEL'
    threshold_min DECIMAL(10, 2),
    threshold_max DECIMAL(10, 2),
    severity_level VARCHAR(20) NOT NULL, -- 'WARNING', 'CRITICAL'
    description VARCHAR(255)
);

-- Table volumineuse (Timeseries)
CREATE TABLE IF NOT EXISTS measurements (
    time TIMESTAMP
    WITH
        TIME ZONE NOT NULL,
        device_id UUID NOT NULL,
        value DECIMAL(10, 4) NOT NULL,
        unit VARCHAR(20) NOT NULL
);