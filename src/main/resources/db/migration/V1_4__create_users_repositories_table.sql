CREATE TABLE public.users_repositories(
    user_uuid uuid references users(user_uuid) NOT NULL,
    repository_uuid uuid references repositories(repository_uuid) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,

    CONSTRAINT users_repositories_pkey PRIMARY KEY (user_uuid, repository_uuid)
);