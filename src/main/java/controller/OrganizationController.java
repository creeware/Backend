package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.Transaction;
import payload.NewOrganizationPayload;
import model.Organization;
import spark.Request;
import spark.Response;
import util.HibernateUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class OrganizationController {

    // Insert a Organization
    public static String insertOrganization(Request request, Response response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        NewOrganizationPayload creation = mapper.readValue(request.body(), NewOrganizationPayload.class);

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            if (Organization.getOrganization(creation.getOrganization_name()) == null){
                Organization organization = new Organization();

                organization.setOrganization_uuid(UUID.randomUUID());
                organization.setUser_uuid(creation.getUser_uuid());
                organization.setOrganization_name(creation.getOrganization_name());
                organization.setOrganization_description(creation.getOrganization_description());
                organization.setCompany_name(creation.getCompany_name());
                organization.setRepository_count(creation.getRepository_count());
                organization.setOrganization_git_url(creation.getOrganization_git_url());
                organization.setOrganization_github_type(creation.getOrganization_github_type());
                organization.setCreated_at(new Date());

                Transaction transaction = session.beginTransaction();
                session.persist(organization);
                session.flush();
                transaction.commit();
            }
        } catch (Exception e) {
            response.status(400);
            e.printStackTrace();
        } finally {
            session.close();
            response.status(200);
            response.type("application/json");
            return "success";
        }
    }

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

    public static String deleteOrganization(Request request, Response response){
        UUID uuid= UUID.fromString(request.params(":uuid"));
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


    public static Organization getOrganization(Request request, Response response){
        UUID uuid= UUID.fromString(request.params(":uuid"));
        Session session = HibernateUtil.getSessionFactory().openSession();
        Organization organization = new Organization();
        try {
             organization = session.createQuery("from Organization where organization_uuid=:organization_uuid", Organization.class)
                    .setParameter("organization_uuid", uuid)
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


    public static List<Organization> getOrganizations(Request request, Response response){
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Organization> organizations = new ArrayList<Organization>();
        String organization_uuid = request.queryParamOrDefault("organization_uuid", null);
        String user_uuid = request.queryParamOrDefault("user_uuid", null);
        if (organization_uuid != null){
            session.enableFilter("organization_uuid")
                    .setParameter("organization_uuid", organization_uuid);
        }
        if (user_uuid != null){
            session.enableFilter("user_uuid")
                    .setParameter("user_uuid", user_uuid);
        }
        try {
            organizations = session.createQuery("from Organization", Organization.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            response.status(400);
        } finally {
            session.close();
            response.status(200);
            return organizations;
        }
    }


}