package User;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.sparkjava.SparkWebContext;
import spark.*;

public class Profile {
    public java.util.Optional profile;

    public Profile(final Request request, final Response response) {
        profile = getProfile(request, response);
    }

    public CommonProfile getProfile(){
        return (CommonProfile) profile.get();
    }

    private java.util.Optional getProfile(final Request request, final Response response) {
        final SparkWebContext context = new SparkWebContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        return manager.get(true);
    }

}
