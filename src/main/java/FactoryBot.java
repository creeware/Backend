import com.github.javafaker.Faker;
import model.Organization;
import model.Repository;
import model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FactoryBot {
    public static String roles[] = { "admin", "teacher", "student"};
    public static String statuses[] = { "unreleased", "released", "solved", "failed", "reset"};

    public static void main(String args[]) {
        Random index = new Random();
        int userCount = 200;
        for (int i = 0; i < userCount; i++) {
            User user = createFakeUser();
            int organizationCount = index.nextInt(20);
            for (int j = 0; j < organizationCount; j++){
                int repositoryCount = index.nextInt(20);
                Organization organization = createFakeOrganization(user.getUser_uuid(), repositoryCount);
                for (int k = 0; k < repositoryCount; k++){
                    createFakeRepository(user.getUser_uuid(), organization.getOrganization_uuid());
                }
            }
        }
    }

    private static User createFakeUser() {
        Random index = new Random();
        Faker faker = new Faker();
        User user = new User();
        user.setJwt_token(faker.crypto().sha256());
        user.setAccess_token(faker.crypto().sha256());
        user.setUser_uuid(UUID.randomUUID());
        user.setCreated_at(faker.date().future(730, TimeUnit.DAYS));
        user.setUser_location(faker.lordOfTheRings().location());
        user.setUser_role(roles[index.nextInt(3)]);
        user.setProfile_url(faker.internet().url());
        user.setAvatar_url(faker.avatar().image());
        user.setUser_client("GitHubClient");
        user.setUser_email(faker.internet().safeEmailAddress());
        user.setUsername(faker.pokemon().name());
        user.setUser_display_name(faker.name().fullName());
        user.setUpdated_at(faker.date().future(730, TimeUnit.DAYS));
        user.setUser_bio(faker.yoda().quote());
        saveUser(user);
        return user;
    }

    ;

    private static Organization createFakeOrganization(UUID userUUID, int repositoryCount) {
        Faker faker = new Faker();
        Organization organization = new Organization();
        organization.setOrganization_github_type(faker.beer().style());
        organization.setOrganization_git_url(faker.internet().url());
        organization.setOrganization_description(faker.lorem().paragraph());
        organization.setOrganization_name(faker.funnyName().name());
        organization.setCompany_name(faker.funnyName().name());
        organization.setUser_uuid(userUUID);
        organization.setOrganization_uuid(UUID.randomUUID());
        organization.setUpdated_at(faker.date().future(730, TimeUnit.DAYS));
        organization.setCreated_at(faker.date().future(730, TimeUnit.DAYS));
        organization.setRepository_count(repositoryCount);
        saveOrganization(organization);
        return organization;
    }

    ;

    private static Repository createFakeRepository(UUID userUUID, UUID orgUUID) {
        Random index = new Random();
        Faker faker = new Faker();
        User user = createFakeUser();
        Repository repository = new Repository();
        repository.setSolution_repository_git_url(faker.internet().url());
        repository.setRepository_status(statuses[index.nextInt(5)]);
        repository.setRepository_type("challenge");
        repository.setRepository_github_type("public");
        repository.setRepository_visibility("public");

        repository.setRepository_git_url(faker.internet().url());
        repository.setRepository_description(faker.chuckNorris().fact());
        repository.setOrganization_uuid(orgUUID);
        repository.setUser_uuid(user.getUser_uuid());
        repository.setRepository_name(faker.crypto().md5());
        repository.setCreated_at(faker.date().future(300, TimeUnit.DAYS));
        repository.setRelease_date(faker.date().between( repository.getCreated_at(), faker.date().future( 700,600, TimeUnit.DAYS)));
        repository.setDue_date(faker.date().between( repository.getRelease_date(), faker.date().future( 800,710, TimeUnit.DAYS)));
        repository.setRepository_submission_date(faker.date().between( repository.getRelease_date(), faker.date().future( 850,710, TimeUnit.DAYS)));
        if (faker.bool().bool()) {
            repository.setTry_count(index.nextInt(21));
            repository.setUnlimited(false);
        }
        repository.setUser_name(user.getUsername());
        repository.setRepository_admin_uuid(userUUID);
        repository.setUpdated_at(faker.date().future(1500, TimeUnit.DAYS));
        repository.setRepository_uuid(UUID.randomUUID());

        saveRepository(repository);
        return repository;
    }

    private static void saveUser(User model) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();
            session.persist(model);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    private static void saveRepository(Repository model) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();
            session.persist(model);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    private static void saveOrganization(Organization model) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Transaction transaction = session.beginTransaction();
            session.persist(model);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
