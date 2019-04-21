CREATE TABLE public.users_organizations(
    user_uuid uuid references users(user_uuid) NOT NULL,
    organization_uuid uuid references organizations(organization_uuid) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,

    CONSTRAINT users_organizations_pkey PRIMARY KEY (user_uuid, organization_uuid)
);