package com.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.DayOfWeek;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsTest {

    @TempDir
    Path tmp;

    private SettingsDao dao() {
        SettingsDao dao = new SettingsDao(tmp.resolve("settings.db").toString());
        dao.init();
        return dao;
    }

    @Test
    void returnsDefaultsWhenUnset() {
        Settings settings = new Settings(dao());
        assertEquals(Settings.DEFAULT_EDITOR_FONT_SIZE, settings.editorFontSize());
        assertEquals(DayOfWeek.SUNDAY, settings.weekStart());
    }

    @Test
    void roundTripsTypedValues() {
        Settings settings = new Settings(dao());
        settings.setEditorFontSize(18);
        settings.setWeekStart(DayOfWeek.MONDAY);
        assertEquals(18, settings.editorFontSize());
        assertEquals(DayOfWeek.MONDAY, settings.weekStart());
    }

    @Test
    void weekStartFallsBackWhenStoredValueIsInvalid() {
        SettingsDao raw = dao();
        raw.set(Settings.WEEK_START, "FUNDAY");   // not a real DayOfWeek
        assertEquals(DayOfWeek.SUNDAY, new Settings(raw).weekStart());
    }
}
