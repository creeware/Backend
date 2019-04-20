import authentication.AuthenticationController;
import util.JsonTransformer;
import util.Path.*;
import static User.ProfileController.getProfile;
import static spark.Spark.*;


public class Main {


    public static void main(String[] args) {
        port(getHerokuAssignedPort());

        before(Web.LOGIN, AuthenticationController.serveLoginPage());
        before("/api", (req, res) -> AuthenticationController.ensureUserIsLoggedIn(req, res));
        before("/api/*", (req, res) -> AuthenticationController.ensureUserIsLoggedIn(req, res));

        redirect.get(Web.LOGIN, "/api/user_profile");
        redirect.get("/", "/api");

        get(Web.API, (req,res) -> "hello world");
        get(Web.USER_PROFILE, "application/json", (req,res) -> getProfile.handle(req, res), new JsonTransformer());

        get(Web.CALLBACK,(req,res) ->  AuthenticationController.callback().handle(req, res));
        post(Web.CALLBACK,(req,res) ->  AuthenticationController.callback().handle(req, res));
        post(Web.LOGOUT,(req,res) ->  AuthenticationController.logout().handle(req, res));
    }

    static private int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}
