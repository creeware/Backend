package user;

import spark.Request;
import spark.Response;
import spark.Route;
import util.JsonTransformer;

public class ProfileController {

    public static Route getProfile = (Request req, Response res) -> {
        user.Profile user = new user.Profile(req, res);
        res.body(new JsonTransformer().render(user.profile.getAttributes()));
        res.header("Authorization", user.profile.getAttribute("access_token").toString());
        return "";
    };

    public static Route handleLogin = (Request req, Response res) -> {
        user.Profile user = new user.Profile(req, res);
        res.body(new JsonTransformer().render(user.profile.getAttributes()));
        res.header("Authorization", user.profile.getAttribute("access_token").toString());
        res.redirect(req.queryParams("redirect_uri"));
        return res;
    };
}
