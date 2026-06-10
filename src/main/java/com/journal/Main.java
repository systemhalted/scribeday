package com.journal;

import java.time.YearMonth;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * Application entry point. Wires the SQLite DAO to the calendar UI and shows
 * the main window.
 */
public class Main {

    public static void main(String[] args) {
        JournalDao dao = new JournalDao("journal.db");
        dao.init();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to the default look and feel.
            }

            JFrame frame = new JFrame("Calendar Journal");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.add(new CalendarPanel(dao, YearMonth.now()));
            frame.setSize(560, 480);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
