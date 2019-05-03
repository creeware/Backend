package controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import github.GithubManager;
import model.Repository;
import model.StandardJsonList;
import model.User;
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

public class RepositoryController {
    // insert a repository
    public static String insertRepository(Request request, Response response) throws ParseException {
        Gson googleJson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonObject payload = jsonParser.parse(request.body()).getAsJsonObject();

        User profile = User.getUser(payload.get("admin_user_name").getAsString(), payload.get("client_name").getAsString());

        String accessToken = String.valueOf(profile.getAccess_token());

        Date release_date = new SimpleDateFormat("yyyy-MM-dd").parse(payload.get("release_date").getAsString());
        String repository_name = payload.get("repository_name").getAsString();
        String organization_name = payload.get("organization_name").getAsString();
        String[] user_names = googleJson.fromJson(payload.get("user_names").getAsJsonArray(), String[].class);
        String solutionUrl = payload.get("solution_repo_url").getAsString();

        String result = "";
        try {
            result = GithubManager.createRepository(accessToken, organization_name, user_names, profile.getUsername(),
                                                                    repository_name, solutionUrl, release_date);
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
        UUID uuid= UUID.fromString(request.params(":uuid"));
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

    public static StandardJsonList getRepositories(Request request, Response response){
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
                    session.enableFilter("repository_submission_date")
                            .setParameter("repository_submission_date", param.split("=")[1]);
                }
                else if (param.contains("repository_submission_date")) {
                    session.enableFilter("repository_visibility")
                            .setParameter("repository_visibility", param.split("=")[1]);
                }
                else if (param.contains("release_date")) {
                    session.enableFilter("release_date")
                            .setParameter("release_date", param.split("=")[1]);
                }
                else if (param.contains("due_date")) {
                    session.enableFilter("due_date")
                            .setParameter("due_date", param.split("=")[1]);
                }
                else if (param.contains("due_date")) {
                    session.enableFilter("due_date")
                            .setParameter("due_date", param.split("=")[1]);
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
            Query query = session.createQuery("from Repository", Repository.class);
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
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Repository> repositories = new ArrayList<Repository>();
        try {
            repositories = session.createQuery("from Repository repository", Repository.class).getResultList();

        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(200);
            return repositories;
        }
    }
}
