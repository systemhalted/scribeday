package com.journal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

/**
 * All SQLite access for journal entries. This is the single point of database
 * contact — no SQL lives anywhere else in the app.
 *
 * <p>One entry per day, keyed by ISO date ({@code yyyy-MM-dd}). Saving upserts.
 */
public class JournalDao {

    private final String url;

    /**
     * @param dbFile path to the SQLite database file (created on first use)
     */
    public JournalDao(String dbFile) {
        this.url = "jdbc:sqlite:" + dbFile;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    /** Create the entries table if it does not already exist. */
    public void init() {
        String sql = """
                CREATE TABLE IF NOT EXISTS entries (
                    entry_date TEXT PRIMARY KEY,
                    content    TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
                """;
        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new JournalException("Failed to initialize database", e);
        }
    }

    /** @return the saved content for the date, or {@code null} if none exists. */
    public String load(LocalDate date) {
        String sql = "SELECT content FROM entries WHERE entry_date = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("content") : null;
            }
        } catch (SQLException e) {
            throw new JournalException("Failed to load entry for " + date, e);
        }
    }

    /** Insert or update the entry for the given date. */
    public void save(LocalDate date, String content) {
        String sql = """
                INSERT INTO entries (entry_date, content, updated_at)
                VALUES (?, ?, ?)
                ON CONFLICT(entry_date) DO UPDATE SET
                    content = excluded.content,
                    updated_at = excluded.updated_at
                """;
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            ps.setString(2, content);
            ps.setString(3, java.time.LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JournalException("Failed to save entry for " + date, e);
        }
    }

    /** Remove the entry for the given date (no-op if absent). */
    public void delete(LocalDate date) {
        String sql = "DELETE FROM entries WHERE entry_date = ?";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new JournalException("Failed to delete entry for " + date, e);
        }
    }

    /** @return the set of dates within the month that have a saved entry. */
    public Set<LocalDate> datesWithEntries(YearMonth month) {
        String sql = "SELECT entry_date FROM entries WHERE entry_date >= ? AND entry_date <= ?";
        Set<LocalDate> dates = new HashSet<>();
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, month.atDay(1).toString());
            ps.setString(2, month.atEndOfMonth().toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dates.add(LocalDate.parse(rs.getString("entry_date")));
                }
            }
        } catch (SQLException e) {
            throw new JournalException("Failed to query entries for " + month, e);
        }
        return dates;
    }

    /** Unchecked wrapper so UI code can surface DB errors without checked-exception noise. */
    public static class JournalException extends RuntimeException {
        public JournalException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
