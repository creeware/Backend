package model;

import email.WelcomeEmail;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.sparkjava.SparkWebContext;
import spark.*;
import util.HibernateUtil;
import java.util.Date;
import java.util.UUID;

import static model.User.getUser;


public class Profile {
    public CommonProfile profile;

    public Profile(final Request request, final Response response) {
        profile = (CommonProfile) getProfile(request, response).get();
    }

    public UUID createUser(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            if (getUser(profile.getUsername(), profile.getClientName()) == null){
                User user = new User();
                user.setUser_display_name(profile.getDisplayName());
                user.setUsername(profile.getUsername());
                user.setUser_email(profile.getEmail());
                user.setUser_client(profile.getClientName());
                user.setAvatar_url(profile.getPictureUrl().toString());
                user.setProfile_url(profile.getProfileUrl().toString());
                user.setUser_role("user");
                user.setUser_location(profile.getLocation());
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

    private java.util.Optional getProfile(final Request request, final Response response) {
        final SparkWebContext context = new SparkWebContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        return manager.get(true);
    }

}
