package org.json; public class JSONObject {
public JSONObject(){} public JSONObject(String json){} public JSONObject put(String s,Object v){return this;}
public String toString(int spaces){return "";}
public String getString(String s){return "";}public int getInt(String s){return 0;}
public long getLong(String s){return 0;}public boolean getBoolean(String s){return false;}
public JSONArray getJSONArray(String s){return null;}}
