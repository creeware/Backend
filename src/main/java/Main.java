import controller.AuthenticationController;
import controller.GithubController;
import controller.OrganizationController;
import org.sql2o.*;
import util.JsonTransformer;
import util.Path.*;
import controller.RepositoryController;
import java.net.URI;
import java.util.UUID;

import static controller.UserController.createAndGetProfile;
import static spark.Spark.*;


public class Main {
    private static URI dbUri;
    public static Sql2o sql2o;


    public static void main(String[] args) {
        port(getHerokuAssignedPort());


        before(Web.LOGIN, AuthenticationController.serveLoginPage());
        get(Web.LOGIN,(req,res) -> AuthenticationController.login.handle(req, res) );
        post(Web.LOGIN,(req,res) -> createAndGetProfile.handle(req, res) );

        post(Web.LOGOUT,(req,res) ->  AuthenticationController.logout().handle(req, res));


        path(Web.API, () -> {
            before("/*", AuthenticationController::ensureUserIsLoggedIn);

            path(Web.REPOSITORIES, () -> {
                post("", RepositoryController::insertRepository);
                delete("/:uuid", "application/json", (request, response) ->
                        RepositoryController.deleteRepository(UUID.fromString(request.params(":uuid")), response));
                patch("", RepositoryController::updateRepository);
                get("/:uuid", "application/json", (request, response) ->
                        RepositoryController.getRepository(UUID.fromString(request.params(":uuid")), response), new JsonTransformer());
            });


            path(Web.ORGANIZATIONS, () -> {
                post("", OrganizationController::insertOrganization);
                delete(":uuid",  (request, response) ->
                        OrganizationController.deleteOrganization(UUID.fromString(request.params(":uuid")), response));
                patch("", OrganizationController::updateOrganization);
                get("/:uuid", "application/json", (request, response) ->
                        OrganizationController.getOrganization(UUID.fromString(request.params(":uuid")), response), new JsonTransformer());
            });
        });


        post(Web.WEBHOOK, "*/*", (req, res) -> {
            String result = GithubController.payloadHandler(req, res);
            return result;
        });

        get(Web.CALLBACK,(req,res) ->  AuthenticationController.callback().handle(req, res));
        post(Web.CALLBACK,(req,res) ->  AuthenticationController.callback().handle(req, res));
    }

    static private int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}
