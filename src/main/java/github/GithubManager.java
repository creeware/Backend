package github;


import io.github.cdimascio.dotenv.Dotenv;
import model.Organization;
import model.User;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static util.Directory.deleteDirectory;

public class GithubManager {

    public static void main(String[] args) throws IOException {
    }

    public static String createRepository(String access_token, String org_name, String[] user_names, String admin_user_name, String template_repo_name, String solution_repo_url, Date release_date) throws IOException {
        RepositoryService service = new RepositoryService();
        service.getClient().setOAuth2Token(access_token);
        for (String user_name : user_names) {
            String repo_name = user_name + "-" + UUID.randomUUID().toString();
            RepositoryHook hook = createHook();
            Repository repository = new Repository();
            repository.setName(repo_name);
            repository = service.createRepository(org_name, repository);
            if (new SimpleDateFormat("yyyy-MM-dd").format(release_date).equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))) {
                inviteMember(access_token, repository, user_name);
            }
            clone_and_push(access_token, org_name, repo_name, template_repo_name);
            service.createHook(repository, hook);
            putRepositoryToDb(repository, user_name, admin_user_name, org_name, template_repo_name , solution_repo_url, release_date);
        }
        return "success";
    }

    public static void putRepositoryToDb(Repository repository, String user_name, String admin_user_name, String org_name, String template_repository_name, String solution_repo_url, Date release_date) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = User.getUser(user_name, "GitHubClient");
        User admin = User.getUser(admin_user_name, "GitHubClient");
        Organization organization = Organization.getOrganization(org_name);
        model.Repository newRepository = new model.Repository();
        newRepository.setRepository_uuid(UUID.randomUUID());
        newRepository.setUser_uuid(user.getUser_uuid());
        newRepository.setOrganization_uuid(organization.getOrganization_uuid());
        newRepository.setRepository_name(repository.getName());
        newRepository.setRepository_description(repository.getDescription());
        newRepository.setRepository_visibility(Boolean.toString(!repository.isPrivate()));
        newRepository.setRepository_git_url(repository.getCloneUrl());
        newRepository.setRepository_github_type(repository.getDefaultBranch());
        newRepository.setRepository_type("challenge");
        newRepository.setRepository_status("unsolved");
        newRepository.setSolution_repository_git_url(solution_repo_url);
        newRepository.setRepository_admin_uuid(admin.getUser_uuid());
        newRepository.setUser_name(user.getUsername());
        newRepository.setTemplate_repository_name(template_repository_name);
        newRepository.setOrganization_name(org_name);
        newRepository.setRelease_date(release_date);
        newRepository.setCreated_at(new Date());

        Transaction transaction = session.beginTransaction();
        session.persist(newRepository);
        transaction.commit();

        try {

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }


    public static RepositoryHook createHook() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        RepositoryHook hook = new RepositoryHook();
        hook.setName("web");
        hook.setActive(true);
        Map<String, String> map = new HashMap<String, String>();
        map.put("events", "[*]");
        map.put("url", dotenv.get("SERVER_URL") + "/hook_payload");
        map.put("content_type", "json");
        hook.setConfig(map);
        return hook;
    }


    public static void inviteMember(String access_token, Repository repository, String user_name) throws IOException {
        CollaboratorService c_service = new CollaboratorService();
        c_service.getClient().setOAuth2Token(access_token);
        c_service.addCollaborator(repository, user_name);
    }

    public static void clone_and_push(String access_token, String org_name, String repo_name, String template_repo_name) {
        String remoteUrl = "https://" + access_token + "@github.com/" + org_name + "/" + repo_name + ".git";
        String templateRemoteUrl = "https://" + access_token + "@github.com/" + org_name + "/" + template_repo_name + ".git";
        try {
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(access_token, "");
            File folder = new File(template_repo_name);
            Git git = Git.cloneRepository()
                    .setURI(templateRemoteUrl)
                    .setCloneAllBranches(true)
                    .setDirectory(folder)
                    .call();

            List<Ref> call = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();

            for (Ref ref : call) {
                git.checkout().setForced(true).setName(ref.getName()).call();
                git.push().setRemote(remoteUrl).setRefSpecs(new RefSpec(ref.getName() + ":" + ref.getName().replaceAll("refs/remotes/origin/", "refs/heads/"))).setForce(true).setCredentialsProvider(credentialsProvider).call();
            }

            deleteDirectory(folder);
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public static List<model.Repository> importOrganization(String jws, String organizationName) throws IOException {

        OrganizationService o_service = new OrganizationService();
        RepositoryService r_service = new RepositoryService();
        User user = User.getUser(jws);
        o_service.getClient().setOAuth2Token(user.getAccess_token());
        r_service.getClient().setOAuth2Token(user.getAccess_token());
        org.eclipse.egit.github.core.User organization = o_service.getOrganization(organizationName);
        List<Repository> repositories = r_service.getOrgRepositories(organizationName);
        Organization newOrganization = Organization.createOrganization(user, organization);
        Session session = HibernateUtil.getSessionFactory().openSession();
        for (Repository repository : repositories) {
            putRepositoryToDb(repository, user.getUsername(), user.getUsername(), newOrganization.getOrganization_name(), repository.getName(), repository.getHtmlUrl(), new Date());
        }
        List<model.Repository> dbRepositories = new ArrayList<>();
        try {
             dbRepositories = session.createQuery("from Repository where repository_admin_uuid=:repository_admin_uuid AND organization_uuid=:organization_uuid", model.Repository.class)
                    .setParameter("repository_admin_uuid", user.getUser_uuid())
                     .setParameter("organization_uuid", newOrganization.getOrganization_uuid())
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();

        }
        return dbRepositories;
    }
}
