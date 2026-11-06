package com.example.hskcompanion;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public final class CurriculumRepository {
    private final List<CurriculumUnit> units=new ArrayList<>();
    public CurriculumRepository(Context context){
        try(InputStream in=context.getAssets().open("curriculum.json");ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[4096];int n;
            while((n=in.read(buffer))!=-1)out.write(buffer,0,n);
            JSONArray array=new JSONArray(out.toString("UTF-8"));
            for(int i=0;i<array.length();i++){
                JSONObject o=array.getJSONObject(i);
                units.add(new CurriculumUnit(o.getString("id"),o.getInt("level"),o.getString("title"),o.getString("goal"),
                    values(o.getJSONArray("grammar")),values(o.getJSONArray("dialogues"))));
            }
        }catch(Exception error){throw new IllegalStateException("Cannot load learning paths",error);}
    }
    private static List<String> values(JSONArray array)throws Exception{
        List<String> data=new ArrayList<>();
        for(int i=0;i<array.length();i++)data.add(array.getString(i));
        return data;
    }
    public List<CurriculumUnit> throughLevel(int level){
        List<CurriculumUnit> result=new ArrayList<>();
        for(CurriculumUnit u:units)if(u.level<=level)result.add(u);
        return Collections.unmodifiableList(result);
    }
}
