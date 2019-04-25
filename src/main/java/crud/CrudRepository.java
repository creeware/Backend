package crud;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import github.GithubManager;
import handlers.NewRepositoryPayload;
import model.Model;
import model.Repository;
import spark.Request;
import spark.Response;
import sql2omodel.Sql2oModel;
import user.Profile;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

public class CrudRepository {

    public static Sql2oModel sql2oModel;

    // insert a repository
    public static String insertRepo(Request request, Response response) {
        Gson googleJson = new Gson();
        Profile profile = new Profile(request, response);
        Object accessToken = profile.profile.getAttribute("access_token");
        String accessTokenFinal = String.valueOf(accessToken);
        String req = request.body();
        JsonParser jsonParser = new JsonParser();
        JsonObject payload = jsonParser.parse(req).getAsJsonObject();
        String repository_name = payload.get("repository_name").getAsString();
        String organization_name = payload.get("organization_name").getAsString();
        String[] user_names = googleJson.fromJson(payload.get("user_names").getAsJsonArray(), String[].class);
        String solutionUrl = payload.get("solution_repo_url").getAsString();

       String result = "";
        try {
            result = GithubManager.createRepository(accessTokenFinal, organization_name, user_names,
                    repository_name, solutionUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.status(200);
        response.type("application/json");
        return result;
    }


    // Update a repository
    public static String updateRepo (Request request, Response response, Model model) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Repository updatedRepository = mapper.readValue(request.body(), Repository.class);

        model.updateRepository(updatedRepository);
        response.status(200);
        response.type("application/json");
        return "success";
    }
}
