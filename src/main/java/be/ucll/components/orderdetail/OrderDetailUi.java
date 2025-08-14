package be.ucll.components.orderdetail;

import be.ucll.entities.Order;
import be.ucll.entities.Product;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

public class OrderDetailUi extends VerticalLayout {

    private final Span customerNumber = new Span();
    private final Span deliveredStatus = new Span();
    private final Span totalPrice = new Span();
    private final Grid<Product> productGrid = new Grid<>(Product.class);
    private final Button backButton = new Button("Terug");

    public OrderDetailUi() {
        setSizeFull();
        setSpacing(true);

        H2 title = new H2("Order Details");

        productGrid.removeAllColumns();
        productGrid.addColumn(Product::getId).setHeader("ID");
        productGrid.addColumn(Product::getName).setHeader("Name");
        productGrid.addColumn(Product::getDescription).setHeader("Omschrijving");
        productGrid.addColumn(Product::getPrice).setHeader("Price");

        backButton.addClickListener(e -> fireEvent(new BackEvent(this)));

        add(title, customerNumber, deliveredStatus, totalPrice, productGrid, backButton);
    }

    public void setOrderDetails(Order order) {
        customerNumber.setText(order.getCustomerNumber());
        deliveredStatus.setText("Afgeleverd: " + (order.isDelivered() ? "Ja" : "Nee"));
        totalPrice.setText("Totale prijs: €" + order.getTotalPrice());
        productGrid.setItems(order.getProducts());
    }

    //Events
    public static class BackEvent extends ComponentEvent<OrderDetailUi> {
        public BackEvent(OrderDetailUi source) {
            super(source, false);
        }
    }

    public Registration addBackListener(ComponentEventListener<BackEvent> listener) {
        return addListener(BackEvent.class, listener);
    }
}
