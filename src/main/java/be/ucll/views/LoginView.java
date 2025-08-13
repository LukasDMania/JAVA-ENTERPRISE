package be.ucll.views;

import be.ucll.dto.LoginDTO;

import be.ucll.services.LoginService;
import be.ucll.util.AppLayoutTemplate;
import be.ucll.util.AppRoutes;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
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

    public LoginView() {
        log.info("LoginView initialized");
        setBody(buildLoginLayout());
    }

    private VerticalLayout buildLoginLayout() {
        LoginDTO loginDTO = new LoginDTO();
        Binder<LoginDTO> binder = new Binder<>(LoginDTO.class);

        TextField username = new TextField("Gebruikersnaam");
        PasswordField password = new PasswordField("Wachtwoord");
        Span errorLabel = new Span();
        errorLabel.getStyle().set("color", "red");

        Button loginButton = createLoginButton(binder, loginDTO, errorLabel);

        configureBinder(binder, username, password, loginButton);

        FormLayout formLayout = new FormLayout(username, password, loginButton);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        VerticalLayout wrapper = new VerticalLayout(formLayout, errorLabel);
        wrapper.setWidth("300px");
        wrapper.setAlignItems(Alignment.CENTER);
        wrapper.setJustifyContentMode(JustifyContentMode.CENTER);
        wrapper.setSizeFull();

        return wrapper;
    }

    private void configureBinder(Binder<LoginDTO> binder, TextField username, PasswordField password, Button loginButton) {
        binder.forField(username)
                .asRequired("Gebruikersnaam is verplicht")
                .bind(LoginDTO::getUsername, LoginDTO::setUsername);
        binder.forField(password)
                .asRequired("Wachtwoord is verplicht")
                .bind(LoginDTO::getPassword, LoginDTO::setPassword);

        binder.addStatusChangeListener(_ -> {
            loginButton.setEnabled(binder.isValid());
        });
    }
    private Button createLoginButton(Binder<LoginDTO> binder, LoginDTO loginDTO, Span errorLabel) {
        Button loginButton = new Button("Login");
        loginButton.setEnabled(false);

        loginButton.addClickListener(_ -> {
            if (binder.writeBeanIfValid(loginDTO)) {
                log.infof("Login attempt for username: %s", loginDTO.getUsername());
                handleLogin(loginDTO, errorLabel);
            } else {
                log.warn("Login form validation failed");
                errorLabel.setText("Ongeldige gebruikersnaam of wachtwoord.");
            }
        });

        return loginButton;
    }

    private void handleLogin(LoginDTO loginDTO, Span errorLabel) {
        try {
            loginService.authenticate(loginDTO);
            log.infof("Login successful for user: %s", loginDTO.getUsername());
            Notification.show("Login Successful", 2000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate(AppRoutes.DASHBOARD_VIEW));
        } catch (AuthenticationException e) {
            log.warnf("Login failed for user: %s", loginDTO.getUsername());
            errorLabel.setText("Ongeldige gebruikersnaam of wachtwoord.");
        }
    }
}
