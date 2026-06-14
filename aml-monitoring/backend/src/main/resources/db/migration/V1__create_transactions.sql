CREATE TABLE transactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_account      VARCHAR(64)    NOT NULL,
    sender_country      VARCHAR(2)     NOT NULL,
    receiver_account    VARCHAR(64)    NOT NULL,
    receiver_country    VARCHAR(2)     NOT NULL,
    receiver_last_active DATE          NOT NULL,
    amount              NUMERIC(19, 2) NOT NULL,
    currency            VARCHAR(3)     NOT NULL DEFAULT 'EUR',
    timestamp           TIMESTAMP      NOT NULL,
    risk_score          INTEGER        NOT NULL DEFAULT 0,
    status              VARCHAR(16)    NOT NULL DEFAULT 'APPROVED',
    fired_rules         JSONB          NOT NULL DEFAULT '[]',
    created_at          TIMESTAMP      NOT NULL DEFAULT now()
);
