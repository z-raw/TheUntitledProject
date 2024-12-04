CREATE DATABASE chirpster;
\c chirpster;

CREATE TABLE IF NOT EXISTS chirps (
  id uuid DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  content text NOT NULL,
  created_at bigint NOT NULL
);

ALTER TABLE chirps
ADD CONSTRAINT pk_chirps PRIMARY KEY (id) ;