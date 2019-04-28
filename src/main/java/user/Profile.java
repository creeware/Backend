package user;

import email.WelcomeEmail;
import io.github.cdimascio.dotenv.Dotenv;
import model.Model;
import model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.sparkjava.SparkWebContext;
import org.sql2o.Sql2o;
import spark.*;
import sql2omodel.Sql2oModel;
import util.HibernateUtil;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;


public class Profile {
    public CommonProfile profile;
    private Model model;

    public Profile(final Request request, final Response response) {
        profile = (CommonProfile) getProfile(request, response).get();
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        Sql2o sql2o = new Sql2o(dotenv.get("JDBC_DATABASE_URL"), dotenv.get("JDBC_DATABASE_USERNAME"), dotenv.get("JDBC_DATABASE_PASSWORD"));
        model = new Sql2oModel(sql2o);
    }

    public Optional<User>  getProfile(){
        return model.getUserByNameAndClient(profile.getUsername(), profile.getClientName());
    }

    public UUID createUser(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            if (session.createQuery("from User where username=:username AND user_client=:user_client", User.class)
                    .setParameter("username", profile.getUsername())
                    .setParameter("user_client", profile.getClientName())
                    .uniqueResult() == null){
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            WelcomeEmail.main(profile.getEmail());
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
