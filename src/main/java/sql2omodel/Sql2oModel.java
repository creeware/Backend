package sql2omodel;

import model.*;
import org.jetbrains.annotations.NotNull;
import util.RandomUuidGenerator;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Sql2oModel implements Model {

    private Sql2o sql2o;
    private RandomUuidGenerator uuidGenerator;

    public Sql2oModel(Sql2o sql2o) {
        this.sql2o = sql2o;
        uuidGenerator = new RandomUuidGenerator();
    }

    /**
     * User
     **/
    @Override
    public UUID createUser(String user_display_name,
                           String username,
                           String user_email,
                           String user_client,
                           String avatar_url,
                           String profile_url,
                           String user_role,
                           String user_location) {
        try (Connection conn = sql2o.beginTransaction()) {
            UUID userUuid = uuidGenerator.generate();
            conn.createQuery("insert into users(user_uuid, user_display_name, username, user_email, user_client, avatar_url, profile_url, user_role, user_location, created_at) " +
                    "VALUES (:user_uuid, :user_display_name, :username, :user_email, :user_client, :avatar_url, :profile_url, :user_role, :user_location, :created_at)")
                    .addParameter("user_uuid", userUuid)
                    .addParameter("user_display_name", user_display_name)
                    .addParameter("username", username)
                    .addParameter("user_email", user_email)
                    .addParameter("user_client", user_client)
                    .addParameter("avatar_url", avatar_url)
                    .addParameter("profile_url", profile_url)
                    .addParameter("user_role", user_role)
                    .addParameter("user_location", user_location)
                    .addParameter("created_at", new Date())
                    .executeUpdate();
            conn.commit();
            return userUuid;
        }
    }

    @Override
    public List<User> getAllUsers() {
        try (Connection conn = sql2o.open()) {
            List<User> users = conn.createQuery("select * from users")
                    .executeAndFetch(User.class);
            return users;
        }
    }


    @Override
    public boolean existUser(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            List<User> users = conn.createQuery("select * from users where user_uuid=:user_uuid")
                    .addParameter("user_uuid", uuid)
                    .executeAndFetch(User.class);
            return users.size() > 0;
        }
    }

    @Override
    public boolean existUserByNameAndClient(String username, String user_client) {
        try (Connection conn = sql2o.open()) {
            List<User> users = conn.createQuery("select * from users where username=:username AND user_client=:user_client")
                    .addParameter("username", username)
                    .addParameter("user_client", user_client)
                    .executeAndFetch(User.class);
            return users.size() > 0;
        }
    }

    @Override
    public Optional<User> getUser(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            List<User> users = conn.createQuery("select * from users where user_uuid=:user_uuid")
                    .addParameter("user_uuid", uuid)
                    .executeAndFetch(User.class);
            return getUser(users);
        }
    }

    @Override
    public Optional<User> getUserByNameAndClient(String username, String user_client) {
        try (Connection conn = sql2o.open()) {
            List<User> users = conn.createQuery("SELECT * FROM users WHERE username=:username AND user_client=:user_client")
                    .addParameter("username", username)
                    .addParameter("user_client", user_client)
                    .executeAndFetch(User.class);
            return getUser(users);
        }
    }

    @NotNull
    private Optional<User> getUser(List<User> users) {
        if (users.size() == 0) {
            return Optional.empty();
        } else if (users.size() == 1) {
            return Optional.of(users.get(0));
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public void updateUser(User user) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("update users set user_display_name=:user_display_name, " +
                    "username=:username, user_email=:user_email, user_client=:user_client, avatar_url=:avatar_url, profile_url=:profile_url, user_role=:user_role, user_location=:user_location where user_uuid=:user_uuid")
                    .addParameter("user_uuid", user.getUser_uuid())
                    .addParameter("user_display_name", user.getUser_display_name())
                    .addParameter("username", user.getUsername())
                    .addParameter("user_email", user.getUser_email())
                    .addParameter("user_client", user.getUser_client())
                    .addParameter("user_email", user.getAvatar_url())
                    .addParameter("profile_url", user.getProfile_url())
                    .addParameter("user_role", user.getUser_role())
                    .addParameter("user_location", user.getUser_location())
                    .executeUpdate();
        }
    }

    @Override
    public String deleteUser(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("delete from users where user_uuid=:user_uuid")
                    .addParameter("user_uuid", uuid)
                    .executeUpdate();
            return "200";
        } catch (Exception e){
            return "400";
        }
    }

    /**
     * Repository
     **/
    @Override
    public UUID createRepository(
                                 UUID user_uuid,
                                 UUID organization_uuid,
                                 String repository_name,
                                 String repository_description,
                                 String repository_visibility,
                                 String repository_git_url,
                                 String repository_github_type,
                                 String repository_type,
                                 String repository_status,
                                 String solution_repository_git_url
    ) {
        try (Connection conn = sql2o.beginTransaction()) {
            UUID repositoryUuid = uuidGenerator.generate();
            conn.createQuery("insert into repositories(repository_uuid, user_uuid, organization_uuid, repository_name, repository_description, repository_visibility, repository_git_url, repository_github_type, repository_type, repository_status, solution_repository_git_url, created_at) " +
                    "VALUES (:repository_uuid, :user_uuid, :organization_uuid, :repository_name, :repository_description, :repository_visibility, :repository_git_url, :repository_github_type, :repository_type, :repository_status, :solution_repository_git_url, :created_at)")
                    .addParameter("repository_uuid", repositoryUuid)
                    .addParameter("user_uuid", user_uuid)
                    .addParameter("organization_uuid", organization_uuid)
                    .addParameter("repository_name", repository_name)
                    .addParameter("repository_description", repository_description)
                    .addParameter("repository_visibility", repository_visibility)
                    .addParameter("repository_git_url", repository_git_url)
                    .addParameter("repository_github_type", repository_github_type)
                    .addParameter("repository_type", repository_type)
                    .addParameter("repository_status", repository_status)
                    .addParameter("solution_repository_git_url", solution_repository_git_url )
                    .addParameter("created_at", new Date())
                    .executeUpdate();
            conn.commit();
            return repositoryUuid;
        }
    }

    @Override
    public List<Repository> getAllRepositories() {
        try (Connection conn = sql2o.open()) {
            List<Repository> repositories = conn.createQuery("select * from repositories")
                    .executeAndFetch(Repository.class);
            return repositories;
        }
    }

    @Override
    public List<Repository> getAllUserRepositories(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            List<Repository> repositories = conn.createQuery("select * from repositories")
                    .addParameter("user_uuid", uuid)
                    .executeAndFetch(Repository.class);
            return repositories;
        }
    }

    @Override
    public List<Repository> getAllOrganizationRepositories(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            List<Repository> repositories = conn.createQuery("select * from repositories")
                    .addParameter("organization_uuid", uuid)
                    .executeAndFetch(Repository.class);
            return repositories;
        }
    }

    @Override
    public boolean existRepository(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            List<Repository> repositories = conn.createQuery("select * from repositories where repository_uuid=:repository_uuid")
                    .addParameter("repository_uuid", uuid)
                    .executeAndFetch(Repository.class);
            return repositories.size() > 0;
        }
    }

    @Override
    public Optional<Repository> getRepository(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            List<Repository> repositories = conn.createQuery("select * from repositories where repository_uuid=:repository_uuid")
                    .addParameter("repository_uuid", uuid)
                    .executeAndFetch(Repository.class);
            return getRepository(repositories);
        }
    }

    @Override
    public Optional<Repository> getRepositoryByName(String repo_name) {
        try (Connection conn = sql2o.open()) {
            List<Repository> repositories = conn.createQuery("select * from repositories where repository_name=:repository_name")
                            .addParameter("repository_name", repo_name)
                            .executeAndFetch(Repository.class);
            return getRepository(repositories);
        }
    }



    @NotNull
    private Optional<Repository> getRepository(List<Repository> repositories) {
        if (repositories.size() == 0) {
            return Optional.empty();
        } else if (repositories.size() == 1) {
            return Optional.of(repositories.get(0));
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public void updateRepository(Repository repository) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("update repositories set repository_name=:repository_name, " +
                    "repository_description=:repository_description, repository_visibility=:repository_visibility," +
                    "repository_git_url=:repository_git_url, repository_github_type=:repository_github_type," +
                    "repository_type=:repository_type, repository_status=:repository_status, " +
                    "solution_repository_git_url=:solution_repository_git_url, updated_at=:updated_at " +
                    "where repository_uuid=:repository_uuid")
                    .addParameter("repository_uuid", repository.getRepository_uuid())
                    .addParameter("repository_name", repository.getRepository_name())
                    .addParameter("repository_description", repository.getRepository_description())
                    .addParameter("repository_visibility", repository.getRepository_visibility())
                    .addParameter("repository_git_url", repository.getRepository_git_url())
                    .addParameter("repository_github_type", repository.getRepository_github_type())
                    .addParameter("repository_type", repository.getRepository_type())
                    .addParameter("repository_status", repository.getRepository_status())
                    .addParameter("solution_repository_git_url", repository.getSolution_repository_git_url())
                    .addParameter("updated_at", new Date())
                    .executeUpdate();
        }
    }

    @Override
    public String deleteRepository(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("delete from repositories where repository_uuid=:repository_uuid")
                    .addParameter("repository_uuid", uuid)
                    .executeUpdate();
            return "204";
        } catch (Exception e){
            return "400";
        }
    }

    /**
     * Organization
     **/
    @Override
    public UUID createOrganization(String organization_name,
                                   String company_name,
                                   String organization_description,
                                   int repository_count,
                                   String organization_git_url,
                                   String organization_github_type
                                   ) {
        try (Connection conn = sql2o.beginTransaction()) {
            UUID organizationUuid = uuidGenerator.generate();
            conn.createQuery("insert into organizations(organization_uuid, organization_name, company_name, organization_description, repository_count, organization_git_url, organization_github_type, created_at) " +
                    "VALUES (:organization_uuid, :organization_name, :company_name, :organization_description, :repository_count, :organization_git_url, :organization_github_type, :created_at)")
                    .addParameter("organization_uuid", organizationUuid)
                    .addParameter("company_name", company_name)
                    .addParameter("organization_name", organization_name)
                    .addParameter("organization_description", organization_description)
                    .addParameter("repository_count", repository_count)
                    .addParameter("organization_git_url", organization_git_url)
                    .addParameter("organization_github_type", organization_github_type)
                    .addParameter("created_at", new Date())
                    .executeUpdate();
            conn.commit();
            return organizationUuid;
        }
    }

    @Override
    public List<Organization> getAllOrganizations() {
        try (Connection conn = sql2o.open()) {
            List<Organization> organizations = conn.createQuery("select * from organizations")
                    .executeAndFetch(Organization.class);
            return organizations;
        }
    }

    @Override
    public List<Organization> getAllUserOrganizations(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            List<Organization> organizations = conn.createQuery("select * from organizations")
                    .addParameter("user_uuid", uuid)
                    .executeAndFetch(Organization.class);
            return organizations;
        }
    }

    @Override
    public boolean existOrganization(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            List<Organization> organizations = conn.createQuery("select * from organizations where organization_uuid=:organization_uuid")
                    .addParameter("organization_uuid", uuid)
                    .executeAndFetch(Organization.class);
            return organizations.size() > 0;
        }
    }

    @Override
    public Optional<Organization> getOrganization(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            List<Organization> organizations = conn.createQuery("select * from organizations where organization_uuid=:organization_uuid")
                    .addParameter("organization_uuid", uuid)
                    .executeAndFetch(Organization.class);
            return getOrganization(organizations);
        }
    }
    @Override
    public Optional<Organization>  getOrganizationbyName(String organization_name){
        try (Connection conn = sql2o.open()) {
            List<Organization> organizations = conn.createQuery("SELECT * FROM organizations WHERE organization_name=:organization_name")
                    .addParameter("organization_name", organization_name)
                    .executeAndFetch(Organization.class);
            return getOrganization(organizations);
        }
    }

    @NotNull
    private Optional<Organization> getOrganization(List<Organization> organizations) {
        if (organizations.size() == 0) {
            return Optional.empty();
        } else if (organizations.size() == 1) {
            return Optional.of(organizations.get(0));
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public void updateOrganization(Organization organization) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("update organizations set organization_name=:organization_name, company_name=:company_name " +
                    "organization_description=:organization_description, repository_count=:repository_count," +
                    "organization_git_url=:organization_git_url, organization_github_type=:organization_github_type" +
                    " where organization_uuid=:organization_uuid")
                    .addParameter("organization_uuid", organization.getOrganization_uuid())
                    .addParameter("organization_name", organization.getOrganization_name())
                    .addParameter("company_name", organization.getCompany_name())
                    .addParameter("organization_description", organization.getOrganization_description())
                    .addParameter("repository_count", organization.getRepository_count())
                    .addParameter("organization_git_url", organization.getOrganization_git_url())
                    .addParameter("organization_github_type", organization.getOrganization_github_type())
                    .executeUpdate();
        }
    }

    @Override
    public String deleteOrganization(UUID uuid) {
        try (Connection conn = sql2o.open()) {
            conn.createQuery("delete from organizations where organization_uuid=:organization_uuid")
                    .addParameter("organization_uuid", uuid)
                    .executeUpdate();
            return "200";
        } catch (Exception e){
            return "400";
        }
    }
}
