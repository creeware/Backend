package controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import github.GithubManager;
import model.Repository;
import model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import spark.Request;
import spark.Response;
import util.HibernateUtil;

import java.io.IOException;
import java.util.UUID;

public class RepositoryController {
    // insert a repository
    public static String insertRepository(Request request, Response response) {
        Gson googleJson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonObject payload = jsonParser.parse(request.body()).getAsJsonObject();

        User profile = User.getUser(payload.get("user_name").getAsString(), payload.get("client_name").getAsString());

        String accessToken = String.valueOf(profile.getAccess_token());

        String repository_name = payload.get("repository_name").getAsString();
        String organization_name = payload.get("organization_name").getAsString();
        String[] user_names = googleJson.fromJson(payload.get("user_names").getAsJsonArray(), String[].class);
        String solutionUrl = payload.get("solution_repo_url").getAsString();

        String result = "";
        try {
            result = GithubManager.createRepository(accessToken, organization_name, user_names,
                                                                    repository_name, solutionUrl);
        } catch (IOException e) {
            e.printStackTrace();
            response.status(400);
        }
        response.status(200);
        response.type("application/json");
        return result;
    }


    // Update a repository
    public static String updateRepository(Request request, Response response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Repository updatedRepository = mapper.readValue(request.body(), Repository.class);
        try {
            Repository.updateRepository(updatedRepository);
        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            response.status(200);
            response.type("application/json");
            return "success";
        }
    }

    public static String deleteRepository(UUID uuid, Response response){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Repository repository = session.createQuery("from Repository where repository_uuid=:repository_uuid", Repository.class)
                    .setParameter("repository_uuid", uuid)
                    .uniqueResult();

            Transaction transaction = session.beginTransaction();
            session.remove(repository);
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


    public static Repository getRepository(UUID uuid, Response response){
        Session session = HibernateUtil.getSessionFactory().openSession();
        Repository repository = new Repository();
        try {
            repository = session.createQuery("from Repository where repository_uuid=:organization_uuid", Repository.class)
                    .setParameter("organization_uuid", uuid)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(200);
            return repository;
        }
    }
}
