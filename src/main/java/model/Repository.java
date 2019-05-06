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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
@FilterDef(
        name = "repository_visibility",
        parameters = @ParamDef(name = "repository_visibility", type = "string")
)
@Filter(
        name = "repository_visibility",
        condition = "repository_visibility LIKE :repository_visibility"
)
@FilterDef(
        name = "repository_type",
        parameters = @ParamDef(name = "repository_type", type = "string")
)
@Filter(
        name = "repository_type",
        condition = "repository_type LIKE :repository_type"
)@FilterDef(
        name = "repository_status",
        parameters = @ParamDef(name = "repository_status", type = "string")
)
@Filter(
        name = "repository_status",
        condition = "repository_status LIKE :repository_status"
)
@FilterDef(
        name = "repository_submission_date",
        parameters = @ParamDef(name = "repository_submission_date", type = "date")
)
@Filter(
        name = "repository_submission_date",
        condition = "repository_submission_date = :repository_submission_date"
)
@FilterDef(
        name = "release_date",
        parameters = @ParamDef(name = "release_date", type = "date")
)
@Filter(
        name = "release_date",
        condition = "release_date = :release_date"
)
@FilterDef(
        name = "due_date",
        parameters = @ParamDef(name = "due_date", type = "date")
)
@Filter(
        name = "due_date",
        condition = "due_date = :due_date"
)
@Data
@Entity
@Table(name = "repositories")
public class Repository {
    @Id
    @Expose
    private UUID repository_uuid;
    @Expose
    private UUID user_uuid;
    @Expose
    private UUID organization_uuid;
    @Expose
    private UUID repository_admin_uuid;
    @Expose
    private String repository_name;
    @Expose
    private String repository_description;
    @Expose
    private String repository_visibility;
    @Expose
    private String repository_git_url;
    @Expose
    private String repository_github_type;
    @Expose
    private String repository_type;
    @Expose
    private String repository_status;
    @Expose
    private String solution_repository_git_url;
    private String user_name;
    @Expose
    private Boolean unlimited;
    @Expose
    private Integer try_count;
    @Expose
    private Date release_date;
    @Expose
    private Date due_date;
    @Expose
    private Date repository_submission_date;
    @Expose
    private Date created_at;
    @Expose
    private Date updated_at;

    public static Repository getRepository(String repositoryName){
        Session session = HibernateUtil.getSessionFactory().openSession();
        Repository repository = new Repository();
        try {
            repository = session.createQuery("from repository where repository_name=:repository_name", Repository.class)
                    .setParameter("repository_name", repositoryName)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            return repository;
        }
    }

    public static List<Repository> getAdminUserRepositoriesForToday(UUID adminUUID){
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Repository> repositories = new ArrayList<Repository>();
        try {
            repositories = session.createQuery("from Repository where repository_admin_uuid=:repository_admin_uuid AND release_date=:release_date", Repository.class)
                    .setParameter("repository_admin_uuid", adminUUID)
                    .setParameter("release_date", new Date())
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return repositories;
    }

    public static void updateRepository(Repository updatedRepository){
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Repository repository = session.createQuery("from Repository where repository_uuid=:repository_uuid", Repository.class)
                    .setParameter("repository_uuid", updatedRepository.getUser_uuid())
                    .uniqueResult();
            updatedRepository.setUpdated_at(new Date());
            Transaction transaction = session.beginTransaction();
            session.merge(updatedRepository);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
