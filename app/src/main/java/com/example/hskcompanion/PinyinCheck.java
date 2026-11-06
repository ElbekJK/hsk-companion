package com.example.hskcompanion;

import java.text.Normalizer;
import java.util.Locale;

/** Tone-insensitive for beginner typing; TTS + tone-marked answer provide tone feedback. */
public final class PinyinCheck {
    private PinyinCheck() { }
    public static String normalized(String input) {
        String s = input.toLowerCase(Locale.ROOT).replace('ü', 'v').replace("u:", "v");
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replace("u\u0308", "v");
        s = s.replaceAll("\\p{M}", "");
        return s.replaceAll("[0-9\\s'·.,!?\u00b7-]", "");
    }
    public static boolean matches(String attempt, String pinyin, String hanzi) {
        if (attempt == null || attempt.trim().isEmpty()) return false;
        if (attempt.trim().equals(hanzi)) return true;
        String test = normalized(attempt);
        if (test.isEmpty()) return false;
        for (String variant : pinyin.split("[;/|]") ) {
            if (test.equals(normalized(variant))) return true;
        }
        return false;
    }
    /** Tone-aware comparison for pinyin with numbered or accented syllables.
        This intentionally does not treat Hanzi as evidence of correct tones. */
    public static boolean toneMatches(String input, String expected) {
        if (input == null || input.trim().isEmpty()) return false;
        for (String v:expected.split("[;/|]")) if (toneKey(input).equals(toneKey(v))) return true;
        return false;
    }
    public static String toneKey(String pinyin) {
        StringBuilder result=new StringBuilder();
        String lower=pinyin.toLowerCase(Locale.ROOT).replace("u:","ü");
        // A syllable separated by a space or an apostrophe carries a tone marker.
        for(String segment:lower.split("[\\s'·.,!?-]+")) {
            if(segment.isEmpty())continue;
            String decomposed=Normalizer.normalize(segment,Normalizer.Form.NFD);
            StringBuilder base=new StringBuilder();
            int tone=5;
            for(int i=0;i<decomposed.length();i++) {
                char ch=decomposed.charAt(i);
                if(ch=='\u0304')tone=1;
                else if(ch=='\u0301')tone=2;
                else if(ch=='\u030c')tone=3;
                else if(ch=='\u0300')tone=4;
                else if(ch=='\u0308'){
                    if(base.length()>0 && base.charAt(base.length()-1)=='u')base.setCharAt(base.length()-1,'v');
                } else if(ch>='0' && ch<='5')tone=(ch=='0'?5:ch-'0');
                else if(!Character.isWhitespace(ch))base.append(ch);
            }
            result.append(base).append(tone).append('|');
        }
        return result.toString();
    }
}
