package com.example.hskcompanion;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DialogueRepository {
    private final List<Dialogue> dialogues = new ArrayList<>();
    public DialogueRepository(Context context) {
        try (InputStream in=context.getAssets().open("dialogues.json"); ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buffer=new byte[4096];int count;
            while((count=in.read(buffer))!=-1)out.write(buffer,0,count);
            JSONArray data=new JSONArray(out.toString("UTF-8"));
            for(int i=0;i<data.length();i++){
                JSONObject o=data.getJSONObject(i);
                JSONArray items=o.getJSONArray("choices");
                List<String> answers=new ArrayList<>();
                for(int j=0;j<items.length();j++)answers.add(items.getString(j));
                dialogues.add(new Dialogue(o.getString("id"),o.getInt("level"),o.getString("title"),
                    o.getString("a"),o.getString("b"),o.getString("pa"),o.getString("pb"),
                    o.getString("ea"),o.getString("eb"),o.getString("question"),
                    answers,o.getInt("answer"),o.getString("feedback")));
            }
        } catch(Exception error){throw new IllegalStateException("Cannot load dialogue practice",error);}
    }
    public List<Dialogue> throughLevel(int level){
        List<Dialogue> result=new ArrayList<>();
        for(Dialogue d:dialogues)if(d.level<=level)result.add(d);
        return Collections.unmodifiableList(result);
    }
}
