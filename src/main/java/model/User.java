package model;

import lombok.Data;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

import javax.persistence.*;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    UUID user_uuid;

    String user_display_name;
    String username;
    String user_email;
    String user_client;
    String avatar_url;
    String profile_url;
    String user_role;
    String user_location;
    String access_token;
    String jwt_token;
    String user_bio;
    Date created_at;
    Date updated_at;

    public static User getUser(String username, String clientName){
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = new User();
        try {
             user = session.createQuery("from User where username=:username AND user_client=:user_client", User.class)
                        .setParameter("username", username)
                        .setParameter("user_client", clientName)
                        .uniqueResult();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            return user;
        }
    }

    public static User getUser(String jws){
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = new User();
        try {
            user = session.createQuery("from User where jwt_token=:jwt_token", User.class)
                    .setParameter("jwt_token", jws)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            return user;
        }
    }

    public UUID createGithubUser(org.eclipse.egit.github.core.User githubUser, String access_token, String jwt_token){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            if (getUser(githubUser.getLogin(), "GitHubClient") == null){
                User user = new User();
                user.setUser_display_name(githubUser.getName());
                user.setUsername(githubUser.getLogin());
                user.setUser_email(githubUser.getEmail());
                user.setUser_client("GitHubClient");
                user.setAvatar_url(githubUser.getAvatarUrl());
                user.setProfile_url(githubUser.getUrl());
                user.setUser_role("user");
                user.setUser_location(githubUser.getLocation());
                user.setAccess_token(access_token);
                user.setJwt_token(jwt_token);
                user.setCreated_at(new Date());
                user.setUser_uuid(UUID.randomUUID());

                Transaction transaction = session.beginTransaction();
                session.persist(user);
                session.flush();
                transaction.commit();
                // WelcomeEmail.main(profile.getEmail());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return null;
    }
}
