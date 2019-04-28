package model;

import lombok.Data;
import org.hibernate.Session;
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
}
