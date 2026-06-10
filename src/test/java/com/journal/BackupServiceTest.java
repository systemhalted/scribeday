package com.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackupServiceTest {

    @TempDir
    Path tmp;

    private Path journalDbWith(String fileName, LocalDate date, String content) {
        Path db = tmp.resolve(fileName);
        JournalDao dao = new JournalDao(db.toString());
        dao.init();
        dao.saveEntry(date, "t", content);
        return db;
    }

    @Test
    void backupFileNameIncludesTimestamp() {
        String name = BackupService.backupFileName(LocalDateTime.of(2026, 6, 9, 21, 34, 5));
        assertEquals("calendar-journal-20260609-213405.db", name);
    }

    @Test
    void backupCopiesDatabaseToTimestampedFileInDirectory() throws Exception {
        Path source = journalDbWith("journal.db", LocalDate.of(2026, 6, 9), "hello");
        Path dir = Files.createDirectory(tmp.resolve("backups"));

        Path result = BackupService.backup(source, dir, LocalDateTime.of(2026, 6, 9, 21, 34, 5));

        assertEquals(dir.resolve("calendar-journal-20260609-213405.db"), result);
        assertTrue(Files.exists(result));
        assertTrue(BackupService.isJournalDatabase(result));
    }

    @Test
    void isJournalDatabaseTrueForRealDatabase() {
        Path db = journalDbWith("real.db", LocalDate.of(2026, 6, 9), "x");
        assertTrue(BackupService.isJournalDatabase(db));
    }

    @Test
    void isJournalDatabaseFalseForArbitraryFile() throws Exception {
        Path junk = tmp.resolve("notes.txt");
        Files.writeString(junk, "this is not a database");
        assertFalse(BackupService.isJournalDatabase(junk));
    }

    @Test
    void restoreReplacesTargetWithBackupContents() {
        Path backup = journalDbWith("backup.db", LocalDate.of(2025, 1, 1), "from backup");
        Path target = journalDbWith("current.db", LocalDate.of(2026, 6, 9), "current data");

        BackupService.restore(backup, target);

        JournalDao restored = new JournalDao(target.toString());
        restored.init();
        assertEquals("from backup", restored.load(LocalDate.of(2025, 1, 1)));
        assertEquals(null, restored.load(LocalDate.of(2026, 6, 9)));
    }

    @Test
    void restoreRejectsAFileThatIsNotAJournalDatabase() throws Exception {
        Path junk = tmp.resolve("junk.db");
        Files.writeString(junk, "garbage");
        Path target = journalDbWith("current.db", LocalDate.of(2026, 6, 9), "keep me");

        assertThrows(IllegalArgumentException.class, () -> BackupService.restore(junk, target));
    }
}
