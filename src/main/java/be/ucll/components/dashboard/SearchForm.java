package be.ucll.components.dashboard;

import be.ucll.dto.SearchCriteriaDTO;
import be.ucll.services.ProductService;
import be.ucll.util.SearchHistoryHandler;
import be.ucll.views.DashboardView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;

public class SearchForm extends VerticalLayout {

    private static final Logger log = Logger.getLogger(DashboardView.class);

    private final Binder<SearchCriteriaDTO> binder = new Binder<>(SearchCriteriaDTO.class);
    ComboBox<SearchCriteriaDTO> historyComboBox = new ComboBox<>("Recente Zoekopdrachten");

    private final ProductService productService;
    private final SearchHistoryHandler searchHistoryHandler;

    public SearchForm(ProductService productService, SearchHistoryHandler searchHistoryHandler) {
        this.productService = productService;
        this.searchHistoryHandler = searchHistoryHandler;

        buildSearchForm();
    }

    public void buildSearchForm() {
        NumberField minAmount = new NumberField("Minimum bedrag");
        NumberField maxAmount = new NumberField("Maximum bedrag");
        NumberField productCount = new NumberField("Aantal producten");
        Select<String> deliveredSelect = new Select<>();
        deliveredSelect.setLabel("Afgeleverd");
        deliveredSelect.setItems("Alle", "Ja", "Nee");
        deliveredSelect.setValue("Alle");

        ComboBox<String> productName = new ComboBox<>("Product name");
        productName.setAllowCustomValue(true);

        //TODO: retry pagination instead of calling full dataset
        productName.setItems(query -> {
            String filter = query.getFilter().orElse("");
            return productService.autocompleteProductNames(filter)
                    .stream()
                    .skip(query.getOffset())
                    .limit(query.getLimit());
        });

        EmailField email = new EmailField("Email adres");

        Span errorLabel = new Span();
        errorLabel.getStyle().set("color", "red");

        //TODO: inject historyhandler use createlabel method
        //TODO: fire event to handle search and history load
        historyComboBox.setItemLabelGenerator(searchHistoryHandler::createHistoryLabel);
        historyComboBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                binder.readBean(event.getValue());
                fireEvent(new SearchEvent(this, event.getValue()));
            }
        });


        binder.forField(minAmount)
                .withConverter(
                        doubleValue -> doubleValue == null ? null : BigDecimal.valueOf(doubleValue),
                        bigDecimalValue -> bigDecimalValue == null ? null : bigDecimalValue.doubleValue(),
                        "Het bedrag moet een nummer zijn"
                )
                .withValidator(val -> val == null || val.compareTo(BigDecimal.ZERO) >= 0, "Minimum bedrag moet positief zijn")
                .bind(SearchCriteriaDTO::getMinAmount, SearchCriteriaDTO::setMinAmount);

        binder.forField(maxAmount)
                .withConverter(
                        doubleValue -> doubleValue == null ? null : BigDecimal.valueOf(doubleValue),
                        bigDecimalValue -> bigDecimalValue == null ? null : bigDecimalValue.doubleValue(),
                        "Het bedrag moet een nummer zijn")
                .withValidator(val -> val == null || val.compareTo(BigDecimal.ZERO) >= 0, "Maximaal bedrag moet positief zijn")
                .bind(SearchCriteriaDTO::getMaxAmount, SearchCriteriaDTO::setMaxAmount);

        binder.forField(productCount)
                .withConverter(
                        doubleValue -> doubleValue == null ? null : doubleValue.intValue(),
                        intValue -> intValue == null ? null : intValue.doubleValue(),
                        "Het aantal moet een nummer zijn"
                )
                .withValidator(val -> val == null || val >= 0, "Aantal moet positief zijn")
                .bind(SearchCriteriaDTO::getProductCount, SearchCriteriaDTO::setProductCount);

        binder.forField(deliveredSelect)
                .withConverter(
                        value -> {
                            if ("Ja".equals(value)) return Boolean.TRUE;
                            if ("Nee".equals(value)) return Boolean.FALSE;
                            return null;
                        },
                        boolValue -> {
                            if (boolValue == null) return "Alle";
                            return boolValue ? "Ja" : "Nee";
                        }
                )
                .bind(SearchCriteriaDTO::isDeliveredNullable, SearchCriteriaDTO::setDeliveredNullable);

        binder.forField(productName)
                .bind(SearchCriteriaDTO::getProductName, SearchCriteriaDTO::setProductName);

        binder.forField(email)
                .bind(SearchCriteriaDTO::getEmail, SearchCriteriaDTO::setEmail);


        Button clearButton = new Button("Wissen", event -> {
            binder.readBean(new SearchCriteriaDTO());
            fireEvent(new ClearEvent(this));
            errorLabel.setText("");
        });

        Button searchButton = new Button("Zoeken", event -> {
            log.info("User triggered search");

            SearchCriteriaDTO tempCriteria = new SearchCriteriaDTO();

            if (binder.writeBeanIfValid(tempCriteria)) {
                if (!tempCriteria.hasAtLeastOneCriteria()) {
                    log.warn("Search attempted with no criteria");
                    errorLabel.setText("Geef ten minste één zoekcriteria op.");
                    return;
                }

                fireEvent(new SearchEvent(this, tempCriteria));
                errorLabel.setText("");
            } else {
                log.warn("Search form validation failed");
                errorLabel.setText("Vul de velden correct in.");
            }
        });


        FormLayout formLayout = new FormLayout(
                minAmount, maxAmount, productCount, deliveredSelect,
                productName, email, clearButton, searchButton, historyComboBox
        );
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        VerticalLayout wrapper = new VerticalLayout(formLayout, errorLabel);
        wrapper.setAlignItems(FlexComponent.Alignment.START);
        add(wrapper);
    }

    public void setHistoryComboBoxItems(LinkedList<SearchCriteriaDTO> history) {
        historyComboBox.setItems(history);
    }

    public void loadCriteria(SearchCriteriaDTO criteria) {
        binder.readBean(criteria);
    }

    //Event handling
    public static class SearchEvent extends ComponentEvent<SearchForm> {
        private final SearchCriteriaDTO criteria;
        public SearchEvent(SearchForm source, SearchCriteriaDTO criteria) {
            super(source, false);
            this.criteria = criteria;
        }
        public SearchCriteriaDTO getCriteria() {
            return criteria;
        }
    }

    public static class ClearEvent extends ComponentEvent<SearchForm> {
        public ClearEvent(SearchForm source) {
            super(source, false);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(
            Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
