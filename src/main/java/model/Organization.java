package model;

import com.google.gson.annotations.Expose;
import lombok.Data;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import util.HibernateUtil;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;


@FilterDef(
        name = "organization_uuid",
        parameters = @ParamDef(name = "organization_uuid", type = "pg-uuid")
)
@Filter(
        name = "organization_uuid",
        condition = "organization_uuid = :organization_uuid"
)
@FilterDef(
        name = "user_uuid",
        parameters = @ParamDef(name = "user_uuid", type = "pg-uuid")
)
@Filter(
        name = "user_uuid",
        condition = "user_uuid = :user_uuid"
)
@Data
@Entity
@Table(name = "organizations")
public class Organization {
    @Id
    @Expose
    private UUID organization_uuid;
    @Expose
    private UUID user_uuid;
    @Expose
    private String organization_name;
    @Expose
    private String organization_description;
    @Expose
    private String company_name;
    @Expose
    private int repository_count;
    @Expose
    private String organization_git_url;
    @Expose
    private String organization_github_type;
    @Expose
    private Date created_at;
    @Expose
    private Date updated_at;


    public static Organization getOrganization(String organizationName) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Organization organization = new Organization();
        try {
            organization = session.createQuery("from Organization where organization_name=:organization_name", Organization.class)
                    .setParameter("organization_name", organizationName)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            return organization;
        }
    }

    public static Organization getOrganization(UUID uuid) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Organization organization = new Organization();
        try {
            organization = session.createQuery("from Organization where organization_uuid=:organization_uuid", Organization.class)
                    .setParameter("organization_uuid", uuid)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            return organization;
        }
    }

    public static Organization createOrganization(User user, org.eclipse.egit.github.core.User organization) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Organization newOrganization = new Organization();
        newOrganization.setCompany_name(organization.getCompany());
        newOrganization.setOrganization_description(organization.getBio());
        newOrganization.setOrganization_git_url(organization.getHtmlUrl());
        newOrganization.setOrganization_github_type(organization.getType());
        newOrganization.setUser_uuid(user.getUser_uuid());
        newOrganization.setOrganization_uuid(UUID.randomUUID());
        newOrganization.setOrganization_name(organization.getLogin());
        newOrganization.setRepository_count(organization.getPublicRepos());
        newOrganization.setCreated_at(new Date());

        try {
            Transaction transaction = session.beginTransaction();
            session.persist(newOrganization);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return newOrganization;
    }
}
