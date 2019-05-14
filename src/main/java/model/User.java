package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
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
        name = "user_uuid",
        parameters = @ParamDef(name = "user_uuid", type = "string")
)
@Filter(
        name = "user_uuid",
        condition = "user_uuid = :user_uuid"
)
@FilterDef(
        name = "user_role",
        parameters = @ParamDef(name = "user_role", type = "string")
)
@Filter(
        name = "user_role",
        condition = "user_role LIKE :user_role"
)
@Data
@Entity
@Table(name = "users")
public class User {

    private String access_token;
    private String jwt_token;

    @Id
    @Expose
    private UUID user_uuid;
    @Expose
    private String user_display_name;
    @Expose
    private String username;
    @Expose
    private String user_email;
    @Expose
    private String user_client;
    @Expose
    private String avatar_url;
    @Expose
    private String profile_url;
    @Expose
    private String user_role;
    @Expose
    private String user_location;
    @Expose
    private String user_bio;
    @Expose
    private String user_status;
    @Expose
    private Date created_at;
    @Expose
    private Date updated_at;

    private String canvas_access_token;
    private String canvas_base_url;
    private String canvas_user_uuid;

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

    public static User getCanvasUser(UUID user_uuid, String canvas_user_uuid){
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = new User();
        try {
            user = session.createQuery("from User where user_uuid=:user_uuid AND canvas_user_uuid=:canvas_user_uuid", User.class)
                    .setParameter("user_uuid", user_uuid)
                    .setParameter("canvas_user_uuid", canvas_user_uuid)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            return user;
        }
    }

    public static User getCanvasUser(String canvas_user_uuid){
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = new User();
        try {
            user = session.createQuery("from User where canvas_user_uuid=:canvas_user_uuid", User.class)
                    .setParameter("canvas_user_uuid", canvas_user_uuid)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            return user;
        }
    }

    public static User getUser(UUID user_uuid){
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = new User();
        try {
            user = session.createQuery("from User where user_uuid=:user_uuid", User.class)
                    .setParameter("user_uuid", user_uuid)
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
                user.setProfile_url(githubUser.getHtmlUrl());
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


    public static User createCanvasUser(String user_canvas_id){
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = new User();
        try {
                user.setUser_client("GitHubClient");
                user.setUser_role("provisional");
                user.setJwt_token("temporary_jwt");
                user.setCreated_at(new Date());
                user.setUser_uuid(UUID.randomUUID());
                user.setCanvas_user_uuid(user_canvas_id);

                Transaction transaction = session.beginTransaction();
                session.persist(user);
                transaction.commit();
                user = getCanvasUser(user_canvas_id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return user;
    }

}
