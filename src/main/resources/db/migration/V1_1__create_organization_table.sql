CREATE TABLE public.organizations(
    organization_uuid uuid primary key,
    user_uuid uuid references users(user_uuid) NOT NULL,
    organization_name VARCHAR(500) NOT NULL,
    company_name VARCHAR(500),
    organization_description VARCHAR(500),
    repository_count NUMERIC DEFAULT 0,
    organization_git_url VARCHAR(2000),
    organization_github_type VARCHAR(250),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);