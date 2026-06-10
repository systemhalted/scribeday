package com.journal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A small key/value settings store backed by SQLite. Independent of the journal
 * entries schema — settings are stable key/value pairs that need no migrations.
 */
public class SettingsDao {

    private final String url;

    public SettingsDao(String dbFile) {
        this.url = "jdbc:sqlite:" + dbFile;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    /** Create the settings table if it does not already exist. */
    public void init() {
        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT NOT NULL)");
        } catch (SQLException e) {
            throw new JournalDao.JournalException("Failed to initialize settings", e);
        }
    }

    /** @return the stored value for the key, or {@code null} if absent. */
    public String get(String key) {
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement("SELECT value FROM settings WHERE key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("value") : null;
            }
        } catch (SQLException e) {
            throw new JournalDao.JournalException("Failed to read setting " + key, e);
        }
    }

    /** @return the stored value for the key, or {@code defaultValue} if absent. */
    public String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    /** @return the stored value parsed as an int, or {@code defaultValue} if absent or unparseable. */
    public int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** @return the stored value as a boolean, or {@code defaultValue} if absent. */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value != null ? Boolean.parseBoolean(value.trim()) : defaultValue;
    }

    /** Insert or update the value for the given key. */
    public void set(String key, String value) {
        String sql = "INSERT INTO settings (key, value) VALUES (?, ?) "
                + "ON CONFLICT(key) DO UPDATE SET value = excluded.value";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JournalDao.JournalException("Failed to save setting " + key, e);
        }
    }

    public void setInt(String key, int value) {
        set(key, Integer.toString(value));
    }

    public void setBoolean(String key, boolean value) {
        set(key, Boolean.toString(value));
    }
}
