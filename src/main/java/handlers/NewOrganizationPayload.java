package handlers;

import lombok.Data;
import util.Validable;

import java.util.UUID;

@Data
public class NewOrganizationPayload implements Validable {
    UUID user_uuid;
    String organization_name;
    String organization_description;
    String company_name;
    int repository_count;
    String organization_git_url;
    String organization_github_type;

    public boolean isValid() {
        return organization_name != null && !organization_name.isEmpty()
                && organization_git_url != null && !organization_git_url.isEmpty();
    }
}
