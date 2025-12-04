-- Service: NotificationService


CREATE TABLE IF NOT EXISTS notification_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL, -- Destinataire
    type VARCHAR(50) NOT NULL, -- 'PUSH', 'EMAIL', 'SMS'
    title VARCHAR(255),
    body TEXT,
    status VARCHAR(20) DEFAULT 'PENDING', -- 'SENT', 'FAILED'
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT
);
