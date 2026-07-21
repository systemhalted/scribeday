package in.systemhalted.scribeday;

import java.time.LocalDate;
import java.util.List;

/**
 * Writing-streak arithmetic over the set of dates that have entries. The current
 * streak counts a run ending today <em>or yesterday</em>, so it isn't shown as
 * broken before today's entry has been written. Kept separate from the UI and
 * clock (today is a parameter) so the rules can be tested without JavaFX.
 */
public final class Streaks {

    /** Streak summary: consecutive-day runs and the number of days with entries. */
    public record Stats(int current, int longest, int totalDays) {
    }

    private Streaks() {
    }

    /**
     * @param sortedDates distinct entry dates in ascending order
     * @param today       the date to measure the current streak against
     */
    public static Stats compute(List<LocalDate> sortedDates, LocalDate today) {
        int longest = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate date : sortedDates) {
            run = (previous != null && previous.plusDays(1).equals(date)) ? run + 1 : 1;
            longest = Math.max(longest, run);
            previous = date;
        }
        boolean active = previous != null && !previous.isBefore(today.minusDays(1));
        return new Stats(active ? run : 0, longest, sortedDates.size());
    }
}
