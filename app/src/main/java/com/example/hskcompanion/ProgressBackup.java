package com.example.hskcompanion;

import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Portable, versioned backup for the existing hsk_progress_v1 SharedPreferences. */
public final class ProgressBackup {
    public static final int MAX_BYTES = 2 * 1024 * 1024;
    private static final String FORMAT = "hsk-companion-progress-v1";
    private ProgressBackup() {}

    public static String exportJson(SharedPreferences prefs) throws Exception {
        JSONObject root = new JSONObject();
        root.put("format", FORMAT);
        root.put("schema", 1);
        JSONArray entries = new JSONArray();
        Map<String,Object> snapshot = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) snapshot.put(entry.getKey(),entry.getValue());
        // A first-run installation may not have explicitly persisted the default deck.
        // Including defaults makes even an otherwise empty history safe to export/import.
        if (!snapshot.containsKey("level")) snapshot.put("level", 1);
        if (!snapshot.containsKey("version")) snapshot.put("version", "2.0");
        for (Map.Entry<String, Object> entry : snapshot.entrySet()) {
            Object value = entry.getValue();
            String kind;
            if (value instanceof String) kind = "string";
            else if (value instanceof Integer) kind = "int";
            else if (value instanceof Long) kind = "long";
            else if (value instanceof Boolean) kind = "boolean";
            else continue; // No unsupported types are serialized.
            JSONObject item = new JSONObject();
            item.put("key", entry.getKey());
            item.put("kind", kind);
            item.put("value", value);
            entries.put(item);
        }
        root.put("entries", entries);
        String output = root.toString(2);
        if (output.getBytes("UTF-8").length > MAX_BYTES)
            throw new IllegalArgumentException("Backup exceeds 2 MB");
        return output;
    }

    /** Parses and validates EVERYTHING before any existing preference is modified. */
    public static Map<String,Object> parseJson(String json) throws Exception {
        if (json == null || json.getBytes("UTF-8").length > MAX_BYTES)
            throw new IllegalArgumentException("Backup is too large");
        JSONObject root = new JSONObject(json);
        if (!FORMAT.equals(root.getString("format")) || root.getInt("schema") != 1)
            throw new IllegalArgumentException("Not a compatible HSK Companion backup");
        JSONArray entries = root.getJSONArray("entries");
        if (entries.length() > 12000) throw new IllegalArgumentException("Too many backup records");
        Map<String,Object> validated = new LinkedHashMap<>();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject item = entries.getJSONObject(i);
            String key = item.getString("key");
            String kind = item.getString("kind");
            if (key.length() < 1 || key.length() > 240 || key.indexOf('\n') != -1 || key.indexOf('\r') != -1)
                throw new IllegalArgumentException("Invalid record key");
            Object value;
            switch (kind) {
                case "string": value = item.getString("value"); break;
                case "int": value = item.getInt("value"); break;
                case "long": value = item.getLong("value"); break;
                case "boolean": value = item.getBoolean("value"); break;
                default: throw new IllegalArgumentException("Unknown record type");
            }
            if (value instanceof String && ((String)value).length() > 2000)
                throw new IllegalArgumentException("Oversized string record");
            if (validated.put(key, value) != null)
                throw new IllegalArgumentException("Duplicate record in backup");
        }
        // A legitimate backup has at least the selected deck. Avoid empty accidental restores.
        if (!validated.containsKey("level") || !(validated.get("level") instanceof Integer))
            throw new IllegalArgumentException("Missing HSK level in backup");
        int level = (Integer)validated.get("level");
        if (level != 1 && level != 2) throw new IllegalArgumentException("Invalid HSK level");
        return validated;
    }

    public static void restore(SharedPreferences prefs, Map<String,Object> validated) {
        if (validated == null || !validated.containsKey("level"))
            throw new IllegalArgumentException("No validated backup selected");
        SharedPreferences.Editor edit = prefs.edit().clear();
        for (Map.Entry<String,Object> record : validated.entrySet()) {
            Object v = record.getValue(); String k = record.getKey();
            if (v instanceof String) edit.putString(k, (String)v);
            else if (v instanceof Integer) edit.putInt(k, (Integer)v);
            else if (v instanceof Long) edit.putLong(k, (Long)v);
            else if (v instanceof Boolean) edit.putBoolean(k, (Boolean)v);
        }
        if (!edit.commit()) throw new IllegalStateException("Android could not save the restored progress");
    }
}
