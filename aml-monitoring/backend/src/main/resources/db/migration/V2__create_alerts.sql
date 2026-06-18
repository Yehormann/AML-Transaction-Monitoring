CREATE TABLE alerts (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id       UUID        NOT NULL REFERENCES transactions(id),
    risk_score_snapshot  INTEGER     NOT NULL,
    status               VARCHAR(16) NOT NULL DEFAULT 'OPEN',
    analyst_note         TEXT,
    created_at           TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP   NOT NULL DEFAULT now()
);
