package be.ucll.components.login;

import be.ucll.dto.LoginDTO;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

public class LoginForm extends VerticalLayout {

    private final Binder<LoginDTO> binder = new Binder<>(LoginDTO.class);
    private final LoginDTO loginDTO = new LoginDTO();

    private final TextField username = new TextField("Gebruikersnaam");
    private final PasswordField password = new PasswordField("Wachtwoord");
    private final Span errorLabel = new Span();
    private final Button loginButton = new Button("Login");

    public LoginForm() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();
        setWidth("300px");

        errorLabel.getStyle().set("color", "red");
        loginButton.setEnabled(false);

        configureBinder();

        FormLayout formLayout = new FormLayout(username, password, loginButton);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        add(formLayout, errorLabel);

        loginButton.addClickListener(_ -> {
            if (binder.writeBeanIfValid(loginDTO)) {
                fireEvent(new LoginEvent(this, loginDTO));
            } else {
                errorLabel.setText("Ongeldige gebruikersnaam of wachtwoord.");
            }
        });
    }

    private void configureBinder() {
        binder.forField(username)
                .asRequired("Gebruikersnaam is verplicht")
                .bind(LoginDTO::getUsername, LoginDTO::setUsername);
        binder.forField(password)
                .asRequired("Wachtwoord is verplicht")
                .bind(LoginDTO::getPassword, LoginDTO::setPassword);

        binder.addStatusChangeListener(_ -> loginButton.setEnabled(binder.isValid()));
    }

    public void setErrorMessage(String message) {
        errorLabel.setText(message);
    }

    //Events
    public static class LoginEvent extends ComponentEvent<LoginForm> {
        private final LoginDTO loginDTO;
        public LoginEvent(LoginForm source, LoginDTO loginDTO) {
            super(source, false);
            this.loginDTO = loginDTO;
        }
        public LoginDTO getLoginDTO() {
            return loginDTO;
        }
    }

    public Registration addLoginListener(ComponentEventListener<LoginEvent> listener) {
        return addListener(LoginEvent.class, listener);
    }
}
