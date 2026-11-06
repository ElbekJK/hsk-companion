package com.example.hskcompanion;

public final class SentenceExample {
    public final String target;
    public final String chinese;
    public final String pinyin;
    public final String english;
    public SentenceExample(String target, String chinese, String pinyin, String english) {
        this.target = target; this.chinese = chinese; this.pinyin = pinyin; this.english = english;
    }
    public String cloze() {
        int index = chinese.indexOf(target);
        return index < 0 ? chinese : chinese.substring(0,index) + "＿＿" + chinese.substring(index + target.length());
    }
}
