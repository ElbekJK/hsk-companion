package com.example.hskcompanion;

import java.util.List;
public final class GrammarRule {
    public final String id, title, pattern, explain, hanzi, pinyin, translation, question, feedback;
    public final int level, answer;
    public final List<String> choices;
    public GrammarRule(String id,int level,String title,String pattern,String explain,String hanzi,String pinyin,String translation,String question,List<String> choices,int answer,String feedback) {
        this.id=id;this.level=level;this.title=title;this.pattern=pattern;this.explain=explain;
        this.hanzi=hanzi;this.pinyin=pinyin;this.translation=translation;this.question=question;
        this.choices=choices;this.answer=answer;this.feedback=feedback;
    }
}
