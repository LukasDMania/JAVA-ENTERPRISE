package be.ucll.views;

import be.ucll.components.login.LoginForm;
import be.ucll.dto.LoginDTO;
import be.ucll.services.LoginService;
import be.ucll.util.AppLayoutTemplate;
import be.ucll.util.AppRoutes;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;

@Route(AppRoutes.LOGIN_VIEW)
@PageTitle("Login")
@PermitAll
public class LoginView extends AppLayoutTemplate {

    private static final Logger log = Logger.getLogger(LoginView.class);

    @Autowired
    private LoginService loginService;

    private LoginForm loginForm;

    public LoginView() {
        log.info("LoginView initialized");
    }

    @PostConstruct
    private void init() {
        setBody(buildLoginLayout());
        initEventListeners();
    }

    private VerticalLayout buildLoginLayout() {
        loginForm = new LoginForm();

        VerticalLayout layout = new VerticalLayout(loginForm);
        layout.setSizeFull();
        layout.setAlignItems(VerticalLayout.Alignment.CENTER);
        layout.setJustifyContentMode(VerticalLayout.JustifyContentMode.CENTER);

        return layout;
    }

    private void initEventListeners() {
        loginForm.addLoginListener(event -> {
            handleLogin(event.getLoginDTO());
        });
    }

    private void handleLogin(LoginDTO loginDTO) {
        try {
            loginService.authenticate(loginDTO);
            log.infof("Login successful for user: %s", loginDTO.getUsername());
            Notification.show("Login Successful", 2000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(AppRoutes.DASHBOARD_VIEW));
        } catch (AuthenticationException e) {
            log.warnf("Login failed for user: %s", loginDTO.getUsername());
            loginForm.setErrorMessage("Ongeldige gebruikersnaam of wachtwoord.");
        }
    }
}
