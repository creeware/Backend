package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.GithubManager;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import model.Repository;
import model.StandardJsonList;
import model.User;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import payload.NewOrganizationPayload;
import model.Organization;
import spark.Request;
import spark.Response;
import util.HibernateUtil;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class OrganizationController {

    // Update a organization
    public static String updateOrganization(Request request, Response response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Organization updatedOrganization = mapper.readValue(request.body(), Organization.class);
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Organization organization = session.createQuery("from Organization where organization_uuid=:organization_uuid", Organization.class)
                    .setParameter("organization_uuid", updatedOrganization.getOrganization_uuid())
                    .uniqueResult();
            updatedOrganization.setUpdated_at(new Date());
            Transaction transaction = session.beginTransaction();
            session.merge(updatedOrganization);
            session.flush();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(200);
            return "success";
        }
    }

    public static String deleteOrganization(Request request, Response response) {
        UUID uuid = UUID.fromString(request.params(":uuid"));
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Organization organization = session.createQuery("from Organization where organization_uuid=:organization_uuid", Organization.class)
                    .setParameter("organization_uuid", uuid)
                    .uniqueResult();

            Transaction transaction = session.beginTransaction();
            session.remove(organization);
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


    public static Organization getOrganization(Request request, Response response) {
        String jws = request.headers("Authorization").replaceAll("Bearer ", "");
        User user = User.getUser(jws);
        UUID uuid = UUID.fromString(request.params(":uuid"));
        Session session = HibernateUtil.getSessionFactory().openSession();
        Organization organization = new Organization();
        try {
            organization = session.createQuery("from Organization where organization_uuid=:organization_uuid AND user_uuid=:user_uuid", Organization.class)
                    .setParameter("organization_uuid", uuid)
                    .setParameter("user_uuid", user.getUser_uuid())
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(200);
            return organization;
        }
    }


    public static StandardJsonList getOrganizations(Request request, Response response) {
        String jws = request.headers("Authorization").replaceAll("Bearer ", "");
        User user = User.getUser(jws);
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Organization> organizations = new ArrayList<Organization>();
        if (request.queryString() != null) {
            String[] params = request.queryString().split("&");
            for (String param : params) {
                if (param.contains("organization_uuid")) {
                    session.enableFilter("organization_uuid")
                            .setParameter("organization_uuid", UUID.fromString(param.split("=")[1]));
                } else if (param.contains("user_uuid")) {
                    session.enableFilter("user_uuid")
                            .setParameter("user_uuid", UUID.fromString(param.split("=")[1]));
                }
            }
        }

        int page_size = Integer.parseInt(request.queryParamOrDefault("page_size", "10"));
        int page = Integer.parseInt(request.queryParamOrDefault("page", "1"));
        String countQ = "Select count (organization.id) from Organization organization";
        Query countQuery = session.createQuery(countQ);
        Long countResults = (Long) countQuery.uniqueResult();
        int lastPageNumber = (int) (Math.ceil(countResults / page_size));
        int index = page_size * (page - 1);
        try {
            Query query = session.createQuery("from Organization WHERE user_uuid=:user_uuid", Organization.class).setParameter("user_uuid", user.getUser_uuid());
            //query.setFirstResult(index);
            //query.setMaxResults(page_size);
            organizations = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(200);
            return new StandardJsonList(countResults, page, page_size, lastPageNumber, organizations);
        }
    }

    public static List<Organization> getMinimalOrganizations(Request request, Response response) {
        String jws = request.headers("Authorization").replaceAll("Bearer ", "");
        User user = User.getUser(jws);
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Organization> organizations = new ArrayList<Organization>();
        try {
            organizations = session.createQuery("from Organization organization WHERE user_uuid=:user_uuid", Organization.class).setParameter("user_uuid", user.getUser_uuid()).getResultList();

        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(200);
            return organizations;
        }
    }

    public static List<Repository> importOrganization(Request request, Response response) throws IOException {
        String jws = request.headers("Authorization").replaceAll("Bearer ", "");
        ObjectMapper mapper = new ObjectMapper();
        NewOrganizationPayload creation = mapper.readValue(request.body(), NewOrganizationPayload.class);
        List<Repository> repositories = GithubManager.importOrganization(jws, creation.getOrganization_name());
        return repositories;
    }


}