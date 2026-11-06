package com.example.hskcompanion;

/** Prevents repeated success within one local study day from inflating mastery. */
public final class ReviewPolicy {
    private ReviewPolicy() { }

    public static int stageAfter(int previous, int rating, long lastPromotionDay, long day) {
        if (rating >= 2 && lastPromotionDay == day) return previous;
        return ReviewScheduler.nextStage(previous, rating);
    }

    public static long nextDue(long now, int previous, int rating, int finalStage) {
        if (rating <= 1) return ReviewScheduler.dueAt(now, previous, rating);
        return now + ReviewScheduler.intervalForStage(finalStage);
    }
}
