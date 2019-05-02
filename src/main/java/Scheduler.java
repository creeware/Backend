import github.GithubManager;
import model.Organization;
import model.Repository;
import model.User;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scheduler {
    public static void main(String[] args) throws Exception {
        List<User> users = User.getUsers();
        for(User user:users){
            List<Repository> repositories = Repository.getAdminUserRepositoriesForToday(user.getUser_uuid());
            RepositoryService service = new RepositoryService();
            service.getClient().setOAuth2Token(user.getAccess_token());
            for(Repository repository:repositories){
                Organization organization = Organization.getOrganization(repository.getOrganization_uuid());
                org.eclipse.egit.github.core.Repository githubRepository = service.getRepository(organization.getOrganization_name(), repository.getRepository_name());
                GithubManager.inviteMember(user.getAccess_token(), githubRepository, repository.getUser_name());
                repository.setRepository_status("released");
                Repository.updateRepository(repository);
            }
        }
    }
}
