package com.journal;

import java.time.YearMonth;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application entry point. Wires the SQLite DAO to the calendar view and
 * shows the main window. The data layer ({@link JournalDao}) is unchanged from
 * the original Swing version.
 */
public class JournalApp extends Application {

    private JournalDao dao;

    @Override
    public void init() {
        dao = new JournalDao(AppPaths.databaseFile().toString());
        dao.init();
    }

    @Override
    public void start(Stage stage) {
        CalendarView view = new CalendarView(dao, YearMonth.now());
        Scene scene = new Scene(view, 580, 540);
        stage.setTitle("Calendar Journal");
        stage.setScene(scene);
        stage.setMinWidth(460);
        stage.setMinHeight(440);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
