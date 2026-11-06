package com.example.hskcompanion;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public final class ExampleRepository {
    private final Map<String, List<SentenceExample>> examples = new HashMap<>();
    public ExampleRepository(Context context) {
        try (InputStream in = context.getAssets().open("examples.json");
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096]; int count;
            while ((count=in.read(buffer)) >= 0) out.write(buffer,0,count);
            JSONArray array = new JSONArray(out.toString("UTF-8"));
            for(int i=0;i<array.length();i++) {
                JSONObject o = array.getJSONObject(i);
                String target = o.getString("target");
                SentenceExample ex = new SentenceExample(target, o.getString("chinese"),
                        o.getString("pinyin"), o.getString("english"));
                if (!ex.chinese.contains(target)) throw new IllegalArgumentException("Example missing target: " + target);
                if (!examples.containsKey(target)) examples.put(target, new ArrayList<>());
                examples.get(target).add(ex);
            }
        } catch (Exception error) { throw new IllegalStateException("Unable to load example sentences",error); }
    }
    public SentenceExample find(Word w) {
        List<SentenceExample> variants = examples.get(w.hanzi);
        if (variants == null || variants.isEmpty()) return null;
        // Stable within a local study day; rotates with subsequent days.
        java.util.Calendar c = java.util.Calendar.getInstance();
        long day = c.get(java.util.Calendar.YEAR) * 400L + c.get(java.util.Calendar.DAY_OF_YEAR);
        return variants.get((int)Math.floorMod(day + w.hanzi.hashCode(), variants.size()));
    }
    public int size() { int count=0; for (List<SentenceExample> a:examples.values())count+=a.size(); return count; }
}
