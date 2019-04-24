package user;

import email.WelcomeEmail;
import io.github.cdimascio.dotenv.Dotenv;
import model.Model;
import model.User;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.sparkjava.SparkWebContext;
import org.sql2o.Sql2o;
import spark.*;
import sql2omodel.Sql2oModel;

import java.util.Optional;
import java.util.UUID;


public class Profile {
    private CommonProfile profile;
    private Model model;

    public Profile(final Request request, final Response response) {
        profile = (CommonProfile) getProfile(request, response).get();
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        Sql2o sql2o = new Sql2o(dotenv.get("JDBC_DATABASE_URL"), dotenv.get("JDBC_DATABASE_USERNAME"), dotenv.get("JDBC_DATABASE_PASSWORD"));
        model = new Sql2oModel(sql2o);
    }

    public java.util.Map getProfile(){
        return profile.getAttributes();
                // model.getUserByNameAndClient(profile.getUsername(), profile.getClientName());
    }

    public UUID createUser(){
        if(!model.existUserByNameAndClient(profile.getUsername(), profile.getClientName())){
            UUID id = model.createUser(
                    profile.getDisplayName(),
                    profile.getUsername(),
                    profile.getEmail(),
                    profile.getClientName(),
                    profile.getPictureUrl().toString(),
                    profile.getProfileUrl().toString(),
                    "user",
                    profile.getLocation());

            WelcomeEmail.main(profile.getEmail());

            return id;
        }
        return null;
    }

    private java.util.Optional getProfile(final Request request, final Response response) {
        final SparkWebContext context = new SparkWebContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        return manager.get(true);
    }

}
