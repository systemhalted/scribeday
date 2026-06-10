package com.journal;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.YearMonth;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * JavaFX application entry point. Wires the SQLite data layer and settings to the
 * calendar and agenda views and shows the main window.
 */
public class JournalApp extends Application {

    private JournalDao dao;
    private Settings settings;
    private Path dbPath;
    private Scene scene;
    private BorderPane root;
    private CalendarView calendarView;
    private AgendaView agendaView;

    @Override
    public void init() {
        dbPath = AppPaths.databaseFile();
        dao = new JournalDao(dbPath.toString());
        dao.init();
        SettingsDao settingsDao = new SettingsDao(dbPath.toString());
        settingsDao.init();
        settings = new Settings(settingsDao);
    }

    @Override
    public void start(Stage stage) {
        calendarView = new CalendarView(dao, settings, YearMonth.now());
        agendaView = new AgendaView(dao, settings);

        root = new BorderPane();
        root.setTop(buildMenuBar(stage));
        root.setCenter(calendarView);

        scene = new Scene(root, 580, 560);
        settings.theme().applyTo(scene);
        stage.setTitle("Calendar Journal");
        stage.setScene(scene);
        stage.setMinWidth(460);
        stage.setMinHeight(460);
        stage.show();
    }

    private MenuBar buildMenuBar(Stage stage) {
        MenuItem preferences = new MenuItem("Preferences…");
        preferences.setOnAction(e ->
                new PreferencesDialog(stage, settings, () -> {
                    settings.theme().applyTo(scene);
                    refreshViews();
                }).showAndWait());

        MenuItem backup = new MenuItem("Back Up…");
        backup.setOnAction(e -> backUp(stage));

        MenuItem restore = new MenuItem("Restore…");
        restore.setOnAction(e -> restore(stage));

        MenuItem exportAll = new MenuItem("Export All to Markdown…");
        exportAll.setOnAction(e -> exportAll(stage));

        MenuItem quit = new MenuItem("Quit");
        quit.setOnAction(e -> Platform.exit());

        Menu file = new Menu("File", null, preferences, new SeparatorMenuItem(),
                backup, restore, exportAll, new SeparatorMenuItem(), quit);

        MenuItem find = new MenuItem("Find…");
        find.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        find.setOnAction(e ->
                new SearchDialog(stage, dao, settings, this::refreshViews).showAndWait());

        Menu edit = new Menu("Edit", null, find);

        ToggleGroup viewGroup = new ToggleGroup();
        RadioMenuItem calendarItem = new RadioMenuItem("Calendar");
        calendarItem.setToggleGroup(viewGroup);
        calendarItem.setSelected(true);
        calendarItem.setOnAction(e -> {
            calendarView.refresh();
            root.setCenter(calendarView);
        });
        RadioMenuItem agendaItem = new RadioMenuItem("Agenda");
        agendaItem.setToggleGroup(viewGroup);
        agendaItem.setOnAction(e -> {
            agendaView.refresh();
            root.setCenter(agendaView);
        });

        Menu view = new Menu("View", null, calendarItem, agendaItem);
        return new MenuBar(file, edit, view);
    }

    private void backUp(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose a folder for the backup");
        File dir = chooser.showDialog(stage);
        if (dir == null) {
            return;
        }
        try {
            Path saved = BackupService.backup(dbPath, dir.toPath(), LocalDateTime.now());
            alert(Alert.AlertType.INFORMATION, "Backup complete", "Saved to:\n" + saved);
        } catch (RuntimeException ex) {
            alert(Alert.AlertType.ERROR, "Backup failed", ex.getMessage());
        }
    }

    private void restore(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a backup to restore");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Journal database", "*.db"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        if (!BackupService.isJournalDatabase(file.toPath())) {
            alert(Alert.AlertType.ERROR, "Cannot restore",
                    "That file is not a Calendar Journal backup.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.WARNING,
                "Restoring will replace your current journal with this backup. Continue?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.initOwner(stage);
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            try {
                BackupService.restore(file.toPath(), dbPath);
                refreshViews();
                alert(Alert.AlertType.INFORMATION, "Restore complete",
                        "Your journal has been restored from the backup.");
            } catch (RuntimeException ex) {
                alert(Alert.AlertType.ERROR, "Restore failed", ex.getMessage());
            }
        });
    }

    private void exportAll(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose a folder to export Markdown files");
        File dir = chooser.showDialog(stage);
        if (dir == null) {
            return;
        }
        try {
            int n = MarkdownExport.exportAll(dao, dir.toPath());
            alert(Alert.AlertType.INFORMATION, "Export complete",
                    "Exported " + n + (n == 1 ? " entry" : " entries") + " to:\n" + dir);
        } catch (RuntimeException ex) {
            alert(Alert.AlertType.ERROR, "Export failed", ex.getMessage());
        }
    }

    private void alert(Alert.AlertType type, String header, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    /** Refresh whichever views exist after an edit elsewhere. */
    private void refreshViews() {
        calendarView.refresh();
        agendaView.refresh();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
