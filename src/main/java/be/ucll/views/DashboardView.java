package be.ucll.views;

import be.ucll.dto.SearchCriteriaDTO;
import be.ucll.entities.Order;
import be.ucll.services.JmsEmailService;
import be.ucll.services.OrderService;
import be.ucll.util.AppLayoutTemplate;
import be.ucll.util.AppRoutes;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Route(AppRoutes.DASHBOARD_VIEW)
@PageTitle("Order Dashboard")
@PermitAll
public class DashboardView extends AppLayoutTemplate {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JmsEmailService jmsEmailService;

    private final SearchCriteriaDTO searchCriteriaDTO = new SearchCriteriaDTO();
    private final Binder<SearchCriteriaDTO> binder = new Binder<>(SearchCriteriaDTO.class);

    private final Grid<Order> orderGrid = new Grid<>();

    public DashboardView() {
        setBody(buildDashboardLayout());
    }

    private VerticalLayout buildDashboardLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(true);

        layout.add(buildSearchForm(), buildOrderGrid(), buildEmailButton());
        return layout;
    }

    private Component buildSearchForm() {
        NumberField minAmount = new NumberField("Minimum bedrag");
        NumberField maxAmount = new NumberField("Maximum bedrag");
        NumberField productCount = new NumberField("Aantal producten");
        Checkbox delivered = new Checkbox("Afgeleverd");
        TextField productName = new TextField("Product naam");
        EmailField email = new EmailField("Email adres");

        Span errorLabel = new Span();
        errorLabel.getStyle().set("color", "red");


        binder.forField(minAmount)
                .withConverter(
                        doubleValue -> doubleValue == null ? null : BigDecimal.valueOf(doubleValue),
                        bigDecimalValue -> bigDecimalValue == null ? null : bigDecimalValue.doubleValue(),
                        "Het bedrag moet een nummer zijn"
                )
                .withValidator(val -> val == null || val.compareTo(BigDecimal.ZERO) >= 0, "Minimum bedrag moet positief zijn")
                .bind(SearchCriteriaDTO::getMinAmount, SearchCriteriaDTO::setMinAmount);

        binder.forField(maxAmount)
                .withConverter(
                        doubleValue -> doubleValue == null ? null : BigDecimal.valueOf(doubleValue),
                        bigDecimalValue -> bigDecimalValue == null ? null : bigDecimalValue.doubleValue(),
                        "Het bedrag moet een nummer zijn")
                .withValidator(val -> val == null || val.compareTo(BigDecimal.ZERO) >= 0, "Maximaal bedrag moet positief zijn")
                .bind(SearchCriteriaDTO::getMaxAmount, SearchCriteriaDTO::setMaxAmount);

        binder.forField(productCount)
                .withConverter(Double::intValue, Integer::doubleValue)
                .withValidator(val -> val == null || val >= 0, "Aantal moet positief zijn")
                .bind(SearchCriteriaDTO::getProductCount, SearchCriteriaDTO::setProductCount);

        binder.forField(delivered)
                .bind(SearchCriteriaDTO::isDelivered, SearchCriteriaDTO::setDelivered);

        binder.forField(productName)
                .bind(SearchCriteriaDTO::getProductName, SearchCriteriaDTO::setProductName);

        binder.forField(email)
                .withValidator(value -> value == null || value.matches("^[A-Za-z]+@[A-Za-z]+\\.[A-Za-z]{2,}$"),
                        "Ongeldig e-mailadres")
                .bind(SearchCriteriaDTO::getEmail, SearchCriteriaDTO::setEmail);

        Button clearButton = new Button("Wissen", event -> {
            binder.readBean(new SearchCriteriaDTO());
            orderGrid.setItems(Collections.emptyList());
            errorLabel.setText("");
        });

        Button searchButton = new Button("Zoeken", event -> {
            if (!hasAtLeastOneCriteria()) {
                errorLabel.setText("Geef ten minste één zoekcriteria op.");
                return;
            }
            if (binder.writeBeanIfValid(searchCriteriaDTO)) {
                List<Order> results = orderService.searchOrders(searchCriteriaDTO); //TODO: implement in serv
                orderGrid.setItems(results);
                errorLabel.setText("");
            } else {
                errorLabel.setText("Vul de velden correct in.");
            }
        });

        FormLayout formLayout = new FormLayout(
                minAmount, maxAmount, productCount, delivered,
                productName, email, clearButton, searchButton
        );
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        VerticalLayout wrapper = new VerticalLayout(formLayout, errorLabel);
        wrapper.setAlignItems(Alignment.START);
        return wrapper;
    }

    private Component buildEmailButton() {
        Button emailButton = new Button("Stuur Email", event -> {
            List<Order> currentOrders = orderGrid.getListDataView().getItems().toList();
            if (currentOrders.isEmpty() || searchCriteriaDTO.getEmail() == null) {
                Notification.show("Geen resultaten of e-mail om te verzenden.", 3000, Notification.Position.MIDDLE);
                return;
            }

            jmsEmailService.sendOrderSummaryEmail(searchCriteriaDTO.getEmail(), currentOrders);

            Notification.show("Email wordt verzonden...", 3000, Notification.Position.MIDDLE);
        });
        return emailButton;
    }

    private Component buildOrderGrid() {
        orderGrid.removeAllColumns();

        orderGrid.addColumn(Order::getId).setHeader("Bestelling Id");
        orderGrid.addColumn(Order::getCustomerNumber).setHeader("Klanten nr");
        orderGrid.addColumn(order ->  order.getProducts().size()).setHeader("Aantal Produkten");
        orderGrid.addColumn(Order::isDelivered).setHeader("Afgeleverd?");
        orderGrid.addColumn(Order::getTotalPrice).setHeader("Totaal");

        orderGrid.addComponentColumn(order -> {
            Button detailButton = new Button("Details", event -> {
                getUI().ifPresent(ui -> ui.navigate("order/" + order.getId()));
            });
            return detailButton;
        }).setHeader("Details");

        orderGrid.setWidthFull();
        orderGrid.setItems(Collections.emptyList());

        return orderGrid;
    }

    private boolean hasAtLeastOneCriteria() {
        if (searchCriteriaDTO.getMinAmount() != null ||
                 searchCriteriaDTO.getMaxAmount() != null ||
                searchCriteriaDTO.getProductCount() != null) {
            return true;
        }

        String productName = searchCriteriaDTO.getProductName();
        if (productName != null && !productName.isBlank()) {
            return true;
        }

        String email = searchCriteriaDTO.getEmail();
        if (email != null && !email.isBlank()) {
            return true;
        }

        return false;
    }
}
