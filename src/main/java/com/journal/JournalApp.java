package com.journal;

import java.time.YearMonth;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
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
import javafx.stage.Stage;

/**
 * JavaFX application entry point. Wires the SQLite data layer and settings to the
 * calendar and agenda views and shows the main window.
 */
public class JournalApp extends Application {

    private JournalDao dao;
    private Settings settings;
    private Scene scene;
    private BorderPane root;
    private CalendarView calendarView;
    private AgendaView agendaView;

    @Override
    public void init() {
        String dbFile = AppPaths.databaseFile().toString();
        dao = new JournalDao(dbFile);
        dao.init();
        SettingsDao settingsDao = new SettingsDao(dbFile);
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

        MenuItem quit = new MenuItem("Quit");
        quit.setOnAction(e -> Platform.exit());

        Menu file = new Menu("File", null, preferences, new SeparatorMenuItem(), quit);

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

    /** Refresh whichever views exist after an edit elsewhere. */
    private void refreshViews() {
        calendarView.refresh();
        agendaView.refresh();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
