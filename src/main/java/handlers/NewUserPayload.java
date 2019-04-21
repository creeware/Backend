package handlers;

import lombok.Data;
import util.Validable;

@Data
public class NewUserPayload implements Validable {
    String user_display_name;
    String user_name;
    String avatar_url;
    String profile_url;
    String user_role;
    String user_location;

    public boolean isValid() {
        return user_display_name != null && !user_display_name.isEmpty() && profile_url != null && !profile_url.isEmpty();
    }
}
