package be.ucll.services;

import be.ucll.entities.Order;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderSummary(String email, List<Order> orders) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        System.out.println("Sending email to " + email);
        helper.setTo(email);
        helper.setSubject("Overzicht Bestellingen JAVA EE");

        helper.setText(buildEmailHtml(orders), true);

        mailSender.send(mimeMessage);
        System.out.println("Email sent to " + email + " successfully.");
    }

    private String buildEmailHtml(List<Order> orders) {
        StringBuilder html = new StringBuilder();
        html.append("<p>Beste gebruiker, hieronder het gevraagde overzicht van de bestellingen via email:</p>");
        html.append("<table border='1' cellpadding='5' cellspacing='0'>")
                .append("<tr>")
                .append("<th>BestelId</th>")
                .append("<th>Klantnr</th>")
                .append("<th># Producten</th>")
                .append("<th>Afgeleverd</th>")
                .append("<th>Totaal</th>")
                .append("</tr>");

        for (Order order : orders) {
            html.append("<tr>")
                    .append("<td>").append(order.getId()).append("</td>")
                    .append("<td>").append(order.getCustomerNumber()).append("</td>")
                    .append("<td>").append(order.getProducts().size()).append("</td>")
                    .append("<td>").append(order.isDelivered() ? "Ja" : "Nee").append("</td>")
                    .append("<td>").append(order.getTotalPrice()).append("</td>")
                    .append("</tr>");
        }
        html.append("</table>");

        return html.toString();
    }
}
