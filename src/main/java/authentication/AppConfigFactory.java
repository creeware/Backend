package authentication;

import io.github.cdimascio.dotenv.Dotenv;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.http.ajax.DefaultAjaxRequestResolver;
import org.pac4j.core.http.callback.QueryParameterCallbackUrlResolver;
import org.pac4j.core.http.url.DefaultUrlResolver;
import org.pac4j.core.matching.PathMatcher;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.sparkjava.DefaultHttpActionAdapter;

public class AppConfigFactory {

    public static Config build(final Object... parameters) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        GitHubClient gitHubClient = new GitHubClient(dotenv.get("GITHUB_OAUTH_KEY"), dotenv.get("GITHUB_OAUTH_SECRET"));
        gitHubClient.setScope("user, user:email,repo,admin:org,admin:org_hook,delete_repo");
        
        final Clients clients = new Clients(dotenv.get("GITHUB_OAUTH_CALLBACK_URL"), gitHubClient);

        clients.setUrlResolver(new DefaultUrlResolver());
        clients.setCallbackUrlResolver(new QueryParameterCallbackUrlResolver());
        clients.setAjaxRequestResolver(new DefaultAjaxRequestResolver());

        final Config config = new Config(clients);

        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
        config.addMatcher("excludedPath", new PathMatcher().excludeRegex("^/*/notprotected$"));
        config.setHttpActionAdapter(new DefaultHttpActionAdapter());

        return config;
    }
}
