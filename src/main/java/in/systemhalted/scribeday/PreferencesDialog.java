package in.systemhalted.scribeday;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

/**
 * Preferences window. Reads current values from {@link Settings}, lets the user
 * change them, and persists on Save. {@code onSaved} runs after a successful save
 * so the caller can re-apply settings that affect the live UI.
 */
public class PreferencesDialog extends Stage {

    public PreferencesDialog(Window owner, Settings settings, Runnable onSaved) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Preferences");

        Spinner<Integer> fontSize = new Spinner<>(10, 28, settings.editorFontSize());
        fontSize.setEditable(true);
        fontSize.setPrefWidth(80);

        ComboBox<DayOfWeek> weekStart = new ComboBox<>();
        weekStart.getItems().addAll(DayOfWeek.SUNDAY, DayOfWeek.MONDAY);
        weekStart.setValue(settings.weekStart());
        weekStart.setConverter(new StringConverter<>() {
            @Override
            public String toString(DayOfWeek day) {
                return day == null ? "" : day.getDisplayName(TextStyle.FULL, Locale.getDefault());
            }

            @Override
            public DayOfWeek fromString(String s) {
                return DayOfWeek.valueOf(s.toUpperCase(Locale.ROOT));
            }
        });

        ComboBox<Theme> theme = new ComboBox<>();
        theme.getItems().addAll(Theme.LIGHT, Theme.DARK);
        theme.setValue(settings.theme());
        theme.setConverter(new StringConverter<>() {
            @Override
            public String toString(Theme t) {
                if (t == null) {
                    return "";
                }
                String name = t.name().toLowerCase(Locale.ROOT);
                return Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }

            @Override
            public Theme fromString(String s) {
                return Theme.valueOf(s.toUpperCase(Locale.ROOT));
            }
        });

        CheckBox autoBackup = new CheckBox("Back up automatically on launch");
        autoBackup.setSelected(settings.autoBackupEnabled());

        Spinner<Integer> backupInterval = new Spinner<>(1, 30, settings.autoBackupIntervalDays());
        backupInterval.setEditable(true);
        backupInterval.setPrefWidth(80);
        backupInterval.disableProperty().bind(autoBackup.selectedProperty().not());

        Spinner<Integer> backupKeep = new Spinner<>(1, 50, settings.autoBackupKeep());
        backupKeep.setEditable(true);
        backupKeep.setPrefWidth(80);
        backupKeep.disableProperty().bind(autoBackup.selectedProperty().not());

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.addRow(0, new Label("Theme:"), theme);
        form.addRow(1, new Label("Editor font size:"), fontSize);
        form.addRow(2, new Label("Week starts on:"), weekStart);
        form.add(autoBackup, 0, 3, 2, 1);
        form.addRow(4, new Label("Back up every (days):"), backupInterval);
        form.addRow(5, new Label("Backups to keep:"), backupKeep);

        Button save = new Button("Save");
        save.setDefaultButton(true);
        save.setOnAction(e -> {
            settings.setTheme(theme.getValue());
            settings.setEditorFontSize(fontSize.getValue());
            settings.setWeekStart(weekStart.getValue());
            settings.setAutoBackupEnabled(autoBackup.isSelected());
            settings.setAutoBackupIntervalDays(backupInterval.getValue());
            settings.setAutoBackupKeep(backupKeep.getValue());
            if (onSaved != null) {
                onSaved.run();
            }
            close();
        });

        Button cancel = new Button("Cancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> close());

        HBox buttons = new HBox(8, cancel, save);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setCenter(form);
        root.setBottom(buttons);
        BorderPane.setMargin(buttons, new Insets(16, 0, 0, 0));

        setScene(new Scene(root));
        settings.theme().applyTo(getScene());
        setResizable(false);
    }
}
