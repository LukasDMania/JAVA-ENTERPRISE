package be.ucll.views;

import be.ucll.dto.LoginDTO;

import be.ucll.util.AppLayoutTemplate;
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
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Route("login")
@PageTitle("Login")
@PermitAll
public class LoginView extends AppLayoutTemplate {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private HttpServletRequest httpServletRequest;

    public LoginView() {
        setBody(buildLoginForm());
    }

    private VerticalLayout buildLoginForm() {
        LoginDTO loginDTO = new LoginDTO();
        Binder<LoginDTO> binder = new Binder<>(LoginDTO.class);

        TextField username = new TextField("Gebruikersnaam");
        PasswordField password = new PasswordField("Wachtwoord");
        Span errorLabel = new Span();
        errorLabel.getStyle().set("color", "red");

        binder.forField(username)
                .asRequired("Gebruikersnaam is verplicht")
                .bind(LoginDTO::getUsername, LoginDTO::setUsername);
        binder.forField(password)
                .asRequired("Wachtwoord is verplicht")
                .bind(LoginDTO::getPassword, LoginDTO::setPassword);

        Button loginButton = new Button("Login", event -> {
            if (binder.isValid()) {
                Notification.show("BINDER VALID is verplicht", 3000, Notification.Position.MIDDLE);
                handleLogin(username.getValue(), password.getValue(), errorLabel);
            }
        });

        FormLayout formLayout = new FormLayout(username, password, loginButton);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        VerticalLayout wrapper = new VerticalLayout(formLayout, errorLabel);
        wrapper.setWidth("300px");
        wrapper.setAlignItems(Alignment.CENTER);
        wrapper.setJustifyContentMode(JustifyContentMode.CENTER);
        wrapper.setSizeFull();

        return wrapper;
    }

    private void handleLogin(String username, String password, Span errorLabel) {
        Notification.show("BINDER VALID", 3000, Notification.Position.MIDDLE);
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManager.authenticate(authRequest);

        //store in sesh
        SecurityContextHolder.getContext().setAuthentication(authentication);
        httpServletRequest.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        Notification.show("Login Successful", 2000, Notification.Position.MIDDLE);
        getUI().ifPresent(ui -> ui.navigate("dashboard"));
    }
}
