package in.systemhalted.scribeday;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Backup and restore for the single-file SQLite journal database.
 */
public final class BackupService {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private BackupService() {
    }

    /** @return a timestamped backup file name like {@code scribeday-20260609-213405.db}. */
    public static String backupFileName(LocalDateTime when) {
        return "scribeday-" + STAMP.format(when) + ".db";
    }

    /**
     * Copy the journal database into {@code directory} under a timestamped name.
     *
     * @return the path of the created backup file
     */
    public static Path backup(Path source, Path directory, LocalDateTime when) {
        Path destination = directory.resolve(backupFileName(when));
        try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            return destination;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to back up to " + destination, e);
        }
    }

    /** @return true if the file looks like a journal database (a readable SQLite DB with an entries table). */
    public static boolean isJournalDatabase(Path file) {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name='entries'")) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Replace {@code target} with the contents of {@code backupFile}.
     *
     * @throws IllegalArgumentException if {@code backupFile} is not a journal database
     */
    public static void restore(Path backupFile, Path target) {
        if (!isJournalDatabase(backupFile)) {
            throw new IllegalArgumentException("Not a ScribeDay database: " + backupFile);
        }
        try {
            Files.copy(backupFile, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to restore from " + backupFile, e);
        }
    }
}
