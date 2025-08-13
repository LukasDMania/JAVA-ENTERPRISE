package be.ucll.util;

import be.ucll.dto.SearchCriteriaDTO;
import be.ucll.views.DashboardView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import org.jboss.logging.Logger;

import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Tag("div")
public class SearchHistoryHandler extends Component {
    private static final String SEARCH_HISTORY_SESSION_KEY = "searchHistory";
    private static final int MAX_HISTORY_SIZE = 5;


    public void addToHistory(SearchCriteriaDTO criteria) {
        LinkedList<SearchCriteriaDTO> history = (LinkedList<SearchCriteriaDTO>)
                VaadinSession.getCurrent().getAttribute(SEARCH_HISTORY_SESSION_KEY);

        if (history == null) {
            history = new LinkedList<>();
        }

        Optional<SearchCriteriaDTO> existingMatch = history.stream()
                .filter(criteria::equals)
                .findFirst();

        if (existingMatch.isPresent()) {
            SearchCriteriaDTO existing = existingMatch.get();
            existing.setCreatedDate(criteria.getCreatedDate());

            history.remove(existing);
            history.addFirst(existing);
        } else {
            history.addFirst(criteria);

            if (history.size() > MAX_HISTORY_SIZE) {
                history.removeLast();
            }
        }

        VaadinSession.getCurrent().setAttribute(SEARCH_HISTORY_SESSION_KEY, history);

        fireEvent(new HistoryChangedEvent(this, history));
    }

    public LinkedList<SearchCriteriaDTO> loadHistory() {
        LinkedList<SearchCriteriaDTO> history =
                (LinkedList<SearchCriteriaDTO>) VaadinSession.getCurrent().getAttribute(SEARCH_HISTORY_SESSION_KEY);
        if (history == null) {
            history = new LinkedList<>();
            VaadinSession.getCurrent().setAttribute(SEARCH_HISTORY_SESSION_KEY, history);
        }
        return history;
    }

    public String createHistoryLabel(SearchCriteriaDTO criteria) {
        StringBuilder labelBuilder = new StringBuilder();
        if (criteria.getCreatedDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            labelBuilder.append(formatter.format(criteria.getCreatedDate())).append(": ");
        }

        List<String> parts = new LinkedList<>();
        Optional.ofNullable(criteria.getMinAmount()).map(v -> "Min bedrag: " + v).ifPresent(parts::add);
        Optional.ofNullable(criteria.getMaxAmount()).map(v -> "Max bedrag: " + v).ifPresent(parts::add);
        Optional.ofNullable(criteria.getProductCount()).map(v -> "Aantal prod: " + v).ifPresent(parts::add);
        Optional.ofNullable(criteria.getProductName()).filter(s -> !s.isBlank()).map(v -> "Product: " + v).ifPresent(parts::add);
        Optional.ofNullable(criteria.getEmail()).filter(s -> !s.isBlank()).map(v -> "Email: " + v).ifPresent(parts::add);
        Optional.ofNullable(criteria.isDeliveredNullable()).map(v -> "Afgeleverd: " + (v ? "Ja" : "Nee")).ifPresent(parts::add);

        if (parts.isEmpty()) {
            return "Lege zoekopdracht";
        }

        labelBuilder.append(String.join(", ", parts));

        return labelBuilder.toString();
    }

    public static class HistoryChangedEvent extends ComponentEvent<SearchHistoryHandler> {
        private final LinkedList<SearchCriteriaDTO> history;
        public HistoryChangedEvent(SearchHistoryHandler source, LinkedList<SearchCriteriaDTO> history) {
            super(source, false);
            this.history = history;
        }
        public LinkedList<SearchCriteriaDTO> getHistory() { return history; }
    }

    public Registration addHistoryChangedListener(ComponentEventListener<HistoryChangedEvent> listener) {
        return addListener(HistoryChangedEvent.class, listener);
    }
}
