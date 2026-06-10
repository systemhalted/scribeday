package com.journal;

import java.time.YearMonth;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * JavaFX application entry point. Wires the SQLite data layer and settings to the
 * calendar view and shows the main window.
 */
public class JournalApp extends Application {

    private JournalDao dao;
    private Settings settings;

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
        CalendarView view = new CalendarView(dao, settings, YearMonth.now());

        BorderPane root = new BorderPane();
        root.setTop(buildMenuBar(stage, view));
        root.setCenter(view);

        Scene scene = new Scene(root, 580, 560);
        stage.setTitle("Calendar Journal");
        stage.setScene(scene);
        stage.setMinWidth(460);
        stage.setMinHeight(460);
        stage.show();
    }

    private MenuBar buildMenuBar(Stage stage, CalendarView view) {
        MenuItem preferences = new MenuItem("Preferences…");
        preferences.setOnAction(e ->
                new PreferencesDialog(stage, settings, view::refresh).showAndWait());

        MenuItem quit = new MenuItem("Quit");
        quit.setOnAction(e -> Platform.exit());

        Menu file = new Menu("File", null, preferences, new SeparatorMenuItem(), quit);
        return new MenuBar(file);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
