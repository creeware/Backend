package payload;

import com.google.gson.annotations.Expose;
import lombok.Data;
import util.Validable;

import java.util.Date;
import java.util.UUID;

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
    String canvas_course_uuid;
    String canvas_assignment_uuid;
    String canvas_student_uuid;
    String template_repository_name;
    String organization_name;
    Boolean unlimited;
    String user_name;
    UUID repository_uuid;
    UUID user_uuid;
    UUID organization_uuid;
    UUID repository_admin_uuid;
    Integer try_count;
    Date release_date;
    Date due_date;
    Date repository_submission_date;
    Date created_at;
    Date updated_at;

    public boolean isValid() {
        return repository_name != null && !repository_name.isEmpty()
                && repository_git_url != null && !repository_git_url.isEmpty();
    }
}
