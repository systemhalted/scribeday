package com.journal;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Set;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

/**
 * The month view: a navigable grid of day buttons. Days that have a saved entry
 * are marked with a dot; today is highlighted. Clicking a day opens the editor.
 * The first column of the week follows the user's {@link Settings#weekStart()}.
 */
public class CalendarView extends BorderPane {

    private static final DateTimeFormatter HEADER_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final String DOT_COLOR = "#1A7F37";
    private static final String TODAY_BG = "#D6E9FF";

    private final JournalDao dao;
    private final Settings settings;
    private YearMonth current;

    private final Label headerLabel = new Label();
    private final GridPane grid = new GridPane();

    public CalendarView(JournalDao dao, Settings settings, YearMonth initialMonth) {
        this.dao = dao;
        this.settings = settings;
        this.current = initialMonth;

        setPadding(new Insets(12));
        setTop(buildHeader());
        setCenter(grid);
        configureGrid();
        rebuild();
    }

    /** Re-read settings and rebuild the grid (e.g. after Preferences changes). */
    public void refresh() {
        rebuild();
    }

    private HBox buildHeader() {
        Button prev = new Button("‹");
        prev.setOnAction(e -> {
            current = current.minusMonths(1);
            rebuild();
        });

        Button next = new Button("›");
        next.setOnAction(e -> {
            current = current.plusMonths(1);
            rebuild();
        });

        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.setAlignment(Pos.CENTER);
        HBox.setHgrow(headerLabel, Priority.ALWAYS);

        HBox header = new HBox(8, prev, headerLabel, next);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 12, 0));
        return header;
    }

    private void configureGrid() {
        grid.setHgap(4);
        grid.setVgap(4);
        for (int c = 0; c < 7; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            cc.setHalignment(HPos.CENTER);
            grid.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < 7; r++) {
            RowConstraints rc = new RowConstraints();
            if (r > 0) {
                rc.setVgrow(Priority.ALWAYS);
            }
            grid.getRowConstraints().add(rc);
        }
    }

    /** Rebuild the grid for the {@link #current} month and refresh entry dots. */
    private void rebuild() {
        DayOfWeek weekStart = settings.weekStart();
        headerLabel.setText(current.format(HEADER_FORMAT));
        grid.getChildren().clear();

        for (int c = 0; c < 7; c++) {
            DayOfWeek day = weekStart.plus(c);
            Label label = new Label(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            label.setStyle("-fx-font-weight: bold;");
            GridPane.setHalignment(label, HPos.CENTER);
            grid.add(label, c, 0);
        }

        Set<LocalDate> withEntries = dao.datesWithEntries(current);
        LocalDate today = LocalDate.now();

        int row = 1;
        for (int day = 1; day <= current.lengthOfMonth(); day++) {
            LocalDate date = current.atDay(day);
            int col = columnFor(date, weekStart);
            grid.add(buildDayButton(date, withEntries.contains(date), date.equals(today)), col, row);
            if (col == 6) {
                row++;
            }
        }
    }

    /** Column (0-6) of a date within a week that starts on {@code weekStart}. */
    private int columnFor(LocalDate date, DayOfWeek weekStart) {
        return (date.getDayOfWeek().getValue() - weekStart.getValue() + 7) % 7;
    }

    private Button buildDayButton(LocalDate date, boolean hasEntry, boolean isToday) {
        Label number = new Label(String.valueOf(date.getDayOfMonth()));
        VBox content = new VBox(2, number);
        content.setAlignment(Pos.TOP_CENTER);
        if (hasEntry) {
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: " + DOT_COLOR + "; -fx-font-size: 10px;");
            content.getChildren().add(dot);
        }

        Button button = new Button();
        button.setGraphic(content);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMaxHeight(Double.MAX_VALUE);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        if (isToday) {
            button.setStyle("-fx-background-color: " + TODAY_BG + "; -fx-border-color: " + DOT_COLOR
                    + "; -fx-border-width: 2;");
        }
        GridPane.setHgrow(button, Priority.ALWAYS);
        GridPane.setVgrow(button, Priority.ALWAYS);

        button.setOnAction(e -> {
            JournalEditorDialog editor = new JournalEditorDialog(getScene().getWindow(), dao, settings, date);
            editor.showAndWait();
            rebuild();
        });
        return button;
    }
}
