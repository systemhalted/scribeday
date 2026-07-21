package in.systemhalted.scribeday;

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
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import java.io.File;

/**
 * The "notepad": a modal window for one day's journal entry, with an optional
 * title, a live word/character count, and a Markdown preview. Changes autosave a
 * short pause after you stop typing, and again when the window closes, so text is
 * never lost. A blank title and content removes the entry (see {@link EntryAutosave}).
 */
public class JournalEditorDialog extends Stage {

    private static final DateTimeFormatter TITLE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private static final Duration AUTOSAVE_DELAY = Duration.millis(600);

    private final JournalDao dao;
    private final LocalDate date;
    private final boolean dark;
    private final MarkdownRenderer markdown = new MarkdownRenderer();

    private final TextField titleField = new TextField();
    private final TextArea textArea = new TextArea();
    private final Label status = new Label();
    private final Label count = new Label();
    private final ToggleButton previewToggle = new ToggleButton("Preview");
    private final SplitPane split = new SplitPane();
    private final BorderPane root = new BorderPane();
    private WebView webView;   // created lazily on first preview
    private boolean deleted = false;

    public JournalEditorDialog(Window owner, JournalDao dao, Settings settings, LocalDate date) {
        this.dao = dao;
        this.date = date;
        this.dark = settings.theme() == Theme.DARK;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Journal — " + date.format(TITLE_FORMAT));

        Entry existing = dao.loadEntry(date);
        titleField.setPromptText("Title (optional)");
        titleField.setText(existing == null || existing.title() == null ? "" : existing.title());
        textArea.setText(existing == null ? "" : existing.content());
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: " + settings.editorFontSize() + "px;");

        status.setText(existing == null ? "" : "Saved");
        updateCount();

        PauseTransition debounce = new PauseTransition(AUTOSAVE_DELAY);
        debounce.setOnFinished(e -> autosave());
        titleField.textProperty().addListener((obs, old, val) -> {
            onEdit(debounce);
            renderIfPreviewing();
        });
        textArea.textProperty().addListener((obs, old, val) -> {
            updateCount();
            onEdit(debounce);
            renderIfPreviewing();
        });

        previewToggle.selectedProperty().addListener((obs, was, on) -> setPreviewVisible(on));

        Button export = new Button("Export…");
        export.setOnAction(e -> exportEntry());

        Button delete = new Button("Delete");
        delete.setOnAction(e -> confirmDelete());

        Button close = new Button("Close");
        close.setDefaultButton(true);
        close.setOnAction(e -> close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bottom = new HBox(12, status, count, spacer, previewToggle, export, delete, close);
        bottom.setAlignment(Pos.CENTER_LEFT);

        VBox.setMargin(titleField, new Insets(0, 0, 4, 0));

        root.setPadding(new Insets(12));
        root.setTop(new VBox(8, titleField));
        root.setCenter(textArea);
        root.setBottom(bottom);
        BorderPane.setMargin(textArea, new Insets(4, 0, 0, 0));
        BorderPane.setMargin(bottom, new Insets(10, 0, 0, 0));

        setOnHidden(e -> {
            debounce.stop();
            if (!deleted) {
                autosave();
            }
        });

        setScene(new Scene(root, 580, 480));
        settings.theme().applyTo(getScene());
    }

    private void onEdit(PauseTransition debounce) {
        status.setText("Editing…");
        debounce.playFromStart();
    }

    private void updateCount() {
        TextStats s = TextStats.of(textArea.getText());
        count.setText(s.words() + " words · " + s.chars() + " chars");
    }

    private void setPreviewVisible(boolean on) {
        if (on) {
            if (webView == null) {
                webView = new WebView();
            }
            renderPreview();
            split.getItems().setAll(textArea, webView);
            split.setDividerPositions(0.5);
            root.setCenter(split);
            BorderPane.setMargin(split, new Insets(4, 0, 0, 0));
            if (getWidth() < 880) {
                setWidth(940);
            }
        } else {
            split.getItems().clear();
            root.setCenter(textArea);
            BorderPane.setMargin(textArea, new Insets(4, 0, 0, 0));
        }
    }

    private void renderIfPreviewing() {
        if (previewToggle.isSelected()) {
            renderPreview();
        }
    }

    private void renderPreview() {
        String title = titleField.getText();
        String source = (title == null || title.isBlank() ? "" : "# " + title + "\n\n") + textArea.getText();
        webView.getEngine().loadContent(previewDocument(markdown.toHtml(source)));
    }

    private String previewDocument(String bodyHtml) {
        String bg = dark ? "#2b2b2b" : "#ffffff";
        String fg = dark ? "#e0e0e0" : "#222222";
        String codeBg = dark ? "#3c3f41" : "#f3f3f3";
        String css = "body{font-family:-apple-system,Segoe UI,Roboto,sans-serif;line-height:1.55;"
                + "padding:14px;color:" + fg + ";background:" + bg + ";}"
                + "h1,h2,h3{margin-top:0.6em;}"
                + "code{background:" + codeBg + ";padding:1px 4px;border-radius:3px;}"
                + "pre{background:" + codeBg + ";padding:10px;border-radius:5px;overflow:auto;}"
                + "blockquote{border-left:3px solid #888;margin-left:0;padding-left:12px;color:#999;}";
        return "<!DOCTYPE html><html><head><meta charset='utf-8'><style>" + css
                + "</style></head><body>" + bodyHtml + "</body></html>";
    }

    private void exportEntry() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export entry as Markdown");
        chooser.setInitialFileName(MarkdownExport.fileName(date));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Markdown", "*.md"));
        File file = chooser.showSaveDialog(this);
        if (file == null) {
            return;
        }
        String title = titleField.getText();
        MarkdownExport.exportEntry(file.toPath(), date,
                new Entry(title == null || title.isBlank() ? null : title, textArea.getText()));
    }

    private void autosave() {
        EntryAutosave.persist(dao, date, titleField.getText(), textArea.getText());
        boolean empty = titleField.getText().isBlank() && textArea.getText().isBlank();
        status.setText(empty ? "" : "Saved");
    }

    private void confirmDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete the journal entry for this day?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.initOwner(this);
        confirm.showAndWait()
                .filter(b -> b == ButtonType.YES)
                .ifPresent(b -> {
                    deleted = true;
                    dao.delete(date);
                    close();
                });
    }
}
