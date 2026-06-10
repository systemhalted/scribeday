package com.journal;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EntryAutosaveTest {

    @TempDir
    Path tmp;

    private final LocalDate date = LocalDate.of(2026, 6, 9);

    private JournalDao freshDao() {
        JournalDao dao = new JournalDao(tmp.resolve("journal.db").toString());
        dao.init();
        return dao;
    }

    @Test
    void nonBlankTextIsSaved() {
        JournalDao dao = freshDao();
        EntryAutosave.persist(dao, date, "a thought");
        assertEquals("a thought", dao.load(date));
    }

    @Test
    void blankTextRemovesExistingEntry() {
        JournalDao dao = freshDao();
        dao.save(date, "to be cleared");
        EntryAutosave.persist(dao, date, "");
        assertNull(dao.load(date));
    }

    @Test
    void whitespaceOnlyCountsAsBlank() {
        JournalDao dao = freshDao();
        dao.save(date, "something");
        EntryAutosave.persist(dao, date, "   \n  ");
        assertNull(dao.load(date));
    }

    @Test
    void blankTextOnAbsentEntryDoesNotCreateOne() {
        JournalDao dao = freshDao();
        EntryAutosave.persist(dao, date, "");
        assertNull(dao.load(date));
    }
}
