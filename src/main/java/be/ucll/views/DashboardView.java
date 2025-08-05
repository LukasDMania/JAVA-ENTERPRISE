package be.ucll.views;

import be.ucll.util.AppLayoutTemplate;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("dashboard")
@PageTitle("Order Dashboard")
@PermitAll
public class DashboardView extends AppLayoutTemplate {
    public DashboardView() {
        setBody(createContent());
    }

    private Component createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(false);
        layout.setSpacing(false);
        H1 title = new H1("Order Dashboard");
        layout.add(title);
        return layout;
    }
}
