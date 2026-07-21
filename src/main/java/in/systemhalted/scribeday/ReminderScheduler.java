package in.systemhalted.scribeday;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/**
 * Fires a daily reminder callback (on the FX thread) at the configured time
 * while the app is running. The next-occurrence arithmetic is a pure static so
 * it can be tested without threads; the instance wraps a single daemon
 * scheduler thread.
 */
public class ReminderScheduler {

    private final Settings settings;
    private final Runnable onRemind;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> pending;

    public ReminderScheduler(Settings settings, Runnable onRemind) {
        this.settings = settings;
        this.onRemind = onRemind;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "daily-reminder");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Time until the next occurrence of {@code target}: later today if still
     * ahead, otherwise the same time tomorrow.
     */
    public static Duration untilNext(LocalTime target, LocalDateTime now) {
        LocalDateTime next = now.toLocalDate().atTime(target);
        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }
        return Duration.between(now, next);
    }

    /** (Re)schedule from current settings: cancels any pending firing first. */
    public synchronized void reschedule() {
        if (pending != null) {
            pending.cancel(false);
            pending = null;
        }
        if (!settings.reminderEnabled()) {
            return;
        }
        Duration delay = untilNext(settings.reminderTime(), LocalDateTime.now());
        pending = executor.schedule(this::fire, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void fire() {
        Platform.runLater(onRemind);
        reschedule();   // set up tomorrow's firing
    }

    /** Stop firing and release the scheduler thread. */
    public synchronized void stop() {
        if (pending != null) {
            pending.cancel(false);
        }
        executor.shutdownNow();
    }
}
