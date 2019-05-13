ALTER TABLE repositories
  ADD COLUMN template_repository_name VARCHAR(250);
ALTER TABLE repositories
  ADD COLUMN organization_name VARCHAR(250);