package com.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MarkdownExportTest {

    @TempDir
    Path tmp;

    @Test
    void formatsEntryWithTitle() {
        String md = MarkdownExport.toMarkdown(LocalDate.of(2026, 6, 9), new Entry("My Day", "Hello world"));
        assertTrue(md.contains("# My Day"), md);
        assertTrue(md.contains("June 9, 2026"), md);
        assertTrue(md.contains("Hello world"), md);
    }

    @Test
    void formatsEntryWithoutTitleUsingTheDateAsHeading() {
        String md = MarkdownExport.toMarkdown(LocalDate.of(2026, 6, 9), new Entry(null, "Body text"));
        assertTrue(md.contains("# June 9, 2026"), md);
        assertTrue(md.contains("Body text"), md);
    }

    @Test
    void fileNameIsIsoDateWithMdExtension() {
        assertEquals("2026-06-09.md", MarkdownExport.fileName(LocalDate.of(2026, 6, 9)));
    }

    @Test
    void exportAllWritesOneFilePerEntry() throws Exception {
        JournalDao dao = new JournalDao(tmp.resolve("journal.db").toString());
        dao.init();
        dao.saveEntry(LocalDate.of(2026, 6, 9), "Day one", "first body");
        dao.saveEntry(LocalDate.of(2026, 6, 10), null, "second body");
        Path out = Files.createDirectory(tmp.resolve("export"));

        int count = MarkdownExport.exportAll(dao, out);

        assertEquals(2, count);
        assertTrue(Files.exists(out.resolve("2026-06-09.md")));
        assertTrue(Files.exists(out.resolve("2026-06-10.md")));
        assertTrue(Files.readString(out.resolve("2026-06-09.md")).contains("first body"));
    }
}
