CREATE TABLE IF NOT EXISTS url_mappings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  short_key VARCHAR(16)  NOT NULL,
  long_url  VARCHAR(2048) NOT NULL,
  created_at BIGINT       NOT NULL,
  CONSTRAINT uk_short_key UNIQUE (short_key),
  CONSTRAINT uk_long_url  UNIQUE (long_url)
);

CREATE INDEX IF NOT EXISTS idx_created_at ON url_mappings(created_at);
