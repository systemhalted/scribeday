package in.systemhalted.scribeday;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Startup policy for scheduled backups: copy the database into the backups
 * directory when the newest backup there is missing or older than the
 * configured interval, then prune old backups beyond the keep count. Only
 * files named by {@link BackupService#backupFileName} are considered or
 * deleted. The clock is a parameter so the policy is testable.
 */
public final class AutoBackup {

    private AutoBackup() {
    }

    /**
     * @return the backup created, or empty when one was recent enough already
     */
    public static Optional<Path> runIfDue(Path db, Path backupsDir, int intervalDays, int keep,
                                          LocalDateTime now) {
        LocalDateTime latest = latestBackupTime(backupsDir);
        if (latest != null && latest.isAfter(now.minusDays(intervalDays))) {
            return Optional.empty();
        }
        Path created = BackupService.backup(db, backupsDir, now);
        prune(backupsDir, keep);
        return Optional.of(created);
    }

    /** The timestamp of the newest backup in the directory, or {@code null} if none. */
    private static LocalDateTime latestBackupTime(Path backupsDir) {
        return backups(backupsDir).stream()
                .map(p -> BackupService.parseBackupTimestamp(p.getFileName().toString()))
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    /** Delete the oldest backups beyond {@code keep}. */
    private static void prune(Path backupsDir, int keep) {
        List<Path> backups = backups(backupsDir);
        backups.sort(Comparator.comparing(
                (Path p) -> BackupService.parseBackupTimestamp(p.getFileName().toString())).reversed());
        for (Path old : backups.subList(Math.min(keep, backups.size()), backups.size())) {
            try {
                Files.deleteIfExists(old);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to prune old backup " + old, e);
            }
        }
    }

    private static List<Path> backups(Path backupsDir) {
        try (Stream<Path> files = Files.list(backupsDir)) {
            return files
                    .filter(p -> BackupService.parseBackupTimestamp(p.getFileName().toString()) != null)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list backups in " + backupsDir, e);
        }
    }
}
