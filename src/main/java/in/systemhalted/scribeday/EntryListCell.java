package in.systemhalted.scribeday;

import java.time.format.DateTimeFormatter;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

/**
 * Renders a {@link SearchHit} as a date + title heading over a content snippet.
 * Shared by the search results and the agenda list.
 */
public class EntryListCell extends ListCell<SearchHit> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy");

    @Override
    protected void updateItem(SearchHit hit, boolean empty) {
        super.updateItem(hit, empty);
        if (empty || hit == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        String heading = hit.date().format(DATE_FORMAT);
        if (hit.title() != null && !hit.title().isBlank()) {
            heading += " — " + hit.title();
        }
        Label headingLabel = new Label(heading);
        headingLabel.setStyle("-fx-font-weight: bold;");
        Label snippetLabel = new Label(hit.snippet() == null ? "" : hit.snippet());
        snippetLabel.setStyle("-fx-opacity: 0.8;");
        snippetLabel.setWrapText(true);
        setGraphic(new VBox(2, headingLabel, snippetLabel));
    }
}
