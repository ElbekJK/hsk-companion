package com.example.hskcompanion;

/** A simple, inspectable Leitner-style scheduler, not a validated memory predictor. */
public final class ReviewScheduler {
    public static final long MINUTE = 60_000L;
    public static final long DAY = 24L * 60 * MINUTE;
    private static final long[] INTERVALS = {
        10 * MINUTE, 1 * DAY, 3 * DAY, 7 * DAY, 14 * DAY, 30 * DAY, 60 * DAY
    };
    private ReviewScheduler() { }

    /** ratings: 0=again, 1=hard, 2=good, 3=easy */
    public static int nextStage(int previous, int rating) {
        int stage = Math.max(0, Math.min(previous, 6));
        if (rating == 0) return 0;
        if (rating == 1) return Math.max(1, stage);
        if (rating == 2) return Math.min(6, stage + 1);
        if (rating == 3) return Math.min(6, stage + 2);
        throw new IllegalArgumentException("Unknown rating: " + rating);
    }

    public static long intervalForStage(int stage) {
        return INTERVALS[Math.max(0, Math.min(6, stage))];
    }

    public static long interval(int previous, int rating) {
        int stage = nextStage(previous, rating);
        if (rating == 1) return stage <= 1 ? 8 * 60 * MINUTE : INTERVALS[stage - 1];
        return INTERVALS[stage];
    }

    public static long dueAt(long now, int previous, int rating) {
        return now + interval(previous, rating);
    }
}
