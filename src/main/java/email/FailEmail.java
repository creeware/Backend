package email;

import com.sun.jersey.api.client.ClientResponse;

import java.io.*;
import java.util.InvalidPropertiesFormatException;
import java.util.Scanner;
import java.util.UUID;

public class FailEmail extends Email {
    private String to, subject;
    private String body = "";

    private String subjectTemplate = "Your submission %s has been graded.";
    private String bodyTemplate;

    public FailEmail(UUID recipient, String task, String rows) throws InvalidPropertiesFormatException {
        super();
        File bodyTemplateFile = new File("src/main/java/email/FailEmailTemplate.html");
        try {
            FileInputStream fis = new FileInputStream(bodyTemplateFile);
            byte[] charBytes = new byte[(int) bodyTemplateFile.length()];
            fis.read(charBytes);
            fis.close();

            bodyTemplate = new String(charBytes);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("TEMPLATE NOT FOUND!\nRAW OUTPUT WILL BE USED");
            bodyTemplate = "Fail.<br>Task: %s<br><br>Output:<br>%s";
        }
        to = getUserMail(recipient);
        subject = String.format(subjectTemplate, task);
        body = String.format(bodyTemplate, task, markLines(rows));
    }

    private static String markLines(String textraw) {
        String[] rows = textraw.split("\n");

        StringBuilder sb = new StringBuilder();

        for (int it = 0; it < rows.length; it++) {
            String s = rows[it];
            switch (s.charAt(0)) {
                case '+':
                    sb.append("<div class=\"row add\">");
                    break;
                case '-':
                    sb.append("<div class=\"row rem\">");
                    break;
                default:
                    sb.append("<div class=\"row\">");
            }
            sb.append(s.replaceAll("&", "&amp;").replaceAll("<", "&lt;")).append("</div>");
        }
        return sb.toString();
    }

    @Override
    public ClientResponse sendMail() {
        return sendMail(to, subject, body);
    }

    public static void main(String[] args) {
        System.out.println(markLines("  asfasjdfnajksfnjaew"));
    }

}
