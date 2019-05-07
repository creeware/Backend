package email;

import com.sun.jersey.api.client.ClientResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class RepositoryEmail extends Email {
    private String to, subject;
    private String body = "";

    private String subjectTemplate = "Subscribed to %s.";
    private String bodyTemplate;

    public RepositoryEmail(UUID recipient, String repository, String organization) {
        super();
        String combinedRepo = organization.isEmpty()? repository : organization + '/' + repository;
        File bodyTemplateFile = new File("src/main/java/email/InviteEmailTemplate.html");
        try {
            FileInputStream fis = new FileInputStream(bodyTemplateFile);
            byte[] charBytes = new byte[(int) bodyTemplateFile.length()];
            fis.read(charBytes);
            fis.close();

            bodyTemplate = new String(charBytes);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("TEMPLATE NOT FOUND!\nRAW OUTPUT WILL BE USED");
            bodyTemplate = "Hello %s, you have been subscribed to the repository <a href='https://github.com/%s'>%s</a>.";
        }
        to = getUserMail(recipient);
        subject = String.format(subjectTemplate, combinedRepo);
        body = String.format(bodyTemplate, getUserDisplayName(recipient), combinedRepo, combinedRepo);
    }

    @Override
    public ClientResponse sendMail() {
        return sendMail(to, subject, body);
    }

}
