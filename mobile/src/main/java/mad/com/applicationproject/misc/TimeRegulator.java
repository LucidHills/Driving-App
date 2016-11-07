package mad.com.applicationproject.misc;

import java.util.concurrent.TimeUnit;

/**
 * Measures time since creation or last checkTime() which returned true
 */
public class TimeRegulator {

    private final long periodMilliseconds; // how long to wait before returning true to a check
    private long lastTick = 0L; // 1970 will be long before the current time
    private long previousTick = 0L; // used to undoUpdate update

    public TimeRegulator(long periodMilliseconds) {
        this.periodMilliseconds = periodMilliseconds;
    }

    public boolean checkTime() {
        long now = System.nanoTime();
        long diffMilliseconds = TimeUnit.NANOSECONDS.toMillis(now - lastTick);
        if (diffMilliseconds < periodMilliseconds) return false;
        previousTick = lastTick;
        lastTick = now;
        return true;
    }

    public void undoUpdate() {
        lastTick = previousTick;
    }
}
