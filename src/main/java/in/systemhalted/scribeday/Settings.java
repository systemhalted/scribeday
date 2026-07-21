package in.systemhalted.scribeday;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Typed, app-specific view over the generic {@link SettingsDao} key/value store.
 * UI code reads and writes preferences through here rather than touching raw keys.
 */
public class Settings {

    public static final String EDITOR_FONT_SIZE = "editor.font.size";
    public static final String WEEK_START = "calendar.week.start";
    public static final String THEME = "app.theme";
    public static final String WELCOME_SHOWN = "app.welcome.shown";
    public static final String AUTO_BACKUP_ENABLED = "backup.auto.enabled";
    public static final String AUTO_BACKUP_INTERVAL_DAYS = "backup.auto.interval.days";
    public static final String AUTO_BACKUP_KEEP = "backup.auto.keep";
    public static final String REMINDER_ENABLED = "reminder.enabled";
    public static final String REMINDER_TIME = "reminder.time";

    public static final int DEFAULT_EDITOR_FONT_SIZE = 14;
    public static final DayOfWeek DEFAULT_WEEK_START = DayOfWeek.SUNDAY;
    public static final Theme DEFAULT_THEME = Theme.LIGHT;
    public static final boolean DEFAULT_AUTO_BACKUP_ENABLED = true;
    public static final int DEFAULT_AUTO_BACKUP_INTERVAL_DAYS = 7;
    public static final int DEFAULT_AUTO_BACKUP_KEEP = 10;
    public static final LocalTime DEFAULT_REMINDER_TIME = LocalTime.of(20, 0);

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

    public Theme theme() {
        return Theme.fromName(dao.get(THEME, DEFAULT_THEME.name()), DEFAULT_THEME);
    }

    public void setTheme(Theme theme) {
        dao.set(THEME, theme.name());
    }

    public boolean welcomeShown() {
        return dao.getBoolean(WELCOME_SHOWN, false);
    }

    public void setWelcomeShown(boolean shown) {
        dao.setBoolean(WELCOME_SHOWN, shown);
    }

    public boolean autoBackupEnabled() {
        return dao.getBoolean(AUTO_BACKUP_ENABLED, DEFAULT_AUTO_BACKUP_ENABLED);
    }

    public void setAutoBackupEnabled(boolean enabled) {
        dao.setBoolean(AUTO_BACKUP_ENABLED, enabled);
    }

    public int autoBackupIntervalDays() {
        return dao.getInt(AUTO_BACKUP_INTERVAL_DAYS, DEFAULT_AUTO_BACKUP_INTERVAL_DAYS);
    }

    public void setAutoBackupIntervalDays(int days) {
        dao.setInt(AUTO_BACKUP_INTERVAL_DAYS, days);
    }

    public boolean reminderEnabled() {
        return dao.getBoolean(REMINDER_ENABLED, false);
    }

    public void setReminderEnabled(boolean enabled) {
        dao.setBoolean(REMINDER_ENABLED, enabled);
    }

    public LocalTime reminderTime() {
        String stored = dao.get(REMINDER_TIME, DEFAULT_REMINDER_TIME.toString());
        try {
            return LocalTime.parse(stored);
        } catch (DateTimeParseException e) {
            return DEFAULT_REMINDER_TIME;
        }
    }

    public void setReminderTime(LocalTime time) {
        dao.set(REMINDER_TIME, time.toString());
    }

    public int autoBackupKeep() {
        return dao.getInt(AUTO_BACKUP_KEEP, DEFAULT_AUTO_BACKUP_KEEP);
    }

    public void setAutoBackupKeep(int keep) {
        dao.setInt(AUTO_BACKUP_KEEP, keep);
    }
}
