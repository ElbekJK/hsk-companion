package com.example.hskcompanion;
import java.util.*;
/** Two different tones with otherwise identical romanization; no non-HSK terms. */
public final class ToneTrainer {
    private ToneTrainer() { }
    public static List<Word[]> pairs(List<Word> words) {
        Map<String,List<Word>> groups=new LinkedHashMap<>();
        for(Word w:words) {
            if(w.hanzi.codePointCount(0,w.hanzi.length())!=1)continue;
            String key=PinyinCheck.normalized(w.pinyin);
            if(key.isEmpty() || key.contains("/"))continue;
            if(!groups.containsKey(key))groups.put(key,new ArrayList<>());
            groups.get(key).add(w);
        }
        List<Word[]> pairs=new ArrayList<>();
        for(List<Word> candidates:groups.values()) {
            for(int i=0;i<candidates.size();i++) for(int j=i+1;j<candidates.size();j++) {
                Word a=candidates.get(i),b=candidates.get(j);
                if(!PinyinCheck.toneKey(a.pinyin).equals(PinyinCheck.toneKey(b.pinyin)))
                    pairs.add(new Word[]{a,b});
            }
        }
        return pairs;
    }
}
