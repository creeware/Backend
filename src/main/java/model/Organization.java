package model;

import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Data
public class Organization {
    UUID organization_uuid;
    UUID user_uuid;
    String organization_name;
    String organization_description;
    int repository_count;
    String organization_git_url;
    String organization_github_type;
    Date created_at;
    Date updated_at;
}
