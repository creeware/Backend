import authentication.AuthenticationController;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.GithubController;
import crud.CrudOrganization;
import handlers.NewUserPayload;
import io.github.cdimascio.dotenv.Dotenv;
import model.Model;
import org.sql2o.*;
import sql2omodel.Sql2oModel;
import user.Profile;
import util.JsonTransformer;
import util.Path.*;
import crud.CrudRepository;
import java.net.URI;
import java.util.UUID;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static user.ProfileController.getProfile;
import static spark.Spark.*;
import static user.ProfileController.handleLogin;


public class Main {
    private static URI dbUri;
    public static Sql2o sql2o;


    public static void main(String[] args) {
        port(getHerokuAssignedPort());

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();



        sql2o = new Sql2o(dotenv.get("JDBC_DATABASE_URL"), dotenv.get("JDBC_DATABASE_USERNAME"), dotenv.get("JDBC_DATABASE_PASSWORD"));


        Model model = new Sql2oModel(sql2o);
        
        // insert a user (using HTTP post method)
        post("/users", (request, response) -> {
            ObjectMapper mapper = new ObjectMapper();
            NewUserPayload creation = mapper.readValue(request.body(), NewUserPayload.class);
            if (!creation.isValid()) {
                response.status(HTTP_BAD_REQUEST);
                return "";
            }
            UUID id = model.createUser(
                    creation.getUser_display_name(),
                    creation.getUsername(),
                    creation.getUser_email(),
                    creation.getUser_client(),
                    creation.getAvatar_url(),
                    creation.getProfile_url(),
                    creation.getUser_role(),
                    creation.getUser_location());
            response.status(200);
            response.type("application/json");
            return id;
        });


        post("/api/repositories", (request, response) -> CrudRepository.insertRepo(request, response));

        delete("/api/repositories/:uuid", "application/json", (request, response) ->
                model.deleteRepository(UUID.fromString(request.params(":uuid"))));

        patch("/api/repositories", (request, response) -> CrudRepository.updateRepo(request, response, model));

        get("/api/repositories/:uuid", "application/json", (request, response) ->
                model.getRepository(UUID.fromString(request.params(":uuid"))), new JsonTransformer());


        

        post("/api/organizations", (request, response) -> CrudOrganization.insertOrg(request, response, model));


        delete("/api/organizations/:uuid",  (request, response) ->
                model.deleteOrganization(UUID.fromString(request.params(":uuid"))));

        patch("/api/organizations", (request, response) -> CrudOrganization.updateOrg(request, response, model));

        get("/api/organizations/:uuid", "application/json", (request, response) ->
                model.getOrganization(UUID.fromString(request.params(":uuid"))), new JsonTransformer());


        before(Web.LOGIN, AuthenticationController.serveLoginPage());
        before("/api", (req, res) -> AuthenticationController.ensureUserIsLoggedIn(req, res));
        before("/api/*", (req, res) -> AuthenticationController.ensureUserIsLoggedIn(req, res));

        get(Web.LOGIN,(req,res) -> handleLogin.handle(req, res) );
        post(Web.LOGIN,(req,res) -> getProfile.handle(req, res) );
        redirect.get("/", "/api");

        get(Web.API, (req,res) -> "hello world");
        post("/hook_payload", "*/*", (req, res) -> {
            String result = GithubController.payloadHandler(req, res, model);
            return result;
        });
        after(Web.LOGIN, (request, response) -> {
            Profile user = new Profile(request, response);
            user.createUser();
        });

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
