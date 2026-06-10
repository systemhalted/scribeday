package com.journal;

import java.time.LocalDate;

/**
 * The autosave persistence policy for a day's entry: non-blank text is saved,
 * blank text removes the entry. Kept separate from the editor UI so the rule can
 * be tested without the JavaFX toolkit.
 */
public final class EntryAutosave {

    private EntryAutosave() {
    }

    /** Persist {@code text} for {@code date}: upsert if it has content, otherwise delete. */
    public static void persist(JournalDao dao, LocalDate date, String text) {
        if (text == null || text.isBlank()) {
            dao.delete(date);
        } else {
            dao.save(date, text);
        }
    }
}
