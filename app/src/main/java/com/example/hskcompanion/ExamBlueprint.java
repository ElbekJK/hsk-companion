package com.example.hskcompanion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
public final class ExamBlueprint {
    private ExamBlueprint() {}
    public static int listeningCount(int level){return level == 1 ? 20 : 35;}
    public static int readingCount(int level){return level == 1 ? 20 : 25;}
    public static int total(int level){return listeningCount(level) + readingCount(level);}
    public static int minutes(int level){return level == 1 ? 40 : 55;}
    public static List<Word> questions(List<Word> vocabulary,int level,Random random){
        if(vocabulary.size()<total(level)) throw new IllegalArgumentException("Vocabulary too small");
        List<Word> shuffled = new ArrayList<>(vocabulary);
        Collections.shuffle(shuffled,random);
        return new ArrayList<>(shuffled.subList(0,total(level)));
    }
}
