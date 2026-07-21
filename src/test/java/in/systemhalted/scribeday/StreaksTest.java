package in.systemhalted.scribeday;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class StreaksTest {

    private final LocalDate today = LocalDate.of(2026, 7, 21);

    @Test
    void emptyHistoryHasNoStreaks() {
        Streaks.Stats s = Streaks.compute(List.of(), today);
        assertEquals(new Streaks.Stats(0, 0, 0), s);
    }

    @Test
    void singleEntryTodayStartsAStreak() {
        Streaks.Stats s = Streaks.compute(List.of(today), today);
        assertEquals(new Streaks.Stats(1, 1, 1), s);
    }

    @Test
    void runEndingYesterdayStillCountsAsCurrent() {
        List<LocalDate> dates = List.of(today.minusDays(3), today.minusDays(2), today.minusDays(1));
        Streaks.Stats s = Streaks.compute(dates, today);
        assertEquals(3, s.current());
        assertEquals(3, s.longest());
    }

    @Test
    void runEndingBeforeYesterdayIsBroken() {
        List<LocalDate> dates = List.of(today.minusDays(4), today.minusDays(3), today.minusDays(2));
        Streaks.Stats s = Streaks.compute(dates, today);
        assertEquals(0, s.current());
        assertEquals(3, s.longest());
    }

    @Test
    void longestRunInTheMiddleOfHistoryIsKept() {
        List<LocalDate> dates = List.of(
                today.minusDays(10), today.minusDays(9), today.minusDays(8), today.minusDays(7),
                today.minusDays(1), today);
        Streaks.Stats s = Streaks.compute(dates, today);
        assertEquals(2, s.current());
        assertEquals(4, s.longest());
        assertEquals(6, s.totalDays());
    }

    @Test
    void currentRunCanBeTheLongest() {
        List<LocalDate> dates = List.of(
                today.minusDays(6),
                today.minusDays(2), today.minusDays(1), today);
        Streaks.Stats s = Streaks.compute(dates, today);
        assertEquals(3, s.current());
        assertEquals(3, s.longest());
    }
}
