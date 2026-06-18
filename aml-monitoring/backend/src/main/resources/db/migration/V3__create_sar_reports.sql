CREATE TABLE sar_reports (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_id   UUID      NOT NULL REFERENCES alerts(id),
    pdf_path   TEXT      NOT NULL,
    filed_at   TIMESTAMP NOT NULL DEFAULT now(),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
