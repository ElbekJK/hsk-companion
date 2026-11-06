package com.example.hskcompanion;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class GrammarRepository {
    private final List<GrammarRule> rules = new ArrayList<>();
    public GrammarRepository(Context context) {
        try (InputStream input = context.getAssets().open("grammar.json"); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096]; int n;
            while ((n = input.read(buffer)) != -1) bytes.write(buffer, 0, n);
            JSONArray arr = new JSONArray(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
            for (int i=0;i<arr.length();i++) {
                JSONObject o=arr.getJSONObject(i); JSONArray a=o.getJSONArray("choices");
                List<String> options=new ArrayList<>();
                for(int j=0;j<a.length();j++) options.add(a.getString(j));
                rules.add(new GrammarRule(o.getString("id"),o.getInt("level"),o.getString("title"),o.getString("pattern"),o.getString("explain"),o.getString("hanzi"),o.getString("pinyin"),o.getString("translation"),o.getString("question"),options,o.getInt("answer"),o.getString("feedback")));
            }
        } catch(Exception ex) { throw new IllegalStateException("Grammar lesson data invalid",ex); }
    }
    public List<GrammarRule> throughLevel(int level) {
        List<GrammarRule> found=new ArrayList<>();
        for(GrammarRule rule:rules) if(rule.level<=level) found.add(rule);
        return found;
    }
}
