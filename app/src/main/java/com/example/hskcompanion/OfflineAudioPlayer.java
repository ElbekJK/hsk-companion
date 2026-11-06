package com.example.hskcompanion;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Plays only locally bundled, pre-generated Azure recordings. No cloud API, account or key. */
public final class OfflineAudioPlayer {
    public interface Listener { void finished(boolean completed); }
    private final Context context;
    private MediaPlayer active;
    private long generation=0;

    public OfflineAudioPlayer(Context c) { context=c; }

    public static String filename(String role,String words) {
        if (!"a".equals(role) && !"b".equals(role)) throw new IllegalArgumentException("Unknown role");
        try {
            byte[] bytes=MessageDigest.getInstance("SHA-256")
                .digest((role+"\n"+words.trim()).getBytes(StandardCharsets.UTF_8));
            StringBuilder s=new StringBuilder();
            for(int i=0;i<12;i++) s.append(String.format(java.util.Locale.ROOT,"%02x",bytes[i]&0xff));
            return "audio/"+role+"/"+s+".mp3";
        } catch(java.security.NoSuchAlgorithmException e){throw new AssertionError(e);}
    }

    public boolean has(String role,String text) {
        try(AssetFileDescriptor fd=context.getAssets().openFd(filename(role,text))) {
            return fd.getLength()>0;
        } catch(Exception ignored){return false;}
    }
    public boolean hasDialogue(String a,String b){return has("a",a)&&has("b",b);}
    public boolean hasPreviews(){return has("a","你好，很高兴认识你。")&&has("b","你好，很高兴认识你。");}
    public void stop(){
        generation++;
        if(active!=null){
            try{active.setOnCompletionListener(null);active.setOnErrorListener(null);active.stop();}
            catch(Exception ignored){};
            active.release();active=null;
        }
    }
    public boolean play(String role,String text){
        if(!has(role,text))return false;
        stop();
        return start(role,text,generation,null);
    }
    public boolean playDialogue(String first,String second,Listener listener){
        if(!hasDialogue(first,second))return false;
        stop();
        final long seq=generation;
        return start("a",first,seq,ok->{
            if(seq!=generation)return;
            if(!ok){listener.finished(false);return;}
            if(!start("b",second,seq,listener))listener.finished(false);
        });
    }
    private boolean start(String role,String text,long seq,Listener listener){
        MediaPlayer mp=new MediaPlayer();
        try(AssetFileDescriptor afd=context.getAssets().openFd(filename(role,text))){
            mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            active=mp;
            mp.setOnPreparedListener(p->{if(seq==generation) p.start();});
            mp.setOnCompletionListener(p->{
                if(seq!=generation)return;
                p.release();if(active==p)active=null;
                if(listener!=null)listener.finished(true);
            });
            mp.setOnErrorListener((p,what,extra)->{
                if(seq==generation){p.release();if(active==p)active=null;
                    if(listener!=null)listener.finished(false);}
                return true;
            });
            mp.prepareAsync();
            return true;
        } catch(Exception error){
            if(active==mp)active=null;
            mp.release();
            return false;
        }
    }
}
