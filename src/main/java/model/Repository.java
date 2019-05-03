package model;

import lombok.Data;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import util.HibernateUtil;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
@FilterDef(
        name = "organization_uuid",
        parameters = @ParamDef(name = "organization_uuid", type = "uuid")
)
@Filter(
        name = "organization_uuid",
        condition = "organization_uuid LIKE :organization_uuid"
)
@FilterDef(
        name = "user_uuid",
        parameters = @ParamDef(name = "user_uuid", type = "uuid")
)
@Filter(
        name = "user_uuid",
        condition = "user_uuid LIKE :user_uuid"
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
    UUID repository_uuid;

    UUID user_uuid;
    UUID organization_uuid;
    UUID repository_admin_uuid;
    String repository_name;
    String repository_description;
    String repository_visibility;
    String repository_git_url;
    String repository_github_type;
    String repository_type;
    String repository_status;
    String solution_repository_git_url;
    String user_name;
    Boolean unlimited;
    Integer try_count;
    Date release_date;
    Date due_date;
    Date repository_submission_date;
    Date created_at;
    Date updated_at;

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
