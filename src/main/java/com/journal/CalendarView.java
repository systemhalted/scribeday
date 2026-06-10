package com.journal;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
 */
public class CalendarView extends BorderPane {

    private static final String[] WEEKDAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final DateTimeFormatter HEADER_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final String DOT_COLOR = "#1A7F37";
    private static final String TODAY_BG = "#D6E9FF";

    private final JournalDao dao;
    private YearMonth current;

    private final Label headerLabel = new Label();
    private final GridPane grid = new GridPane();

    public CalendarView(JournalDao dao, YearMonth initialMonth) {
        this.dao = dao;
        this.current = initialMonth;

        setPadding(new Insets(12));
        setTop(buildHeader());
        setCenter(grid);
        configureGrid();
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
        // 1 weekday header row + up to 6 week rows.
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
        headerLabel.setText(current.format(HEADER_FORMAT));
        grid.getChildren().clear();

        for (int c = 0; c < WEEKDAYS.length; c++) {
            Label label = new Label(WEEKDAYS[c]);
            label.setStyle("-fx-font-weight: bold;");
            GridPane.setHalignment(label, HPos.CENTER);
            grid.add(label, c, 0);
        }

        Set<LocalDate> withEntries = dao.datesWithEntries(current);
        LocalDate today = LocalDate.now();

        int leadingBlanks = current.atDay(1).getDayOfWeek().getValue() % 7; // Sun -> 0
        int row = 1;
        int col = leadingBlanks;
        for (int day = 1; day <= current.lengthOfMonth(); day++) {
            LocalDate date = current.atDay(day);
            grid.add(buildDayButton(date, withEntries.contains(date), date.equals(today)), col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
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
        button.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        if (isToday) {
            button.setStyle("-fx-background-color: " + TODAY_BG + "; -fx-border-color: " + DOT_COLOR
                    + "; -fx-border-width: 2;");
        }
        GridPane.setHgrow(button, Priority.ALWAYS);
        GridPane.setVgrow(button, Priority.ALWAYS);

        button.setOnAction(e -> {
            JournalEditorDialog editor = new JournalEditorDialog(getScene().getWindow(), dao, date);
            editor.showAndWait();
            rebuild();
        });
        return button;
    }
}
