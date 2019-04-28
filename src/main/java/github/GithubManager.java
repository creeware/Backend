package github;



import io.github.cdimascio.dotenv.Dotenv;
import model.Model;
import model.Organization;
import model.User;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.sql2o.Sql2o;
import sql2omodel.Sql2oModel;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class GithubManager {

    public static void main (String[] args) throws IOException {}

    public static String createRepository(String access_token, String org_name, String[] user_names, String template_repo_name, String solution_repo_url) throws IOException{
        System.out.println(org_name);
        RepositoryService service = new RepositoryService();
        service.getClient().setOAuth2Token(access_token);
        for(String user_name:user_names){
            String repo_name = user_name + "-" + UUID.randomUUID().toString();
            RepositoryHook hook = createHook();
            Repository repository = new Repository();
            repository.setName(repo_name);
            repository = service.createRepository(org_name, repository);
            service.createHook(repository, hook);
            inviteMember(access_token, repository, user_name);
            clone_and_push(access_token, org_name, repo_name, template_repo_name);
            putRepositoryToDb(repository, user_name, org_name, solution_repo_url);
        }
        return "success";
    }

    public static void putRepositoryToDb(Repository repository, String user_name, String org_name, String solution_repo_url){
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        Sql2o sql2o = new Sql2o(dotenv.get("JDBC_DATABASE_URL"), dotenv.get("JDBC_DATABASE_USERNAME"), dotenv.get("JDBC_DATABASE_PASSWORD"));
        Model model = new Sql2oModel(sql2o);
        Optional<User> user = model.getUserByNameAndClient(user_name, "GitHubClient");
        Optional<Organization> organization = model.getOrganizationbyName(org_name);
        model.createRepository(
                user.get().getUser_uuid(),
                organization.get().getOrganization_uuid(),
                repository.getName(),
                repository.getDescription(),
                Boolean.toString(repository.isPrivate()),
                repository.getCloneUrl(),
                repository.getDefaultBranch(),
                "challenge",
                "unsolved",
                solution_repo_url
        );
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


    public static void inviteMember(String access_token, Repository repository, String user_name) throws IOException{
        CollaboratorService c_service = new CollaboratorService();
        c_service.getClient().setOAuth2Token(access_token);
        c_service.addCollaborator(repository, user_name);
    }

    public static void clone_and_push(String access_token, String org_name, String repo_name, String template_repo_name) {
        String remoteUrl = "https://" +access_token + "@github.com/" + org_name +"/" + repo_name + ".git";
        String templateRemoteUrl = "https://" +access_token + "@github.com/" + org_name +"/" + template_repo_name + ".git";
        try {
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider( access_token, "" );
            File folder = new File(template_repo_name);
            Git git = Git.cloneRepository()
                    .setURI( templateRemoteUrl )
                    .setCloneAllBranches(true)
                    .setDirectory(folder)
                    .call();

            List<Ref> call = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();

            for (Ref ref : call) {
                git.checkout().setForced(true).setName(ref.getName()).call();
                git.push().setRemote( remoteUrl ).setRefSpecs(new RefSpec(ref.getName() + ":" + ref.getName().replaceAll("refs/remotes/origin/", "refs/heads/"))).setForce(true).setCredentialsProvider( credentialsProvider ).call();
            }

            deleteDir(folder);
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    private static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }
}
