package email;

import com.sun.jersey.api.client.ClientResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.UUID;

public class InviteEmail extends Email {
    private String to, subject;
    private String body = "";

    private String subjectTemplate = "Invitation to %s.";
    private String bodyTemplate;

    public InviteEmail(String email) {
        super();
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
            bodyTemplate = "Hi! You have been invited to Creeware :) <a href='https://creeware.com'>creeware.com</a>.";
        }
        to = email;
        subject = String.format(subjectTemplate, "Creeware");
        body = String.format(bodyTemplate);
    }

    @Override
    public ClientResponse sendMail() {
        return sendMail(to, subject, body);
    }

}
