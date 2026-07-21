package in.systemhalted.scribeday;

import java.time.LocalDate;

/**
 * The autosave persistence policy for a day's entry: if both title and content
 * are blank the entry is removed; otherwise it is upserted (a blank title is
 * stored as {@code null}). Kept separate from the editor UI so the rule can be
 * tested without the JavaFX toolkit.
 */
public final class EntryAutosave {

    private EntryAutosave() {
    }

    /**
     * Persist the title/content for {@code date}: upsert if either has text, else
     * delete. Leaves any stored mood untouched.
     */
    public static void persist(JournalDao dao, LocalDate date, String title, String content) {
        if (isBlank(title) && isBlank(content)) {
            dao.delete(date);
        } else {
            dao.saveEntry(date, isBlank(title) ? null : title, content);
        }
    }

    /**
     * Persist the title/content/mood for {@code date}: upsert if title or content
     * has text, else delete. A mood alone does not keep an entry alive.
     */
    public static void persist(JournalDao dao, LocalDate date, String title, String content, Mood mood) {
        if (isBlank(title) && isBlank(content)) {
            dao.delete(date);
        } else {
            dao.saveEntry(date, isBlank(title) ? null : title, content, mood);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
