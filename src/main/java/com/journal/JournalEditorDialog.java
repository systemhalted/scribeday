package com.journal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * The "notepad": a modal window for one day's journal entry. Changes autosave a
 * short pause after you stop typing, and again when the window closes, so text is
 * never lost. Blank text removes the entry (see {@link EntryAutosave}).
 */
public class JournalEditorDialog extends Stage {

    private static final DateTimeFormatter TITLE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private static final Duration AUTOSAVE_DELAY = Duration.millis(600);

    private final JournalDao dao;
    private final LocalDate date;
    private final TextArea textArea;
    private final Label status = new Label();
    private boolean deleted = false;

    public JournalEditorDialog(Window owner, JournalDao dao, Settings settings, LocalDate date) {
        this.dao = dao;
        this.date = date;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Journal — " + date.format(TITLE_FORMAT));

        String existing = dao.load(date);
        status.setText(existing == null ? "" : "Saved");

        textArea = new TextArea(existing == null ? "" : existing);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: " + settings.editorFontSize() + "px;");

        // Debounced autosave: restart the timer on each keystroke; save when it settles.
        PauseTransition debounce = new PauseTransition(AUTOSAVE_DELAY);
        debounce.setOnFinished(e -> autosave());
        textArea.textProperty().addListener((obs, old, val) -> {
            status.setText("Editing…");
            debounce.playFromStart();
        });

        Button delete = new Button("Delete");
        delete.setOnAction(e -> confirmDelete());

        Button close = new Button("Close");
        close.setDefaultButton(true);
        close.setOnAction(e -> close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttons = new HBox(8, status, spacer, delete, close);
        buttons.setAlignment(Pos.CENTER_LEFT);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setCenter(textArea);
        root.setBottom(buttons);
        BorderPane.setMargin(buttons, new Insets(10, 0, 0, 0));

        // Flush a final save on close (unless the entry was explicitly deleted).
        setOnHidden(e -> {
            debounce.stop();
            if (!deleted) {
                autosave();
            }
        });

        setScene(new Scene(root, 560, 460));
        settings.theme().applyTo(getScene());
    }

    private void autosave() {
        EntryAutosave.persist(dao, date, textArea.getText());
        status.setText(textArea.getText().isBlank() ? "" : "Saved");
    }

    private void confirmDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete the journal entry for this day?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.initOwner(this);
        confirm.showAndWait()
                .filter(b -> b == ButtonType.YES)
                .ifPresent(b -> {
                    deleted = true;          // skip the on-close autosave
                    dao.delete(date);
                    close();
                });
    }
}
