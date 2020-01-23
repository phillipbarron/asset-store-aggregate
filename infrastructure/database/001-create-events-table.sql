CREATE TABLE IF NOT EXISTS events(
  event_type VARCHAR(40) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  event_created TIMESTAMP NOT NULL,
  event_id uuid NOT NULL PRIMARY KEY,
  asset_id VARCHAR(20) NOT NULL,
  major_version INT NOT NULL,
  minor_version INT NOT NULL
);

CREATE INDEX CONCURRENTLY asset_id_index
ON events (asset_id);
