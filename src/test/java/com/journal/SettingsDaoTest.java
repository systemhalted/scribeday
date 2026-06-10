package com.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsDaoTest {

    @TempDir
    Path tmp;

    private SettingsDao freshDao() {
        SettingsDao dao = new SettingsDao(tmp.resolve("settings.db").toString());
        dao.init();
        return dao;
    }

    @Test
    void getReturnsNullWhenAbsent() {
        assertNull(freshDao().get("missing"));
    }

    @Test
    void getReturnsDefaultWhenAbsent() {
        assertEquals("fallback", freshDao().get("missing", "fallback"));
    }

    @Test
    void setThenGetRoundTrips() {
        SettingsDao dao = freshDao();
        dao.set("theme", "dark");
        assertEquals("dark", dao.get("theme"));
    }

    @Test
    void setOverwritesExistingValue() {
        SettingsDao dao = freshDao();
        dao.set("theme", "light");
        dao.set("theme", "dark");
        assertEquals("dark", dao.get("theme"));
    }

    @Test
    void getIntParsesStoredValueAndFallsBackOnAbsentOrInvalid() {
        SettingsDao dao = freshDao();
        assertEquals(14, dao.getInt("font", 14));   // absent -> default
        dao.set("font", "18");
        assertEquals(18, dao.getInt("font", 14));    // stored -> parsed
        dao.set("font", "not-a-number");
        assertEquals(14, dao.getInt("font", 14));    // invalid -> default
    }

    @Test
    void getBooleanRoundTrips() {
        SettingsDao dao = freshDao();
        assertTrue(dao.getBoolean("flag", true));    // absent -> default
        dao.setBoolean("flag", false);
        assertEquals(false, dao.getBoolean("flag", true));
    }

    @Test
    void valuesPersistAcrossInstances() {
        Path db = tmp.resolve("persist.db");
        SettingsDao first = new SettingsDao(db.toString());
        first.init();
        first.set("week.start", "MONDAY");

        SettingsDao second = new SettingsDao(db.toString());
        second.init();
        assertEquals("MONDAY", second.get("week.start"));
    }

    @Test
    void initIsIdempotent() {
        SettingsDao dao = freshDao();
        dao.set("k", "v");
        dao.init();   // must not wipe existing settings
        assertEquals("v", dao.get("k"));
    }
}
