package user;

import spark.Request;
import spark.Response;
import spark.Route;

public class ProfileController {
    public static Route getProfile = (Request req, Response res) -> {
        user.Profile profile = new user.Profile(req, res);
        return profile.getProfile().toString();
    };
}
