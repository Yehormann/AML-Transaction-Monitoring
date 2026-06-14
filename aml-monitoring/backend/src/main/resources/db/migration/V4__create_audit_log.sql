CREATE TABLE audit_log (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type  VARCHAR(32)  NOT NULL,
    entity_id    UUID         NOT NULL,
    action       VARCHAR(32)  NOT NULL,
    performed_by VARCHAR(64)  NOT NULL,
    note         TEXT,
    timestamp    TIMESTAMP    NOT NULL DEFAULT now()
);
