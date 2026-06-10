package com.journal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * The month view: a navigable grid of day buttons. Days that have a saved entry
 * are marked with a dot; today is highlighted. Clicking a day opens the editor.
 */
public class CalendarPanel extends JPanel {

    private static final String[] WEEKDAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final DateTimeFormatter HEADER_FORMAT =
            DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final Color TODAY_COLOR = new Color(0xD6, 0xE9, 0xFF);
    private static final Color ENTRY_DOT_COLOR = new Color(0x1A, 0x7F, 0x37);

    private final JournalDao dao;
    private YearMonth current;

    private final JLabel headerLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel grid = new JPanel();

    public CalendarPanel(JournalDao dao, YearMonth initialMonth) {
        this.dao = dao;
        this.current = initialMonth;

        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildHeader(), BorderLayout.NORTH);

        grid.setLayout(new GridLayout(0, 7, 4, 4));
        add(grid, BorderLayout.CENTER);

        rebuild();
    }

    private JPanel buildHeader() {
        JButton prev = new JButton("‹");
        prev.setToolTipText("Previous month");
        prev.addActionListener(e -> {
            current = current.minusMonths(1);
            rebuild();
        });

        JButton next = new JButton("›");
        next.setToolTipText("Next month");
        next.addActionListener(e -> {
            current = current.plusMonths(1);
            rebuild();
        });

        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 18f));

        JPanel header = new JPanel(new BorderLayout());
        header.add(prev, BorderLayout.WEST);
        header.add(headerLabel, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);
        return header;
    }

    /** Rebuild the grid for the {@link #current} month and refresh entry dots. */
    private void rebuild() {
        headerLabel.setText(current.format(HEADER_FORMAT));
        grid.removeAll();

        for (String weekday : WEEKDAYS) {
            JLabel label = new JLabel(weekday, SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            grid.add(label);
        }

        Set<LocalDate> withEntries = dao.datesWithEntries(current);
        LocalDate today = LocalDate.now();

        // Leading blanks so day 1 lands under its weekday (Sunday = column 0).
        int leadingBlanks = current.atDay(1).getDayOfWeek().getValue() % 7; // Sun -> 0
        for (int i = 0; i < leadingBlanks; i++) {
            grid.add(new JLabel());
        }

        for (int day = 1; day <= current.lengthOfMonth(); day++) {
            LocalDate date = current.atDay(day);
            grid.add(buildDayButton(date, withEntries.contains(date), date.equals(today)));
        }

        grid.revalidate();
        grid.repaint();
    }

    private JButton buildDayButton(LocalDate date, boolean hasEntry, boolean isToday) {
        String label = hasEntry
                ? "<html><center>" + date.getDayOfMonth()
                        + "<br><font color='#1A7F37'>•</font></center></html>"
                : String.valueOf(date.getDayOfMonth());

        JButton button = new JButton(label);
        button.setVerticalAlignment(SwingConstants.TOP);
        button.setFocusPainted(false);

        if (isToday) {
            button.setBackground(TODAY_COLOR);
            button.setOpaque(true);
            button.setBorder(BorderFactory.createLineBorder(ENTRY_DOT_COLOR.darker(), 2));
        }

        button.addActionListener(e -> {
            JournalEditor editor = new JournalEditor(
                    javax.swing.SwingUtilities.getWindowAncestor(this), dao, date);
            editor.setVisible(true);   // modal: blocks until closed
            rebuild();                 // refresh dots after a save/delete
        });

        return button;
    }
}
