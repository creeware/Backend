package model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Model {
    UUID createUser(String user_display_name,
            String username,
            String user_email,
            String user_client,
            String avatar_url,
            String profile_url,
            String user_role,
            String user_location);
    //UUID createRepository(UUID post, String author, String content);
    //UUID createOrganization(UUID post, String author, String content);

    List<User> getAllUsers();
    //List<Repository> getAllRepositories();
    //List<Organization> getAllOrganizations();

    //List<Repository> getAllUserRepositories(UUID user);
    //List<Repository> getAllOrganizationRepositories(UUID organization);

    //List<Organization> getAllUserOrganizations(UUID user);


    boolean existUser(UUID user);
    boolean existUserByNameAndClient(String username, String user_client);
    //boolean existRepository(UUID repository);
    //boolean existOrganization(UUID organization);

    Optional<User> getUser(UUID uuid);
    Optional<User> getUserByNameAndClient(String username, String user_client);
    //Optional<Repository> getRepository(UUID uuid);
    //Optional<Organization> getOrganization(UUID uuid);

    void updateUser(User user);
    //void updateRepository(Repository repository);
    //void updateOrganization(Organization organization);

    void deleteUser(UUID uuid);
    // void deleteRepository(UUID uuid);
    // void deleteOrganization(UUID uuid);
}
