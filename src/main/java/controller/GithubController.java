package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import email.FailEmail;
import email.SuccessEmail;
import model.Repository;
import model.User;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;

import static canvas.CanvasManager.gradeAssignment;
import static diffing.Diffing.diffRepositories;

public class GithubController {

    public static String payloadHandler(Request req, Response res) throws IOException {
        JsonParser jsonParser = new JsonParser();
        JsonObject payload = jsonParser.parse(req.body()).getAsJsonObject();
        String repo_name = payload.get("repository").getAsJsonObject().get("name").getAsString();
        Repository repository = Repository.getRepository(repo_name);
        User user = User.getUser(repository.getUser_uuid());
        User admin = User.getUser(repository.getRepository_admin_uuid());
        String challenge_type = repository.getChallenge_type();
        String result = "";
        if (!req.headers("X-GitHub-Event").equals("ping")) {
            if (challenge_type.equals("structure-diff")) {
                result = diffRepositories(repository.getRepository_git_url(), repository.getSolution_repository_git_url());
            }

            if (checkResult(result)) {
                repository.setRepository_status("success");
                if (repository.getCanvas_assignment_uuid() != null) {
                    gradeAssignment(admin, repository.getCanvas_course_uuid(), repository.getCanvas_assignment_uuid(), repository.getCanvas_student_uuid(), "pass");
                }
                Repository.updateRepository(repository);

                SuccessEmail email = new SuccessEmail(repository.getUser_uuid(), repo_name);
                email.sendMail();
            } else {
                repository.setRepository_status("fail");
                if (repository.getCanvas_assignment_uuid() != null) {
                    gradeAssignment(admin, repository.getCanvas_course_uuid(), repository.getCanvas_assignment_uuid(), repository.getCanvas_student_uuid(), "fail");
                    System.out.println("lol");
                }
                Repository.updateRepository(repository);
                FailEmail email = new FailEmail(repository.getUser_uuid(), repo_name, result);
                email.sendMail();
            }
        }
        res.status(200);
        return "payload processed";
    }

    private static boolean checkResult(String result) {
        for (String line : result.split("\n")) {
            if (line.length() > 0) {
                if (line.charAt(0) == '+' || line.charAt(0) == '-')
                    return false;
            }
        }
        return true;
    }
}
