package controller;

import model.Profile;
import spark.Request;
import spark.Response;
import spark.Route;
import util.JsonTransformer;

public class UserController {

    public static Route createAndGetProfile = (Request req, Response res) -> {
        Profile user = new Profile(req, res);
        user.createUser();
        res.body(new JsonTransformer().render(user.profile.getAttributes()));
        res.header("Authorization", user.profile.getAttribute("access_token").toString());
        return res.body();
    };
}
