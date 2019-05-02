package controller;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import model.User;
import org.eclipse.egit.github.core.service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;
import util.JsonTransformer;

import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class UserController {

    public static Route createAndGetProfile = (Request req, Response res) -> {
        final OAuth2AccessToken accessToken = getAccessToken(req);
        User user = new User();
        org.eclipse.egit.github.core.User githubUser = getGithubUser(accessToken);
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String jws = Jwts.builder().setIssuedAt(new Date()).claim("access_token",accessToken.getAccessToken() ).setSubject(githubUser.getLogin()).signWith(key).compact();
        Map<String, String> map = new HashMap<>();
        map.put("access_token", jws);
        user.createGithubUser(githubUser, accessToken.getAccessToken(), jws);
        res.body(new JsonTransformer().render(map));
        res.header("Authorization", "Bearer " + jws);
        return res.body();
    };

    private static OAuth2AccessToken getAccessToken(Request req) throws InterruptedException, ExecutionException, IOException {
        JsonParser jsonParser = new JsonParser();
        JsonObject payload = jsonParser.parse(req.body()).getAsJsonObject();
        String code = payload.get("code").getAsString();
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        final OAuth20Service service = new ServiceBuilder(dotenv.get("GITHUB_OAUTH_KEY"))
                .apiSecret(dotenv.get("GITHUB_OAUTH_SECRET"))
                .callback(dotenv.get("GITHUB_OAUTH_CALLBACK_URL"))
                .build(GitHubApi.instance());
        return service.getAccessToken(code);
    }
    private static org.eclipse.egit.github.core.User getGithubUser(OAuth2AccessToken accessToken) throws IOException {
        UserService userService = new UserService();
        userService.getClient().setOAuth2Token(accessToken.getAccessToken());
        return userService.getUser();
    }
}
