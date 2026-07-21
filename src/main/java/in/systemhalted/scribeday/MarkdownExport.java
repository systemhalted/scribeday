package in.systemhalted.scribeday;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Exports journal entries to Markdown files.
 */
public final class MarkdownExport {

    private static final DateTimeFormatter HEADING_DATE = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    private MarkdownExport() {
    }

    /** Format a single entry as a Markdown document. */
    public static String toMarkdown(LocalDate date, Entry entry) {
        String when = date.format(HEADING_DATE);
        boolean hasTitle = entry.title() != null && !entry.title().isBlank();
        StringBuilder md = new StringBuilder();
        if (hasTitle) {
            md.append("# ").append(entry.title()).append("\n\n");
            md.append("_").append(when).append("_\n\n");
        } else {
            md.append("# ").append(when).append("\n\n");
        }
        md.append(entry.content() == null ? "" : entry.content()).append("\n");
        return md.toString();
    }

    /** @return the Markdown file name for an entry, e.g. {@code 2026-06-09.md}. */
    public static String fileName(LocalDate date) {
        return date + ".md";
    }

    /**
     * Write every entry as its own Markdown file in {@code directory}.
     *
     * @return the number of entries exported
     */
    public static int exportAll(JournalDao dao, Path directory) {
        int count = 0;
        for (SearchHit hit : dao.recentEntries(Integer.MAX_VALUE)) {
            Entry entry = dao.loadEntry(hit.date());
            if (entry == null) {
                continue;
            }
            writeFile(directory.resolve(fileName(hit.date())), toMarkdown(hit.date(), entry));
            count++;
        }
        return count;
    }

    /** Write a single entry to a chosen file. */
    public static void exportEntry(Path file, LocalDate date, Entry entry) {
        writeFile(file, toMarkdown(date, entry));
    }

    private static void writeFile(Path file, String content) {
        try {
            Files.writeString(file, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write " + file, e);
        }
    }
}
