package email;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import io.github.cdimascio.dotenv.Dotenv;
import org.sql2o.Sql2o;
import sql2omodel.Sql2oModel;

import javax.ws.rs.core.MediaType;
import java.util.InvalidPropertiesFormatException;
import java.util.UUID;

public abstract class Email {

    public static final String SENDER_MAIL = "noreply@" + Dotenv.load().get("MAILGUN_DOMAIN");
    private final Sql2oModel sql;

    public Email() {
        String host = Dotenv.load().get("JDBC_DATABASE_URL", "localhost");
        String user = Dotenv.load().get("JDBC_DATABASE_USERNAME", "root");
        String pass = Dotenv.load().get("JDBC_DATABASE_PASSWORD", "");
        this.sql = new Sql2oModel(new Sql2o(
                host,
                user,
                pass
        ));
    }

    public String getUserMail(UUID user) throws InvalidPropertiesFormatException {
        if (!(sql.existUser(user) && sql.getUser(user).isPresent())) {
            throw new InvalidPropertiesFormatException(user.toString() + " does not exist.");
        }
        return sql.getUser(user).get().getUser_email();
    }

    protected ClientResponse sendMail(String recipient, String subject, String body) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("api", dotenv.get("MAILGUN_API_KEY")));
        WebResource webResource = client.resource("https://api.mailgun.net/v3/" + dotenv.get("MAILGUN_DOMAIN")
                + "/messages");
        MultivaluedMapImpl formData = new MultivaluedMapImpl();
        formData.add("from", "Creeware <" + SENDER_MAIL + ">");
        formData.add("to", recipient);
        formData.add("subject", subject);
        formData.add("text", body);
        return webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class,
                formData);
    }

    public abstract ClientResponse sendMail();

    public static void main(String[] args) throws InvalidPropertiesFormatException {
        Email email = new Email() {
            @Override
            public ClientResponse sendMail() {
                return null;
            }
        };

        email.sendMail("poasp@kth.se", "Testlel",
                "<div class='cool'>" +
                "<b>Test</b>" +
                "</div>");

        UUID user = email.sql.createUser("Pontus Asp",
                "pontusasp",
                "poasp@kth.se",
                "pontusasp",
                "https://avatars2.githubusercontent.com/u/18573650?s=460&v=4",
                "https://github.com/pontusasp",
                "student",
                "Sweden");

        String mail = email.getUserMail(user);
        email.sql.deleteUser(user);

        System.out.println(mail);
    }

}
