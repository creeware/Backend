ALTER TABLE repositories
  ADD COLUMN release_date DATE;
ALTER TABLE repositories
  ADD COLUMN due_date DATE;
ALTER TABLE repositories
  ADD COLUMN repository_admin_uuid UUID references users(user_uuid) NOT NULL;
ALTER TABLE repositories
  ADD COLUMN try_count NUMERIC DEFAULT 0;
ALTER TABLE repositories
  ADD COLUMN unlimited BOOLEAN DEFAULT TRUE;
ALTER TABLE repositories
  ADD COLUMN user_name VARCHAR(250);

