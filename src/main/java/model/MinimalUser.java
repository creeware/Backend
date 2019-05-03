package model;

import com.google.gson.annotations.Expose;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class MinimalUser {

    private String access_token;
    private String jwt_token;

    @Id
    @Expose
    private UUID user_uuid;
    @Expose
    private String user_display_name;
    @Transient
    private String username;
    @Transient
    private String user_email;
    @Transient
    private String user_client;
    @Transient
    private String avatar_url;
    @Transient
    private String profile_url;
    @Transient
    private String user_role;
    @Transient
    private String user_location;
    @Transient
    private String user_bio;
    @Transient
    private Date created_at;
    @Transient
    private Date updated_at;
}