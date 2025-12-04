-- Active: 1764793469137@@127.0.0.1@5432@heritage_db
-- Service: UserAuthService

-- Suppression des tables existantes pour réinitialisation (optionnel en dev)
-- DROP TABLE IF EXISTS user_roles CASCADE;
-- DROP TABLE IF EXISTS users CASCADE;
-- DROP TABLE IF EXISTS roles CASCADE;

CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL -- ex: 'CITIZEN', 'TECHNICIAN_LOCAL'
);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    sf_contact_id VARCHAR(18), -- ID du Contact dans Salesforce (pour sync)
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Insertion des rôles par défaut
INSERT INTO roles (name) VALUES ('CITIZEN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('TECHNICIAN_LOCAL') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('TECHNICIAN_NATIONAL') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;
