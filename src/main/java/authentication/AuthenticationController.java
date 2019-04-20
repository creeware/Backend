package authentication;

import org.pac4j.core.config.Config;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.sparkjava.CallbackRoute;
import org.pac4j.sparkjava.LogoutRoute;
import org.pac4j.sparkjava.SecurityFilter;
import org.pac4j.sparkjava.SparkWebContext;
import spark.Request;
import spark.Response;
import spark.Route;

public class AuthenticationController {
    static Config config =  AppConfigFactory.build();

    public static SecurityFilter serveLoginPage() {
        return new SecurityFilter(config, "GitHubClient");
    }

    public static Route callback(){
        return new CallbackRoute(config, "/" , false, true);
    }

    public static Route logout() {
        return new LogoutRoute(config,util.Path.Web.LOGIN );
    }

    // The origin of the request (request.pathInfo()) is saved in the session so
    // the user can be redirected back after authentication
    public static void ensureUserIsLoggedIn(Request request, Response response) {
        final SparkWebContext context = new SparkWebContext(request, response);
        final ProfileManager manager = new ProfileManager(context);

        if (!manager.isAuthenticated()) {
            request.session().attribute("loginRedirect", request.pathInfo());
            response.redirect(util.Path.Web.LOGIN);
        }
    }
}
