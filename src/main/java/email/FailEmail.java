package email;

import com.sun.jersey.api.client.ClientResponse;

import java.util.InvalidPropertiesFormatException;
import java.util.UUID;

public class FailEmail extends Email {
    private String to, subject;
    private String body = "";

    private String subjectTemplate = "Your submission %s has been graded.";
    private String bodyTemplate =
            "<html>" +
            "<head> <style>\n" +
            ".row {  \n" +
            "  background-color: #fff;\n" +
            "  font-family: monospace, monospace;\n" +
            "}\n" +
            "\n" +
            ".add {\n" +
            "background-color: #4f4; \n" +
            "}\n" +
            "\n" +
            ".rem {\n" +
            "background-color: #f55;  \n" +
            "}\n" +
            "</style> </head>" +
            "You have failed %s, your answer does not match the solution.\n" +
            "\n" +
            "The differences are:\n" +
            "<div style=\"width: 800px\">\n" +
            "%s" +
            "</div>" + "</html>";

    public FailEmail(UUID recipient, String task, String rows) throws InvalidPropertiesFormatException {
        super();
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
            sb.append(s).append("</div>");
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
