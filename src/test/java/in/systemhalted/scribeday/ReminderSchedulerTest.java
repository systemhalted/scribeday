package in.systemhalted.scribeday;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReminderSchedulerTest {

    private final LocalDateTime morning = LocalDateTime.of(2026, 7, 21, 9, 0);

    @Test
    void targetLaterTodayIsHoursAway() {
        Duration d = ReminderScheduler.untilNext(LocalTime.of(20, 0), morning);
        assertEquals(Duration.ofHours(11), d);
    }

    @Test
    void targetAlreadyPassedRollsToTomorrow() {
        Duration d = ReminderScheduler.untilNext(LocalTime.of(8, 0), morning);
        assertEquals(Duration.ofHours(23), d);
    }

    @Test
    void targetExactlyNowRollsToTomorrow() {
        Duration d = ReminderScheduler.untilNext(LocalTime.of(9, 0), morning);
        assertEquals(Duration.ofHours(24), d);
    }

    @Test
    void eveningTargetJustBeforeMidnightStaysToday() {
        LocalDateTime lateEvening = LocalDateTime.of(2026, 7, 21, 23, 50);
        Duration d = ReminderScheduler.untilNext(LocalTime.of(23, 55), lateEvening);
        assertEquals(Duration.ofMinutes(5), d);
    }
}
