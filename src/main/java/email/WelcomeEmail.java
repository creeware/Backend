package email;


// using SendGrid's Java Library
// https://github.com/sendgrid/sendgrid-java

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import io.github.cdimascio.dotenv.Dotenv;

import javax.ws.rs.core.MediaType;


public class WelcomeEmail {

    public static ClientResponse main(String recipient) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("api", dotenv.get("MAILGUN_API_KEY")));
        WebResource webResource = client.resource("https://api.mailgun.net/v3/" + dotenv.get("MAILGUN_DOMAIN")
                + "/messages");
        MultivaluedMapImpl formData = new MultivaluedMapImpl();
        formData.add("from", "Information <noreply@" + dotenv.get("MAILGUN_DOMAIN") + ">");
        formData.add("to", recipient);
        formData.add("subject", "Welcome aboard!");
        formData.add("text", "Welcome to the GitGrounds! Have Fun! :)");
        return webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class,
                formData);

    }
}


