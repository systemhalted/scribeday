package com.journal;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Full-text search across all entries: a query box over a results list. Selecting
 * a result opens that day's entry; on return the search re-runs and the caller is
 * notified so the calendar can refresh.
 */
public class SearchDialog extends Stage {

    private final JournalDao dao;
    private final Settings settings;
    private final Runnable onEntryChanged;

    private final TextField queryField = new TextField();
    private final ListView<SearchHit> results = new ListView<>();

    public SearchDialog(Window owner, JournalDao dao, Settings settings, Runnable onEntryChanged) {
        this.dao = dao;
        this.settings = settings;
        this.onEntryChanged = onEntryChanged;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Search journal");

        queryField.setPromptText("Search entries…");

        PauseTransition debounce = new PauseTransition(Duration.millis(200));
        debounce.setOnFinished(e -> runSearch());
        queryField.textProperty().addListener((obs, old, val) -> debounce.playFromStart());
        queryField.setOnAction(e -> {
            if (!results.getItems().isEmpty()) {
                results.getSelectionModel().select(0);
                openSelected();
            }
        });

        results.setPlaceholder(new Label("No matching entries"));
        results.setCellFactory(list -> new EntryListCell());
        results.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openSelected();
            }
        });
        results.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                openSelected();
            }
        });

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setTop(queryField);
        root.setCenter(results);
        BorderPane.setMargin(results, new Insets(10, 0, 0, 0));

        setScene(new Scene(root, 460, 480));
        settings.theme().applyTo(getScene());
    }

    private void runSearch() {
        String match = SearchQuery.toMatch(queryField.getText());
        if (match.isEmpty()) {
            results.getItems().clear();
        } else {
            results.getItems().setAll(dao.searchHits(match));
        }
    }

    private void openSelected() {
        SearchHit hit = results.getSelectionModel().getSelectedItem();
        if (hit == null) {
            return;
        }
        new JournalEditorDialog(this, dao, settings, hit.date()).showAndWait();
        if (onEntryChanged != null) {
            onEntryChanged.run();
        }
        runSearch();   // reflect any edits/deletes in the results
    }
}
