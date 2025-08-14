package be.ucll.views;

import be.ucll.components.orderdetail.OrderDetailUi;
import be.ucll.entities.Order;
import be.ucll.services.OrderService;
import be.ucll.util.AppLayoutTemplate;
import be.ucll.util.AppRoutes;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(AppRoutes.ORDER_VIEW)
@PageTitle("Order Details")
@RolesAllowed("USER")
public class OrderDetailView extends AppLayoutTemplate implements BeforeEnterObserver {

    private static final Logger log = Logger.getLogger(OrderDetailView.class);

    @Autowired
    private OrderService orderService;

    private OrderDetailUi orderDetailUi;

    public OrderDetailView() {
        log.info("OrderDetailView initialized");
    }

    @PostConstruct
    private void init() {
        setBody(buildDetailLayout());
        initEventListeners();
    }

    private VerticalLayout buildDetailLayout() {
        orderDetailUi = new OrderDetailUi();

        VerticalLayout layout = new VerticalLayout(orderDetailUi);
        layout.setSizeFull();
        return layout;
    }

    private void initEventListeners() {
        orderDetailUi.addBackListener(event ->
                getUI().ifPresent(ui -> ui.navigate(AppRoutes.DASHBOARD_VIEW))
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> maybeId = event.getRouteParameters().get("id").map(Long::valueOf);
        if (maybeId.isEmpty()) {
            log.warn("OrderDetailView accessed without ID redirect to dashboard");
            event.forwardTo(AppRoutes.DASHBOARD_VIEW);
            return;
        }

        Optional<Order> orderOpt = orderService.getOrderWithProducts(maybeId.get());
        if (orderOpt.isEmpty()) {
            log.warnf("Order with ID %d not found redirect to dashboard", maybeId.get());
            event.forwardTo(AppRoutes.DASHBOARD_VIEW);
            return;
        }

        log.infof("Loaded OrderDetailView for Order ID: %d", orderOpt.get().getId());
        orderDetailUi.setOrderDetails(orderOpt.get());
    }
}
