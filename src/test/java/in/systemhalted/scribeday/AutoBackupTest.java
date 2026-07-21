package in.systemhalted.scribeday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AutoBackupTest {

    @TempDir
    Path tmp;

    private final LocalDateTime now = LocalDateTime.of(2026, 7, 21, 9, 0);

    private Path journalDb() throws IOException {
        Path db = tmp.resolve("journal.db");
        JournalDao dao = new JournalDao(db.toString());
        dao.init();
        return db;
    }

    private Path backupsDir() throws IOException {
        return Files.createDirectories(tmp.resolve("backups"));
    }

    @Test
    void backsUpWhenDirectoryIsEmpty() throws IOException {
        Path db = journalDb();
        Path dir = backupsDir();
        Optional<Path> created = AutoBackup.runIfDue(db, dir, 7, 10, now);
        assertTrue(created.isPresent());
        assertTrue(Files.exists(created.get()));
        assertEquals(BackupService.backupFileName(now), created.get().getFileName().toString());
    }

    @Test
    void skipsWhenARecentBackupExists() throws IOException {
        Path db = journalDb();
        Path dir = backupsDir();
        Files.createFile(dir.resolve(BackupService.backupFileName(now.minusDays(2))));
        assertTrue(AutoBackup.runIfDue(db, dir, 7, 10, now).isEmpty());
    }

    @Test
    void runsWhenLatestBackupIsOlderThanTheInterval() throws IOException {
        Path db = journalDb();
        Path dir = backupsDir();
        Files.createFile(dir.resolve(BackupService.backupFileName(now.minusDays(8))));
        assertTrue(AutoBackup.runIfDue(db, dir, 7, 10, now).isPresent());
    }

    @Test
    void pruneKeepsOnlyTheNewestBackups() throws IOException {
        Path db = journalDb();
        Path dir = backupsDir();
        for (int i = 10; i >= 8; i--) {
            Files.createFile(dir.resolve(BackupService.backupFileName(now.minusDays(i))));
        }
        AutoBackup.runIfDue(db, dir, 7, 2, now);   // creates a 4th, keep=2
        List<String> remaining;
        try (var files = Files.list(dir)) {
            remaining = files.map(p -> p.getFileName().toString()).sorted().toList();
        }
        assertEquals(List.of(
                BackupService.backupFileName(now.minusDays(8)),
                BackupService.backupFileName(now)), remaining);
    }

    @Test
    void ignoresForeignFilesWhenCheckingAndPruning() throws IOException {
        Path db = journalDb();
        Path dir = backupsDir();
        Files.createFile(dir.resolve("notes.txt"));
        Files.createFile(dir.resolve("scribeday-notatimestamp.db"));
        Optional<Path> created = AutoBackup.runIfDue(db, dir, 7, 1, now);
        assertTrue(created.isPresent());
        assertTrue(Files.exists(dir.resolve("notes.txt")));
        assertTrue(Files.exists(dir.resolve("scribeday-notatimestamp.db")));
    }
}
