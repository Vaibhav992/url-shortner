CREATE TABLE short_urls (
    id           BIGSERIAL PRIMARY KEY,
    alias        VARCHAR(64)  NOT NULL,
    original_url TEXT         NOT NULL,
    access_count BIGINT       NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_short_urls_alias UNIQUE (alias)
);

CREATE INDEX idx_short_urls_expires_at ON short_urls (expires_at);
