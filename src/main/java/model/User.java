package model;

import lombok.Data;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import util.HibernateUtil;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@FilterDef(
        name = "user_display_name",
        parameters = @ParamDef(name = "user_display_name", type = "string")
)
@Filter(
        name = "displayNameFilter",
        condition = "user_display_name LIKE :user_display_name"
)
@FilterDef(
        name = "user_role",
        parameters = @ParamDef(name = "user_role", type = "string")
)
@Filter(
        name = "userRole",
        condition = "user_role LIKE :user_role"
)
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

    public static List<User> getUsers(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<User>  users = new ArrayList<User>();
        try {
             users = session.createQuery("from User", User.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return users;
    }

    public static User createGithubUser(org.eclipse.egit.github.core.User githubUser, String access_token, String jwt_token){
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = new User();
        try {
            User existingUser = getUser(githubUser.getLogin(), "GitHubClient");
            if (existingUser == null){
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
                transaction.commit();
                // WelcomeEmail.main(profile.getEmail());
            } else if(!existingUser.getAccess_token().equals(access_token) || !existingUser.getJwt_token().equals(jwt_token)) {
                existingUser.setAccess_token(access_token);
                existingUser.setJwt_token(jwt_token);
                User.updateUser(existingUser);
            }

            user = getUser(githubUser.getLogin(), "GitHubClient");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return user;
    }


    public static void updateUser(User updatedUser){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            User user = session.createQuery("from User where user_uuid=:user_uuid", User.class)
                    .setParameter("user_uuid", updatedUser.getUser_uuid())
                    .uniqueResult();
            updatedUser.setUpdated_at(new Date());
            Transaction transaction = session.beginTransaction();
            session.merge(updatedUser);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

}
