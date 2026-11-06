package com.example.hskcompanion;

/** Deterministic priority for due items: past mistakes, lower mastery and overdue time. */
public final class ReviewFocus {
    private ReviewFocus() { }

    public static long priority(int stage, int pastMisses, long dueTime, long now) {
        long overdueDays = Math.min(30, Math.max(0, (now - dueTime) / ReviewScheduler.DAY));
        long mistakes = Math.min(12, Math.max(0, pastMisses));
        return mistakes * 500L + (6L - Math.min(6, Math.max(0, stage))) * 80L + overdueDays;
    }

    public static int freshWordLimit(boolean deep, int dueBacklog) {
        if (dueBacklog >= 8) return 0;
        if (dueBacklog >= 4) return 1;
        return deep ? 4 : 2;
    }
}
