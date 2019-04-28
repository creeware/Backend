package email;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import io.github.cdimascio.dotenv.Dotenv;
import model.User;
import org.hibernate.Session;
import util.HibernateUtil;

import javax.ws.rs.core.MediaType;
import java.util.InvalidPropertiesFormatException;
import java.util.UUID;

public abstract class Email {

    public static final String SENDER_MAIL = "noreply@" + Dotenv.load().get("MAILGUN_DOMAIN");

    public Email() {
    }

    public String getUserMail(UUID userUUID){
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = new User();
        try {
            user = session.createQuery("from User where user_uuid=:user_uuid", User.class)
                    .setParameter("user_uuid", userUUID)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            return user.getUser_email();
        }
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
        formData.add("html", body);
        return webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class,
                formData);
    }

    public abstract ClientResponse sendMail();

    public static void main(String[] args) throws InvalidPropertiesFormatException { }

}
