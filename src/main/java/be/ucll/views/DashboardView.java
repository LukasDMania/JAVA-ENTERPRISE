package be.ucll.views;

import be.ucll.components.dashboard.EmailButton;
import be.ucll.components.dashboard.OrderGrid;
import be.ucll.components.dashboard.SearchForm;
import be.ucll.dto.SearchCriteriaDTO;
import be.ucll.entities.Order;
import be.ucll.services.EmailQueueProducerService;
import be.ucll.services.OrderService;
import be.ucll.services.ProductService;
import be.ucll.util.AppLayoutTemplate;
import be.ucll.util.AppRoutes;
import be.ucll.util.SearchHistoryHandler;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;

@Route(AppRoutes.DASHBOARD_VIEW)
@PageTitle("Order Dashboard")
@RolesAllowed("USER")
public class DashboardView extends AppLayoutTemplate {

    private static final Logger log = Logger.getLogger(DashboardView.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private EmailQueueProducerService jmsEmailService;

    private final SearchHistoryHandler searchHistoryHandler =  new SearchHistoryHandler();

    private SearchForm searchForm;
    private OrderGrid orderGrid;
    private EmailButton emailButton;

    public DashboardView() {
        log.info("DashboardView initialized");
    }

    //productservice was null when injected into SearchForm so make sure i inject after spring bean injection
    @PostConstruct
    private void init() {
        setBody(buildDashboardLayout());
        restoreSession();
        searchForm.setHistoryComboBoxItems(searchHistoryHandler.loadHistory());
    }

    private VerticalLayout buildDashboardLayout() {
        orderGrid = new OrderGrid();
        searchForm = new SearchForm(productService, searchHistoryHandler);
        emailButton = new EmailButton();

        VerticalLayout layout = new VerticalLayout(searchForm, orderGrid, emailButton);
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(true);

        initEventListeners();

        return layout;
    }

    private void initEventListeners() {
        searchForm.addListener(SearchForm.SearchEvent.class, event -> {
            handleSearch(event.getCriteria());
        });

        searchForm.addListener(SearchForm.ClearEvent.class, event -> {
            orderGrid.setItems(Collections.emptyList());
        });

        searchHistoryHandler.addHistoryChangedListener(event -> {
            searchForm.setHistoryComboBoxItems(event.getHistory());
        });

        emailButton.addSendEmailListener(event -> {
            jmsEmailService.sendOrderSummaryEmail(event.getEmail(), event.getOrders());
        });
    }

    private void handleSearch(SearchCriteriaDTO criteria) {
        criteria.setCreatedDate(LocalDateTime.now());

        List<Order> results = orderService.searchOrders(criteria);
        orderGrid.setItems(results);

        searchHistoryHandler.addToHistory(criteria);

        VaadinSession.getCurrent().setAttribute("lastSearchCriteria", criteria);
        VaadinSession.getCurrent().setAttribute("lastSearchResults", results);

        emailButton.setOrders(results);
        emailButton.setEmailAddress(criteria.getEmail());

        if (results.isEmpty()) {
            Notification.show("Geen resultaten gevonden.", 3000, Notification.Position.MIDDLE);
        } else {
            Notification.show(results.size() + " resultaten gevonden.", 3000, Notification.Position.MIDDLE);
        }
    }

    private void restoreSession() {
        SearchCriteriaDTO savedCriteria = (SearchCriteriaDTO) VaadinSession.getCurrent().getAttribute("lastSearchCriteria");
        List<Order> savedResults = (List<Order>) VaadinSession.getCurrent().getAttribute("lastSearchResults");

        if (savedCriteria != null && savedResults != null) {
            searchForm.loadCriteria(savedCriteria);
            orderGrid.setItems(savedResults);

            emailButton.setOrders(savedResults);
            emailButton.setEmailAddress(savedCriteria.getEmail());
        }
    }
}
