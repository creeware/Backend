import controller.*;
import org.sql2o.*;
import util.JsonTransformer;
import util.Path.*;

import java.net.URI;
import java.util.UUID;

import static controller.RepositoryController.getMinimalRepositories;
import static controller.UserController.createAndGetProfile;
import static spark.Spark.*;


public class Main {
    private static URI dbUri;
    public static Sql2o sql2o;


    public static void main(String[] args) {
        port(getHerokuAssignedPort());

        // post(Web.LOGOUT,(req,res) ->  AuthenticationController.logout().handle(req, res));

        path(Web.API, () -> {
            before("/*", AuthenticationController::ensureUserIsLoggedIn);

            path(Web.USERS, () -> {
                post("/invite/", "application/json", (request, response) ->
                        UserController.inviteUser(request, response));
                delete("/:uuid", "application/json", (request, response) ->
                        UserController.deleteUser(request, response));
                patch("/patch/", UserController::updateUser, new JsonTransformer());
                get("/:uuid", "application/json", (request, response) ->
                        UserController.getUser(request, response), new JsonTransformer());
                get("/list/", "application/json", (request, response) ->
                        UserController.getUsers(request, response), new JsonTransformer());
                get("/minimal_list/", "application/json", (request, response) ->
                        UserController.getMinimalUsers(request, response), new JsonTransformer());
            });

            path(Web.REPOSITORIES, () -> {
                post("/post/", RepositoryController::insertRepository);
                delete("/:uuid", "application/json", (request, response) ->
                        RepositoryController.deleteRepository(request, response));
                patch("/patch/", "application/json",(request, response) ->
                        RepositoryController.updateRepository(request,response), new JsonTransformer());
                get("/:uuid", "application/json", (request, response) ->
                        RepositoryController.getRepository(request, response), new JsonTransformer());
                get("/list/", "application/json", (request, response) ->
                        RepositoryController.getRepositories(request, response), new JsonTransformer());
                get("/minimal_list/", "application/json", (request, response) ->
                        getMinimalRepositories(request, response), new JsonTransformer());
                post("/reset/", "application/json", RepositoryController::resetRepository);
            });

            path(Web.ORGANIZATIONS, () -> {
                post("/import/", OrganizationController::importOrganization, new JsonTransformer());
                delete(":uuid",  (request, response) ->
                        OrganizationController.deleteOrganization(request, response));
                patch("/patch/", OrganizationController::updateOrganization);
                get("/:uuid", "application/json", (request, response) ->
                        OrganizationController.getOrganization(request, response), new JsonTransformer());
                get("/list/", "application/json", (request, response) ->
                        OrganizationController.getOrganizations(request, response), new JsonTransformer());
                get("/minimal_list/", "application/json", (request, response) ->
                        OrganizationController.getMinimalOrganizations(request, response), new JsonTransformer());
            });

            path(Web.CANVAS, () -> {
                get("/courses/:uuid", "application/json", CanvasController::getCanvasCourses, new JsonTransformer());
                get("/students/", "application/json", CanvasController::getCanvasStudents, new JsonTransformer());
                post("/repository/", "application/json", CanvasController::createCanvasRepository);
                post("/integration/", "application/json", CanvasController::integrateUser);
                post("/migration/", "application/json", CanvasController::migrateUser);
            });

            path(Web.STATISTICS, () -> {
                get("/profile/:uuid", StatisticsController::getUserStatistics, new JsonTransformer());
            });
        });

        post(Web.WEBHOOK, "*/*", (req, res) -> {
            String result = GithubController.payloadHandler(req, res);
            return result;
        });

        post(Auth.GITHUB,(req,res) -> createAndGetProfile.handle(req, res));
    }

    static private int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}
