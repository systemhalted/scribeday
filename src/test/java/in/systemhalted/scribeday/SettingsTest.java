package in.systemhalted.scribeday;

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

    @Test
    void themeDefaultsToLight() {
        assertEquals(Theme.LIGHT, new Settings(dao()).theme());
    }

    @Test
    void themeRoundTrips() {
        Settings settings = new Settings(dao());
        settings.setTheme(Theme.DARK);
        assertEquals(Theme.DARK, settings.theme());
    }

    @Test
    void themeFallsBackWhenStoredValueIsInvalid() {
        SettingsDao raw = dao();
        raw.set(Settings.THEME, "midnight");   // not a real Theme
        assertEquals(Theme.LIGHT, new Settings(raw).theme());
    }

    @Test
    void welcomeShownDefaultsToFalseAndRoundTrips() {
        Settings settings = new Settings(dao());
        assertEquals(false, settings.welcomeShown());
        settings.setWelcomeShown(true);
        assertEquals(true, settings.welcomeShown());
    }

    @Test
    void autoBackupDefaultsAreOnWeeklyKeepTen() {
        Settings settings = new Settings(dao());
        assertEquals(true, settings.autoBackupEnabled());
        assertEquals(7, settings.autoBackupIntervalDays());
        assertEquals(10, settings.autoBackupKeep());
    }

    @Test
    void reminderDefaultsToOffAtEight() {
        Settings settings = new Settings(dao());
        assertEquals(false, settings.reminderEnabled());
        assertEquals(java.time.LocalTime.of(20, 0), settings.reminderTime());
    }

    @Test
    void reminderSettingsRoundTrip() {
        Settings settings = new Settings(dao());
        settings.setReminderEnabled(true);
        settings.setReminderTime(java.time.LocalTime.of(7, 30));
        assertEquals(true, settings.reminderEnabled());
        assertEquals(java.time.LocalTime.of(7, 30), settings.reminderTime());
    }

    @Test
    void reminderTimeFallsBackWhenStoredValueIsInvalid() {
        SettingsDao raw = dao();
        raw.set(Settings.REMINDER_TIME, "half past nine");
        assertEquals(java.time.LocalTime.of(20, 0), new Settings(raw).reminderTime());
    }

    @Test
    void trayDefaultsToOffAndRoundTrips() {
        Settings settings = new Settings(dao());
        assertEquals(false, settings.trayEnabled());
        settings.setTrayEnabled(true);
        assertEquals(true, settings.trayEnabled());
    }

    @Test
    void autoBackupSettingsRoundTrip() {
        Settings settings = new Settings(dao());
        settings.setAutoBackupEnabled(false);
        settings.setAutoBackupIntervalDays(3);
        settings.setAutoBackupKeep(5);
        assertEquals(false, settings.autoBackupEnabled());
        assertEquals(3, settings.autoBackupIntervalDays());
        assertEquals(5, settings.autoBackupKeep());
    }
}
