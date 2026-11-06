package com.example.hskcompanion;
import java.util.List;
public final class CurriculumUnit {
    public final String id,title,goal;
    public final int level;
    public final List<String> grammar,dialogues;
    public CurriculumUnit(String id,int level,String title,String goal,List<String> grammar,List<String> dialogues){
        this.id=id;this.level=level;this.title=title;this.goal=goal;
        this.grammar=grammar;this.dialogues=dialogues;
    }
}
