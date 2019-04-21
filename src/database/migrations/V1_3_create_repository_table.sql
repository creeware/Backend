CREATE TABLE public.repositories(
    repository_uuid uuid primary key,
    user_uuid uuid references users(user_uuid) NOT NULL,
    organization_uuid uuid references organizations(organization_uuid),
    repository_name VARCHAR(500) NOT NULL,
    repository_description VARCHAR(500),
    repository_visibility VARCHAR(250),
    repository_git_url VARCHAR(2000) NOT NULL,
    repository_github_type VARCHAR(250) NOT NULL,
    repository_type VARCHAR(250) NOT NULL DEFAULT 'challenge template',
    repository_status VARCHAR(250) NOT NULL DEFAULT 'unprocessed',
    repository_submission_date date NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);