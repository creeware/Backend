package sql2omodel;

import model.*;
import util.RandomUuidGenerator;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Sql2oModel implements Model {

    private Sql2o sql2o;
    private RandomUuidGenerator uuidGenerator;

    public Sql2oModel(Sql2o sql2o) {
        this.sql2o = sql2o;
        uuidGenerator = new RandomUuidGenerator();
    }

    @Override
    public UUID createUser(String user_display_name,
                           String user_name,
                           String avatar_url,
                           String profile_url,
                           String user_role,
                           String user_location) {
        try (Connection conn = sql2o.beginTransaction()) {
            UUID userUuid = uuidGenerator.generate();
            conn.createQuery("insert into users(user_uuid, user_display_name, user_name, avatar_url, profile_url, user_role, user_location, created_at) " +
                    "VALUES (:user_uuid, :user_display_name, :user_name, :avatar_url, :profile_url, :user_role, :user_location, :created_at)")
                    .addParameter("user_uuid", userUuid)
                    .addParameter("user_display_name", user_display_name)
                    .addParameter("user_name", user_name)
                    .addParameter("avatar_url", avatar_url)
                    .addParameter("profile_url", profile_url)
                    .addParameter("user_role", user_role)
                    .addParameter("user_location", user_location)
                    .addParameter("created_at", new Date())
                    .executeUpdate();
            conn.commit();
            return userUuid;
        }
    }

    @Override
    public List<User> getAllUsers() {
        try (Connection conn = sql2o.open()) {
            List<User> users = conn.createQuery("select * from users")
                    .executeAndFetch(User.class);
            return users;
        }
    }


    @Override
    public boolean existUser(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            List<User> users = conn.createQuery("select * from users where user_uuid=:user_uuid")
                    .addParameter("user_uuid", uuid)
                    .executeAndFetch(User.class);
            return users.size() > 0;
        }
    }

    @Override
    public boolean existUserByName(String displayName) {
        try (Connection conn = sql2o.open()) {
            List<User> users = conn.createQuery("select * from users where user_display_name=:user_display_name")
                    .addParameter("user_display_name", displayName)
                    .executeAndFetch(User.class);
            return users.size() > 0;
        }
    }

    @Override
    public Optional<User> getUser(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            List<User> users = conn.createQuery("select * from user where user_uuid=:user_uuid")
                    .addParameter("user_uuid", uuid)
                    .executeAndFetch(User.class);
            if (users.size() == 0) {
                return Optional.empty();
            } else if (users.size() == 1) {
                return Optional.of(users.get(0));
            } else {
                throw new RuntimeException();
            }
        }
    }

    @Override
    public void updateUser(User user) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("update users set user_display_name=:user_display_name, " +
                    "user_name=:user_name, avatar_url=:avatar_url, profile_url=:profile_url, user_role=:user_role, user_location=:user_location where user_uuid=:user_uuid")
                    .addParameter("user_uuid", user.getUser_uuid())
                    .addParameter("user_display_name", user.getUser_display_name())
                    .addParameter("user_name", user.getUser_name())
                    .addParameter("user_email", user.getAvatar_url())
                    .addParameter("profile_url", user.getProfile_url())
                    .addParameter("user_role", user.getUser_role())
                    .addParameter("user_location", user.getUser_location())
                    .executeUpdate();
        }
    }

    @Override
    public void deleteUser(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("delete from users where user_uuid=:user_uuid")
                    .addParameter("user_uuid", uuid)
                    .executeUpdate();
        }
    }

}
