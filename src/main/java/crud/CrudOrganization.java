package crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import github.GithubManager;
import handlers.NewOrganizationPayload;
import handlers.NewUserPayload;
import model.Model;
import model.Organization;
import spark.Request;
import spark.Response;
import sql2omodel.Sql2oModel;
import user.Profile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

public class CrudOrganization {
    public static Sql2oModel sql2oModel;

    // Insert a Organization
    public static String insertOrg(Request request, Response response, Model model) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        NewOrganizationPayload creation = mapper.readValue(request.body(), NewOrganizationPayload.class);
        /*
        String req = request.body();
        JsonParser jsonParser = new JsonParser();
        JsonObject payload = jsonParser.parse(req).getAsJsonObject();
        String orgName = payload.get("organization_name").getAsString();
        String companyName = payload.get("company_name").getAsString();
        String orgDescription = payload.get("organization_description").getAsString();
        int repositoryCount = payload.get("repository_count").getAsInt();
        String orgGitUrl = payload.get("organization_git_url").getAsString();
        String orgGitHubType = payload.get("organization_github_type").getAsString();
*/
        String result;
        result = model.createOrganization(creation.getUser_uuid(), creation.getOrganization_name(), creation.getCompany_name(),
                creation.getOrganization_description(), creation.getRepository_count(), creation.getOrganization_git_url(), creation.getOrganization_github_type()).toString();
        response.status(200);
        response.type("application/json");
        return result;
    }

    // Update a organization
    public static String updateOrg(Request request, Response response, Model model) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Organization updatedOrganization = mapper.readValue(request.body(), Organization.class);

        model.updateOrganization(updatedOrganization);
        response.status(200);
        response.type("application/json");
        return "success";
    }
}