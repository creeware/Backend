package controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import github.GithubManager;
import model.Repository;
import model.StandardJsonList;
import model.User;
import org.eclipse.jgit.transport.Daemon;
import org.eclipse.jgit.transport.InternalHttpServerGlue;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import spark.Request;
import spark.Response;
import util.HibernateUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.github.scribejava.core.revoke.TokenTypeHint.access_token;
import static github.GithubManager.clone_and_push;

public class RepositoryController {
    // insert a repository
    public static String insertRepository(Request request, Response response) throws ParseException {
        Gson googleJson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonObject payload = jsonParser.parse(request.body()).getAsJsonObject();

        User profile = User.getUser(payload.get("admin_user_name").getAsString(), payload.get("client_name").getAsString());

        String accessToken = String.valueOf(profile.getAccess_token());
        Date due_date = new SimpleDateFormat("yyyy-MM-dd").parse(payload.get("due_date").getAsString());
        Date release_date = new SimpleDateFormat("yyyy-MM-dd").parse(payload.get("release_date").getAsString());
        String repository_name = payload.get("repository_name").getAsString();
        String organization_name = payload.get("organization_name").getAsString();
        String[] user_names = googleJson.fromJson(payload.get("user_names").getAsJsonArray(), String[].class);
        String solutionUrl = payload.get("solution_repo_url").getAsString();
        String challenge_type = payload.get("challenge_type").getAsString();
        String repository_description = payload.get("repository_description").getAsString();
        Boolean unlimited = payload.get("unlimited").getAsBoolean();
        Integer try_count;
        if(unlimited){
            try_count = null;
        } else{
            try_count = payload.get("attempts").getAsInt();
        }

        String result = "";
        try {
            result = GithubManager.createRepository(accessToken, organization_name, user_names, profile.getUsername(),
                                                                    repository_name, solutionUrl, release_date, challenge_type, due_date, repository_description, try_count, unlimited);
        } catch (IOException e) {
            e.printStackTrace();
            response.status(400);
        }
        response.status(200);
        response.type("application/json");
        return result;
    }


    // Update a repository
    public static Repository updateRepository(Request request, Response response) throws IOException {
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
            return updatedRepository;
        }
    }

    public static String deleteRepository(Request request, Response response){
        UUID uuid= UUID.fromString(request.params(":uuid"));
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


    public static Repository getRepository(Request request, Response response){
        String jws = request.headers("Authorization").replaceAll("Bearer ", "");
        User user = User.getUser(jws);
        UUID uuid= UUID.fromString(request.params(":uuid"));
        Session session = HibernateUtil.getSessionFactory().openSession();
        Repository repository = new Repository();
        try {
            repository = session.createQuery("from Repository where repository_uuid=:organization_uuid AND (user_uuid=:user_uuid OR repository_admin_uuid=:user_uuid)", Repository.class)
                    .setParameter("organization_uuid", uuid)
                    .setParameter("user_uuid", user.getUser_uuid())
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

    public static StandardJsonList getRepositories(Request request, Response response) throws ParseException {
        String jws = request.headers("Authorization").replaceAll("Bearer ", "");
        User user = User.getUser(jws);
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Repository> repositories = new ArrayList<Repository>();

        if (request.queryString() != null) {
            String[] params = request.queryString().split("&");
            for (String param : params) {
                if (param.contains("organization_uuid")) {
                    session.enableFilter("organization_uuid")
                            .setParameter("organization_uuid", UUID.fromString(param.split("=")[1]));
                } else if (param.contains("user_uuid")) {
                    session.enableFilter("user_uuid")
                            .setParameter("user_uuid", UUID.fromString(param.split("=")[1]));
                } else if (param.contains("repository_visibility")) {
                    session.enableFilter("repository_visibility")
                            .setParameter("repository_visibility", param.split("=")[1]);
                }
                else if (param.contains("repository_type")) {
                    session.enableFilter("repository_type")
                            .setParameter("repository_type", param.split("=")[1]);
                }
                else if (param.contains("repository_status")) {
                    session.enableFilter("repository_status")
                            .setParameter("repository_status", param.split("=")[1]);
                }
                else if (param.contains("repository_submission_date")) {
                    System.out.println(new SimpleDateFormat("yyyy-mm-dd").parse(param.split("=")[1]));
                    session.enableFilter("repository_submission_date")
                            .setParameter("repository_submission_date", new SimpleDateFormat("yyyy-mm-dd").parse(param.split("=")[1]));
                }
                else if (param.contains("release_date")) {
                    session.enableFilter("release_date")
                            .setParameter("release_date", new SimpleDateFormat("yyyy-mm-dd").parse(param.split("=")[1]));
                }
                else if (param.contains("due_date")) {
                    session.enableFilter("due_date")
                            .setParameter("due_date", new SimpleDateFormat("yyyy-mm-dd").parse(param.split("=")[1]));
                }
            }
        }

        int page_size = Integer.parseInt(request.queryParamOrDefault("page_size", "10"));
        int page = Integer.parseInt(request.queryParamOrDefault("page", "1"));
        String countQ = "Select count (repository.id) from Repository repository";
        Query countQuery = session.createQuery(countQ);
        Long countResults = (Long) countQuery.uniqueResult();
        int lastPageNumber = (int) (Math.ceil(countResults / page_size));
        int index = page_size * (page - 1);
        try {
            Query query = session.createQuery("from Repository WHERE user_uuid=:user_uuid OR repository_admin_uuid=:user_uuid", Repository.class).setParameter("user_uuid", user.getUser_uuid());
            //query.setFirstResult(index);
            //query.setMaxResults(page_size);
            repositories = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(200);
            return new StandardJsonList(countResults, page, page_size, lastPageNumber, repositories);
        }
    }

    public static List<Repository>  getMinimalRepositories(Request request, Response response){
        String jws = request.headers("Authorization").replaceAll("Bearer ", "");
        User user = User.getUser(jws);
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Repository> repositories = new ArrayList<Repository>();
        try {
            repositories = session.createQuery("from Repository repository WHERE user_uuid=:user_uuid OR repository_admin_uuid=:user_uuid", Repository.class)
                    .setParameter("user_uuid", user.getUser_uuid())
                    .getResultList();

        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(200);
            return repositories;
        }
    }

    public static String resetRepository(Request request, Response response){
        JsonParser jsonParser = new JsonParser();
        JsonObject payload = jsonParser.parse(request.body()).getAsJsonObject();
        Repository repository = Repository.getRepository(payload.get("repository_name").getAsString());
        User user = User.getUser(repository.getRepository_admin_uuid());
        clone_and_push(user.getAccess_token(), repository.getOrganization_name(), repository.getRepository_name(), repository.getTemplate_repository_name());
        response.status(200);
        return "success";
    }
}
