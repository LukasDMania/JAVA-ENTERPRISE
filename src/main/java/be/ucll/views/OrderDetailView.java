package be.ucll.views;


import be.ucll.entities.Order;
import be.ucll.entities.Product;
import be.ucll.services.OrderService;
import be.ucll.util.AppLayoutTemplate;
import be.ucll.util.AppRoutes;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(AppRoutes.ORDER_VIEW)
@PageTitle("Order Details")
@PermitAll
public class OrderDetailView extends AppLayoutTemplate implements BeforeEnterObserver {

    @Autowired
    private OrderService orderService;

    private Long orderId;
    private Order currentOrder;

    private final Span customerNumber = new Span();
    private final Span deliveredStatus = new Span();
    private final Span totalPrice = new Span();
    private final Grid<Product> productGrid = new Grid<>(Product.class);

    public OrderDetailView() {
        setBody(buildDetailLayout());
    }

    private VerticalLayout buildDetailLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);

        H2 title = new H2("Order Details");

        productGrid.removeAllColumns();
        productGrid.addColumn(Product::getId).setHeader("ID");
        productGrid.addColumn(Product::getName).setHeader("Name");
        productGrid.addColumn(Product::getDescription).setHeader("Omschrijving");
        productGrid.addColumn(Product::getPrice).setHeader("Price");

        Button backButton = new Button("Terug", e ->
                getUI().ifPresent(ui -> ui.navigate(AppRoutes.DASHBOARD_VIEW))
        );

        layout.add(title, customerNumber, deliveredStatus, totalPrice, productGrid, backButton);
        return layout;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> maybeId = event.getRouteParameters().get("id").map(Long::valueOf);
        if (maybeId.isEmpty()) {
            event.forwardTo(AppRoutes.DASHBOARD_VIEW);
            return;
        }

        Optional<Order> orderOpt = orderService.getOrderWithProducts(maybeId.get());
        if (orderOpt.isEmpty()) {
            event.forwardTo(AppRoutes.DASHBOARD_VIEW);
            return;
        }

        currentOrder = orderOpt.get();
        populateOrderDetails();
    }

    private void  populateOrderDetails() {
        customerNumber.setText(currentOrder.getCustomerNumber());
        deliveredStatus.setText("Afgeleverd: " + (currentOrder.isDelivered() ? "Ja" : "Nee"));
        totalPrice.setText("Totale prijs: €" + currentOrder.getTotalPrice().toString());

        productGrid.setItems(currentOrder.getProducts());
    }
}
