package in.systemhalted.scribeday;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Resolves where the app keeps its data, in a stable per-user location that
 * survives reinstalls and does not depend on the working directory.
 *
 * <ul>
 *   <li>Linux:   {@code $XDG_DATA_HOME/ScribeDay} or {@code ~/.local/share/ScribeDay}</li>
 *   <li>macOS:   {@code ~/Library/Application Support/ScribeDay}</li>
 *   <li>Windows: {@code %APPDATA%/ScribeDay} or {@code ~/AppData/Roaming/ScribeDay}</li>
 * </ul>
 */
public final class AppPaths {

    private static final String APP_DIR_NAME = "ScribeDay";
    private static final String DB_FILE_NAME = "journal.db";

    private AppPaths() {
    }

    /** The per-user data directory for this app (not guaranteed to exist yet). */
    public static Path dataDir() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home", ".");
        Path base;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            base = (appData != null && !appData.isBlank())
                    ? Path.of(appData)
                    : Path.of(home, "AppData", "Roaming");
        } else if (os.contains("mac") || os.contains("darwin")) {
            base = Path.of(home, "Library", "Application Support");
        } else {
            String xdg = System.getenv("XDG_DATA_HOME");
            base = (xdg != null && !xdg.isBlank())
                    ? Path.of(xdg)
                    : Path.of(home, ".local", "share");
        }
        return base.resolve(APP_DIR_NAME);
    }

    /**
     * The journal database path. Creates the data directory if needed and, on
     * first run, migrates a legacy {@code ~/journal.db} (from the early version
     * that wrote to the working directory) into the new location.
     */
    public static Path databaseFile() {
        Path dir = dataDir();
        try {
            Files.createDirectories(dir);
            Path db = dir.resolve(DB_FILE_NAME);
            if (Files.notExists(db)) {
                Path legacy = Path.of(System.getProperty("user.home", "."), DB_FILE_NAME);
                if (Files.exists(legacy)) {
                    Files.copy(legacy, db, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
            return db;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not prepare data directory at " + dir, e);
        }
    }
}
