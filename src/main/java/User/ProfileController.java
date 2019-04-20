package User;


import spark.Request;
import spark.Response;
import spark.Route;

public class ProfileController {
    public static Route getProfile = (Request req, Response res) -> {
        Profile profile = new Profile(req, res);
        return profile.getProfile().getAttributes();
    };
}
