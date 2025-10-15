CREATE TABLE IF NOT EXISTS url_mappings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  short_key VARCHAR(16)  NOT NULL,
  long_url  VARCHAR(2048) NOT NULL,
  created_at BIGINT       NOT NULL,

  url_hash VARBINARY(32) GENERATED ALWAYS AS (UNHEX(SHA2(COALESCE(long_url, ''), 256))) STORED,

  CONSTRAINT uk_short_key UNIQUE (short_key),

  CONSTRAINT uk_url_hash UNIQUE (url_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

CREATE INDEX idx_created_at ON url_mappings(created_at);
CREATE INDEX idx_url_hash ON url_mappings(url_hash);
