package be.ucll.components.dashboard;

import be.ucll.entities.Order;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;

import java.util.Collections;

public class OrderGrid extends Grid<Order> {

    public OrderGrid() {
        removeAllColumns();

        addColumn(Order::getId).setHeader("Bestelling Id");
        addColumn(Order::getCustomerNumber).setHeader("Klanten nr");
        addColumn(order -> order.getProducts().size()).setHeader("Aantal Produkten");
        addColumn(Order::isDelivered).setHeader("Afgeleverd?");
        addColumn(Order::getTotalPrice).setHeader("Totaal");

        addComponentColumn(order -> {
            Button detailButton = new Button("Details", event -> {
                getUI().ifPresent(ui -> ui.navigate("order/" + order.getId()));
            });
            return detailButton;
        }).setHeader("Details");

        setWidthFull();
        setItems(Collections.emptyList());
    }
}
