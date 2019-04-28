package payload;

import lombok.Data;
import util.Validable;

@Data
public class NewRepositoryPayload implements Validable {
    String repository_name;
    String repository_description;
    String repository_visibility;
    String repository_git_url;
    String repository_github_type;
    String repository_type;
    String repository_status;
    String solution_repository_git_url;

    public boolean isValid() {
        return repository_name != null && !repository_name.isEmpty()
                && repository_git_url != null && !repository_git_url.isEmpty();
    }
}
