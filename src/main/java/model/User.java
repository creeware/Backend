package model;

import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Data
public class User {
    UUID user_uuid;
    String user_display_name;
    String username;
    String user_email;
    String user_client;
    String avatar_url;
    String profile_url;
    String user_role;
    String user_location;
    Date created_at;
    Date updated_at;
}
