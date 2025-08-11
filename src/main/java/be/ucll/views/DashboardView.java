package be.ucll.views;

import be.ucll.dto.SearchCriteriaDTO;
import be.ucll.entities.Order;
import be.ucll.services.EmailQueueProducerService;
import be.ucll.services.OrderService;
import be.ucll.services.ProductService;
import be.ucll.util.AppLayoutTemplate;
import be.ucll.util.AppRoutes;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
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
    private ProductService productService;

    @Autowired
    private EmailQueueProducerService jmsEmailService;

    private SearchCriteriaDTO searchCriteriaDTO = new SearchCriteriaDTO();
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
        Select<String> deliveredSelect = new Select<>();
        deliveredSelect.setLabel("Afgeleverd");
        deliveredSelect.setItems("Alle", "Ja", "Nee");
        deliveredSelect.setValue("Alle");

        ComboBox<String> productName = new ComboBox<>("Product name");
        productName.setAllowCustomValue(true);


        //TODO: retry pagination instead of calling full dataset
        productName.setItems(query -> {
            String filter = query.getFilter().orElse("");
            return productService.autocompleteProductNames(filter)
                    .stream()
                    .skip(query.getOffset())
                    .limit(query.getLimit());
        });




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
                .withConverter(
                        doubleValue -> doubleValue == null ? null : doubleValue.intValue(),
                        intValue -> intValue == null ? null : intValue.doubleValue(),
                        "Het aantal moet een nummer zijn"
                )
                .withValidator(val -> val == null || val >= 0, "Aantal moet positief zijn")
                .bind(SearchCriteriaDTO::getProductCount, SearchCriteriaDTO::setProductCount);

        binder.forField(deliveredSelect)
                .withConverter(
                        value -> {
                            if ("Ja".equals(value)) return Boolean.TRUE;
                            if ("Nee".equals(value)) return Boolean.FALSE;
                            return null;
                        },
                        boolValue -> {
                            if (boolValue == null) return "Alle";
                            return boolValue ? "Ja" : "Nee";
                        }
                )
                .bind(SearchCriteriaDTO::isDeliveredNullable, SearchCriteriaDTO::setDeliveredNullable);

        binder.forField(productName)
                .bind(SearchCriteriaDTO::getProductName, SearchCriteriaDTO::setProductName);

        binder.forField(email)
                .bind(SearchCriteriaDTO::getEmail, SearchCriteriaDTO::setEmail);


        Button clearButton = new Button("Wissen", event -> {
            binder.readBean(new SearchCriteriaDTO());
            orderGrid.setItems(Collections.emptyList());
            errorLabel.setText("");
        });

        Button searchButton = new Button("Zoeken", event -> {
            SearchCriteriaDTO tempCriteria = new SearchCriteriaDTO();

            if (binder.writeBeanIfValid(tempCriteria)) {
                if (!hasAtLeastOneCriteria(tempCriteria)) {
                    errorLabel.setText("Geef ten minste één zoekcriteria op.");
                    return;
                }

                this.searchCriteriaDTO = tempCriteria;
                List<Order> results = orderService.searchOrders(searchCriteriaDTO);

                if (results.isEmpty()) {
                    Notification.show("Geen resultaten gevonden.", 3000, Notification.Position.MIDDLE);
                } else {
                    Notification.show(results.size() + " resultaten gevonden.", 3000, Notification.Position.MIDDLE);
                }

                orderGrid.setItems(results);
                errorLabel.setText("");
            } else {
                errorLabel.setText("Vul de velden correct in.");
            }
        });


        FormLayout formLayout = new FormLayout(
                minAmount, maxAmount, productCount, deliveredSelect,
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

            if (currentOrders.isEmpty()) {
                Notification.show("Geen resultaten om te verzenden.", 3000, Notification.Position.MIDDLE);
                return;
            }

            String emailAddress = searchCriteriaDTO.getEmail();
            if (emailAddress == null || emailAddress.isBlank() || !emailAddress.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                Notification.show("Voer een geldig e-mailadres in", 3000, Notification.Position.MIDDLE);
                return;
            }

            jmsEmailService.sendOrderSummaryEmail(emailAddress, currentOrders);
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

    private boolean hasAtLeastOneCriteria(SearchCriteriaDTO criteria) {
        if (criteria.getMinAmount() != null ||
                criteria.getMaxAmount() != null ||
                criteria.getProductCount() != null) {
            return true;
        }

        String productName = criteria.getProductName();
        if (productName != null && !productName.isBlank()) {
            return true;
        }

        String email = criteria.getEmail();
        if (email != null && !email.isBlank()) {
            return true;
        }

        if (criteria.isDeliveredNullable() != null) {
            return true;
        }

        return false;
    }
}
