package controller;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import model.User;
import spark.Request;
import spark.Response;
import spark.Route;

import java.security.Key;

import static spark.Spark.halt;

public class AuthenticationController {
    public static Route ensureUserIsLoggedIn(Request request, Response response) {
        try {
            Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            String jws = request.headers("Authorization").replaceAll("Bearer ", "");
            User user = User.getUser(jws);

            assert Jwts.parser().setSigningKey(key).parseClaimsJws(jws).getBody().getSubject().equals(user.getUsername());
            //OK, we can trust this JWT
        } catch (Exception e) {
            halt(401, "UNAUTHORIZED!");
            //don't trust the JWT!
        }
        return null;
    }
}
