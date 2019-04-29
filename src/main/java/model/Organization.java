package model;

import lombok.Data;
import org.hibernate.Session;
import util.HibernateUtil;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "organizations")
public class Organization {
    @Id
    UUID organization_uuid;

    UUID user_uuid;
    String organization_name;
    String organization_description;
    String company_name;
    int repository_count;
    String organization_git_url;
    String organization_github_type;
    Date created_at;
    Date updated_at;


    public static Organization getOrganization(String organizationName){
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
}
