CREATE TABLE IF NOT EXISTS chirps (
  id uuid DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  content text NOT NULL,
  created_at bigint NOT NULL
);

ALTER TABLE chirps
ADD CONSTRAINT pk_chirps PRIMARY KEY (id) ;

INSERT INTO chirps(
    id,
    user_id,
    content,
    created_at
) VALUES (
    '30b8119b-7893-4436-9504-8f1342971a42',--id,
    '00000000-0000-0000-0000-000000000001',--user_id
    'This is a valid chirp',--content
    42 --created_at
);