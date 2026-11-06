package com.example.hskcompanion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class QuizEngine {
    private QuizEngine() {}

    public static List<Word> questions(List<Word> vocabulary, int limit, Random random) {
        List<Word> result = new ArrayList<>(vocabulary);
        Collections.shuffle(result, random);
        return result.subList(0, Math.min(limit, result.size()));
    }

    public static List<Word> choices(Word answer, List<Word> vocabulary, Random random) {
        ArrayList<Word> pool = new ArrayList<>(vocabulary);
        Collections.shuffle(pool, random);
        ArrayList<Word> result = new ArrayList<>();
        Set<String> meanings = new HashSet<>();
        result.add(answer);
        meanings.add(answer.english);
        for (Word candidate : pool) {
            if (!candidate.id.equals(answer.id) && meanings.add(candidate.english)) result.add(candidate);
            if (result.size() == 4) break;
        }
        Collections.shuffle(result, random);
        return result;
    }
}
