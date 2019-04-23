package email;

import com.sun.jersey.api.client.ClientResponse;

import java.util.InvalidPropertiesFormatException;
import java.util.UUID;

public class SuccessEmail extends Email {

    private String to;
    private String subject = "";
    private String body = "";

    public SuccessEmail(UUID recipient, String task) {
        try {
            to = getUserMail(recipient);
            subject = String.format("Your submission %s has been graded.", task);
            body = String.format("You passed %s.", task);
        } catch (InvalidPropertiesFormatException e) {
            e.printStackTrace();
        }
    }


    @Override
    public ClientResponse sendMail() {
        return sendMail(to, subject, body);
    }
}
