package com.example.hskcompanion;

public final class Word {
    public final String id;
    public final String version;
    public final int level;
    public final String hanzi;
    public final String pinyin;
    public final String english;

    public Word(String version, int level, String hanzi, String pinyin, String english) {
        this.version = version;
        this.level = level;
        this.hanzi = hanzi;
        this.pinyin = pinyin;
        this.english = english;
        this.id = version + ":" + level + ":" + hanzi;
    }
}
