CREATE TABLE public.users
  (
      user_uuid UUID primary key,
      user_display_name VARCHAR(250),
      username VARCHAR(250) NOT NULL,
      user_email VARCHAR(250) NOT NULL,
      user_client VARCHAR(250) NOT NULL,
      avatar_url VARCHAR(500),
      profile_url VARCHAR(500) NOT NULL,
      user_role VARCHAR(20),
      user_location VARCHAR(250),
      created_at TIMESTAMPTZ NOT NULL,
      updated_at TIMESTAMPTZ
  );

CREATE INDEX index_on_username
ON users (username ASC);

CREATE INDEX index_on_user_client
ON users (user_client);