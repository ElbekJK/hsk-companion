package com.example.hskcompanion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Plans bounded, mixed-mode sessions with an introduction preceding new-word recall. */
public final class SessionPlanner {
    private SessionPlanner() { }
    public static List<Exercise> plan(List<Word> due, List<Word> unseen,
                                       List<Word> familiar, boolean deep, Random random) {
        List<Word> d = new ArrayList<>(due);
        List<Word> n = new ArrayList<>(unseen);
        List<Word> f = new ArrayList<>(familiar);
        // Due words arrive in adaptive priority order from ProgressStore.
        // Keep new material in curriculum order so first lessons form useful phrases.
        Collections.shuffle(f, random);
        int dueMax = deep ? 5 : 3;
        int newMax = ReviewFocus.freshWordLimit(deep, d.size());
        List<Word> selectionDue = new ArrayList<>(d.subList(0, Math.min(dueMax, d.size())));
        List<Word> selectionNew = new ArrayList<>(n.subList(0, Math.min(newMax, n.size())));
        if (selectionDue.isEmpty() && selectionNew.isEmpty()) {
            selectionDue.addAll(f.subList(0, Math.min(deep ? 6 : 4, f.size())));
        }
        Set<String> selected = new HashSet<>();
        for (Word w : selectionDue) selected.add(w.id);
        for (Word w : selectionNew) selected.add(w.id);
        // If the whole deck is already known, add a few well-spaced reinforcement words.
        if (d.isEmpty() && selectionNew.isEmpty() && selectionDue.size() < (deep ? 6 : 4)) {
            for (Word w : f) {
                if (selected.add(w.id)) selectionDue.add(w);
                if (selectionDue.size() >= (deep ? 6 : 4)) break;
            }
        }
        List<Exercise> result = new ArrayList<>();
        int slots = Math.max(selectionDue.size(), selectionNew.size());
        for (int i = 0; i < slots; i++) {
            if (i < selectionDue.size()) result.add(new Exercise(Exercise.Kind.RECALL, selectionDue.get(i)));
            if (i < selectionNew.size()) result.add(new Exercise(Exercise.Kind.INTRO, selectionNew.get(i)));
        }
        // Later transfer: listening, retrieval from English, and aloud shadowing.
        List<Word> transfer = new ArrayList<>();
        transfer.addAll(selectionDue);
        transfer.addAll(selectionNew);
        Collections.shuffle(transfer, random);
        for (int i = 0; i < transfer.size(); i++) {
            Word w = transfer.get(i);
            Exercise.Kind kind;
            switch (i % 4) {
                case 0: kind = Exercise.Kind.LISTEN; break;
                case 1: kind = Exercise.Kind.TYPE; break;
                case 2: kind = Exercise.Kind.LISTEN; break;
                default: kind = Exercise.Kind.RECALL;
            }
            result.add(new Exercise(kind, w));
        }
        // Never end a new word at exposure only: every new word is retrieved again.
        for (Word w : selectionNew) result.add(new Exercise(Exercise.Kind.RECALL, w));
        return result;
    }
}
