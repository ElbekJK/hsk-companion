package com.example.hskcompanion;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class WordRepository {
    private final List<Word> words = new ArrayList<>();

    public WordRepository(Context context) {
        try (InputStream in = context.getAssets().open("vocabulary.json");
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            JSONArray array = new JSONArray(out.toString("UTF-8"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                words.add(new Word(obj.getString("version"), obj.getInt("level"),
                        obj.getString("hanzi"), obj.getString("pinyin"), obj.getString("english")));
            }
        } catch (Exception error) {
            throw new IllegalStateException("Unable to load bundled HSK vocabulary", error);
        }
    }

    public List<Word> forDeck(String version, int level) {
        List<Word> result = new ArrayList<>();
        for (Word w : words) if (w.version.equals(version) && w.level == level) result.add(w);
        return result;
    }

    public List<Word> throughLevel(String version, int level) {
        List<Word> result = new ArrayList<>();
        for (Word w : words) if (w.version.equals(version) && w.level <= level) result.add(w);
        return result;
    }

    public List<Word> search(String version, int level, String term) {
        String text = term.trim().toLowerCase(Locale.ROOT);
        List<Word> result = new ArrayList<>();
        for (Word w : words) {
            if (!w.version.equals(version) || w.level > level) continue;
            if (text.isEmpty() || w.hanzi.contains(text) || w.pinyin.toLowerCase(Locale.ROOT).contains(text)
                    || w.english.toLowerCase(Locale.ROOT).contains(text)) result.add(w);
        }
        return result;
    }

    public int count(String version, int level) { return forDeck(version, level).size(); }
    public List<Word> all() { return Collections.unmodifiableList(words); }
}
