package com.example.hskcompanion;

public final class Exercise {
    public enum Kind { INTRO, RECALL, LISTEN, TYPE, CLOZE, SHADOW }
    public final Kind kind;
    public final Word word;
    public final boolean isRetry;
    public Exercise(Kind kind, Word word) { this(kind, word, false); }
    public Exercise(Kind kind, Word word, boolean retry) {
        this.kind = kind; this.word = word; this.isRetry = retry;
    }
}
