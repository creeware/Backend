CREATE TABLE public.users
  (
      user_uuid UUID primary key,
      user_display_name VARCHAR(250) NOT NULL UNIQUE,
      user_name VARCHAR(250) NOT NULL,
      avatar_url VARCHAR(500),
      profile_url VARCHAR(500) NOT NULL,
      user_role VARCHAR(20),
      user_location VARCHAR(250),
      created_at TIMESTAMPTZ NOT NULL,
      updated_at TIMESTAMPTZ
  );