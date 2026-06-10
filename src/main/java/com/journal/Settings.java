package com.journal;

import java.time.DayOfWeek;

/**
 * Typed, app-specific view over the generic {@link SettingsDao} key/value store.
 * UI code reads and writes preferences through here rather than touching raw keys.
 */
public class Settings {

    public static final String EDITOR_FONT_SIZE = "editor.font.size";
    public static final String WEEK_START = "calendar.week.start";

    public static final int DEFAULT_EDITOR_FONT_SIZE = 14;
    public static final DayOfWeek DEFAULT_WEEK_START = DayOfWeek.SUNDAY;

    private final SettingsDao dao;

    public Settings(SettingsDao dao) {
        this.dao = dao;
    }

    public int editorFontSize() {
        return dao.getInt(EDITOR_FONT_SIZE, DEFAULT_EDITOR_FONT_SIZE);
    }

    public void setEditorFontSize(int px) {
        dao.setInt(EDITOR_FONT_SIZE, px);
    }

    public DayOfWeek weekStart() {
        String stored = dao.get(WEEK_START, DEFAULT_WEEK_START.name());
        try {
            return DayOfWeek.valueOf(stored);
        } catch (IllegalArgumentException e) {
            return DEFAULT_WEEK_START;
        }
    }

    public void setWeekStart(DayOfWeek day) {
        dao.set(WEEK_START, day.name());
    }
}
