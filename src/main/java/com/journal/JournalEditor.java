package com.journal;

import java.awt.BorderLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * The "notepad": a modal dialog with a text area for one day's journal entry.
 * Pre-fills any existing entry; offers Save, Delete, and Cancel.
 */
public class JournalEditor extends JDialog {

    private static final DateTimeFormatter TITLE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    public JournalEditor(java.awt.Window owner, JournalDao dao, LocalDate date) {
        super(owner, "Journal — " + date.format(TITLE_FORMAT), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        String existing = dao.load(date);

        JTextArea textArea = new JTextArea(existing == null ? "" : existing, 18, 50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        textArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            dao.save(date, textArea.getText());
            dispose();
        });

        JButton deleteButton = new JButton("Delete");
        deleteButton.setEnabled(existing != null);
        deleteButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Delete the journal entry for this day?",
                    "Confirm delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                dao.delete(date);
                dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel();
        buttons.add(deleteButton);
        buttons.add(Box.createHorizontalStrut(20));
        buttons.add(cancelButton);
        buttons.add(saveButton);

        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveButton);
        pack();
        setLocationRelativeTo(owner);
    }
}
