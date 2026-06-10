package com.journal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * The "notepad": a modal window with a text area for one day's journal entry.
 * Pre-fills any existing entry; offers Save, Delete, and Cancel.
 */
public class JournalEditorDialog extends Stage {

    private static final DateTimeFormatter TITLE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    public JournalEditorDialog(Window owner, JournalDao dao, Settings settings, LocalDate date) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Journal — " + date.format(TITLE_FORMAT));

        String existing = dao.load(date);

        TextArea textArea = new TextArea(existing == null ? "" : existing);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: " + settings.editorFontSize() + "px;");
        BorderPane.setMargin(textArea, new Insets(0, 0, 10, 0));

        Button saveButton = new Button("Save");
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(e -> {
            dao.save(date, textArea.getText());
            close();
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setDisable(existing == null);
        deleteButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete the journal entry for this day?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.initOwner(this);
            confirm.showAndWait()
                    .filter(b -> b == ButtonType.YES)
                    .ifPresent(b -> {
                        dao.delete(date);
                        close();
                    });
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> close());

        HBox buttons = new HBox(8, deleteButton, cancelButton, saveButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(deleteButton, Priority.NEVER);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setCenter(textArea);
        root.setBottom(buttons);

        setScene(new Scene(root, 560, 460));
    }
}
