package be.ucll.components.dashboard;

import be.ucll.entities.Order;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.shared.Registration;

import java.util.List;

public class EmailButton extends Button {

    private List<Order> currentOrders;
    private String emailAddress;

    public EmailButton() {
        super("Stuur Email");

        addClickListener(event -> {
            if (currentOrders == null || currentOrders.isEmpty()) {
                Notification.show("Geen resultaten om te verzenden.", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (emailAddress == null || emailAddress.isBlank() || !emailAddress.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                Notification.show("Voer een geldig e-mailadres in", 3000, Notification.Position.MIDDLE);
                return;
            }

            fireEvent(new SendEmailEvent(this, emailAddress, currentOrders));
            Notification.show("Email wordt verzonden...", 3000, Notification.Position.MIDDLE);
        });
    }

    public void setOrders(List<Order> orders) {
        this.currentOrders = orders;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public static class SendEmailEvent extends ComponentEvent<EmailButton> {
        private final String email;
        private final List<Order> orders;

        public SendEmailEvent(EmailButton source, String email, List<Order> orders) {
            super(source, false);
            this.email = email;
            this.orders = orders;
        }

        public String getEmail() { return email; }
        public List<Order> getOrders() { return orders; }
    }

    public Registration addSendEmailListener(com.vaadin.flow.component.ComponentEventListener<SendEmailEvent> listener) {
        return addListener(SendEmailEvent.class, listener);
    }
}
