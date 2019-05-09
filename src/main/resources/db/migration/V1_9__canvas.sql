ALTER TABLE repositories
  ADD COLUMN canvas_course_uuid VARCHAR(250);
ALTER TABLE repositories
  ADD COLUMN canvas_assignment_uuid VARCHAR(250);
ALTER TABLE repositories
  ADD COLUMN canvas_student_uuid VARCHAR(250);
ALTER TABLE repositories
  ADD COLUMN canvas_base_url VARCHAR(2000);

ALTER TABLE users ALTER COLUMN username DROP NOT NULL;
ALTER TABLE users ALTER COLUMN user_email DROP NOT NULL;
ALTER TABLE users ALTER COLUMN user_client DROP NOT NULL;
ALTER TABLE users ALTER COLUMN profile_url DROP NOT NULL;
ALTER TABLE users ALTER COLUMN access_token DROP NOT NULL;

ALTER TABLE users
  ADD user_status VARCHAR(100);
ALTER TABLE users
  ADD COLUMN canvas_access_token VARCHAR(2000);
ALTER TABLE users
  ADD COLUMN canvas_base_url VARCHAR(2000);
ALTER TABLE users
  ADD COLUMN canvas_user_uuid VARCHAR(250);




