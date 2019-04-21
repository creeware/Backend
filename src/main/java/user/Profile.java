package user;

import model.Model;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.sparkjava.SparkWebContext;
import spark.*;
import java.util.UUID;


public class Profile {
    private CommonProfile profile;

    public Profile(final Request request, final Response response) {
        profile = (CommonProfile) getProfile(request, response).get();
    }

    public CommonProfile getProfile(){
        return profile;
    }

    public UUID createUser(Model model){
        if(!model.existUserByName(profile.getUsername())){
            UUID id = model.createUser(
                    profile.getUsername(),
                    profile.getDisplayName(),
                    profile.getEmail(),
                    profile.getProfileUrl().toString(),
                    "user",
                    profile.getLocation());

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
