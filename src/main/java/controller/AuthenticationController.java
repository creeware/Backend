package controller;

import authentication.AppConfigFactory;
import model.Profile;
import org.pac4j.core.config.Config;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.sparkjava.CallbackRoute;
import org.pac4j.sparkjava.LogoutRoute;
import org.pac4j.sparkjava.SecurityFilter;
import org.pac4j.sparkjava.SparkWebContext;
import spark.Request;
import spark.Response;
import spark.Route;
import util.JsonTransformer;

public class AuthenticationController {
    static Config config =  AppConfigFactory.build();

    public static SecurityFilter serveLoginPage() {
        return new SecurityFilter(config, "GitHubClient");
    }

    public static Route login = (Request req, Response res) -> {
        Profile user = new Profile(req, res);
        res.body(new JsonTransformer().render(user.profile.getAttributes()));
        res.header("Authorization", user.profile.getAttribute("access_token").toString());
        res.redirect(req.queryParams("redirect_uri"));
        return res.body();
    };

    public static Route callback(){
        return new CallbackRoute(config, "/" , false, true);
    }

    public static Route logout() {
        return new LogoutRoute(config,util.Path.Web.LOGIN );
    }

    public static void ensureUserIsLoggedIn(Request request, Response response) {
        final SparkWebContext context = new SparkWebContext(request, response);
        final ProfileManager manager = new ProfileManager(context);

        if (!manager.isAuthenticated()) {
            response.status(401);
        }
    }
}
