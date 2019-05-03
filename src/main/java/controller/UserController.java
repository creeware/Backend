package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import model.MinimalUser;
import model.StandardJsonList;
import model.User;
import org.eclipse.egit.github.core.service.UserService;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import spark.Request;
import spark.Response;
import spark.Route;
import util.HibernateUtil;
import util.JsonTransformer;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.security.Key;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class UserController {

    public static Route createAndGetProfile = (Request req, Response res) -> {
        final OAuth2AccessToken accessToken = getAccessToken(req);

        org.eclipse.egit.github.core.User githubUser = getGithubUser(accessToken);
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String jws = Jwts.builder().setIssuedAt(new Date()).claim("access_token",accessToken.getAccessToken() ).setSubject(githubUser.getLogin()).signWith(key).compact();
        User user = User.createGithubUser(githubUser, accessToken.getAccessToken(), jws);
        jws = Jwts.builder().setIssuedAt(new Date()).claim("user_id", user.getUser_uuid()).claim("access_token",accessToken.getAccessToken() ).setSubject(githubUser.getLogin()).signWith(key).compact();
        user.setJwt_token(jws);
        User.updateUser(user);
        Map<String, String> map = new HashMap<>();
        map.put("access_token", jws);
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

    // Update a user
    public static User updateUser(Request request, Response response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        User updatedUser = mapper.readValue(request.body(), User.class);
        String jws = request.headers("Authorization").replaceAll("Bearer ", "");
        User user = User.getUser(jws);
        updatedUser.setAccess_token(user.getAccess_token());
        updatedUser.setJwt_token(user.getJwt_token());
        try {
            User.updateUser(updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            response.status(200);
            response.type("application/json");
            User newUser = User.getUser(jws);
            return newUser;
        }
    }

    public static String deleteUser(Request request, Response response){
        UUID uuid= UUID.fromString(request.params(":uuid"));
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            User user = session.createQuery("from User where user_uuid=:user_uuid", User.class)
                    .setParameter("user_uuid", uuid)
                    .uniqueResult();

            Transaction transaction = session.beginTransaction();
            session.remove(user);
            session.flush();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(204);
            return "success";
        }
    }


    public static User getUser(Request request, Response response){
        UUID uuid= UUID.fromString(request.params(":uuid"));
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = new User();
        try {
            user = session.createQuery("from User where user_uuid=:user_uuid", User.class)
                    .setParameter("user_uuid", uuid)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(200);
            return user;
        }
    }

    public static StandardJsonList getUsers(Request request, Response response){
        Session session = HibernateUtil.getSessionFactory().openSession();

        if (request.queryString() != null) {
            String[] params = request.queryString().split("&");
            for (String param : params) {
                if (param.contains("user_uuid")) {
                    session.enableFilter("user_uuid")
                            .setParameter("user_uuid", UUID.fromString(param.split("=")[1]));
                } else if (param.contains("user_role")) {
                    session.enableFilter("user_role")
                            .setParameter("user_role", param.split("=")[1]);
                }
            }
        }

        int page_size = Integer.parseInt(request.queryParamOrDefault("page_size", "10"));
        int page = Integer.parseInt(request.queryParamOrDefault("page", "1"));
        String countQ = "Select count (user.id) from User user";
        Query countQuery = session.createQuery(countQ);
        Long countResults = (Long) countQuery.uniqueResult();
        int lastPageNumber = (int) (Math.ceil(countResults / page_size));
        int index = page_size * (page - 1);
        List<User> users = new ArrayList<User>();
        try {
            Query query = session.createQuery("from User", User.class);
            // query.setFirstResult(index);
            // query.setMaxResults(page_size);
            users = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(200);
            return new StandardJsonList(countResults, page, page_size, lastPageNumber, users);
        }
    }

    public static List<User>  getMinimalUsers(Request request, Response response){
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<User> users = new ArrayList<User>();
        try {
            users = session.createQuery("from User user", User.class).getResultList();

        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(200);
            return users;
        }
    }
}
