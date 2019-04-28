package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import email.FailEmail;
import email.SuccessEmail;
import model.Repository;
import spark.Request;
import spark.Response;

import java.util.InvalidPropertiesFormatException;

import static diffing.Diffing.diffRepositories;

public class GithubController {

    public static String payloadHandler(Request req, Response res) throws InvalidPropertiesFormatException {
        JsonParser jsonParser = new JsonParser();
        JsonObject payload = jsonParser.parse(req.body()).getAsJsonObject();
        String repo_name = payload.get("repository").getAsJsonObject().get("name").getAsString();
        Repository repository = Repository.getRepository(repo_name);

        String result = diffRepositories(repository.getRepository_git_url(), repository.getSolution_repository_git_url());

        if (checkResult(result)){
            repository.setRepository_status("Success");
            Repository.updateRepository(repository);

            SuccessEmail email = new SuccessEmail(repository.getUser_uuid(), repo_name);
            email.sendMail();
        }
        else {
            repository.setRepository_status("Fail");
            Repository.updateRepository(repository);
            FailEmail email = new FailEmail(repository.getUser_uuid(), repo_name, result);
            email.sendMail();
        }

        res.status(200);
        return "payload processed";
    }

    private static boolean checkResult(String result){
        for (String line: result.split("\n")) {
            if (line.charAt(0) == '+' || line.charAt(0) == '-' )
                return false;
        }

        return true;
    }
}
