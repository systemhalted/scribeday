package in.systemhalted.scribeday;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

/**
 * A reverse-chronological list of all entries. Selecting one opens that day's
 * entry; the list refreshes on return.
 */
public class AgendaView extends BorderPane {

    private static final int MAX_ENTRIES = 1000;

    private final JournalDao dao;
    private final Settings settings;
    private final ListView<SearchHit> list = new ListView<>();

    public AgendaView(JournalDao dao, Settings settings) {
        this.dao = dao;
        this.settings = settings;

        setPadding(new Insets(12));
        list.setPlaceholder(new Label("No entries yet"));
        list.setCellFactory(v -> new EntryListCell());
        list.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openSelected();
            }
        });
        list.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                openSelected();
            }
        });
        setCenter(list);
        refresh();
    }

    /** Reload the entry list from the database. */
    public void refresh() {
        list.getItems().setAll(dao.recentEntries(MAX_ENTRIES));
    }

    private void openSelected() {
        SearchHit hit = list.getSelectionModel().getSelectedItem();
        if (hit == null) {
            return;
        }
        new JournalEditorDialog(getScene().getWindow(), dao, settings, hit.date()).showAndWait();
        refresh();
    }
}
