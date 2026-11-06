package android.speech.tts;
public class TextToSpeech {
    public static final int SUCCESS=0, ERROR=-1, LANG_AVAILABLE=1, LANG_MISSING_DATA=-1, LANG_NOT_SUPPORTED=-2, QUEUE_FLUSH=0;
    public interface OnInitListener{void onInit(int status);}
    public TextToSpeech(Object c,OnInitListener l){}
    public String getDefaultEngine(){return "";}
    public int setLanguage(java.util.Locale l){return 1;}
    public int setSpeechRate(float r){return 0;}
    public int speak(String s,int q,Object o,String id){return 0;}
    public void stop(){}
    public void shutdown(){}
    public java.util.Set<Voice> getVoices(){return java.util.Collections.emptySet();}
    public Voice getVoice(){return null;}
    public int setVoice(Voice v){return 0;}
    public int setOnUtteranceProgressListener(UtteranceProgressListener l){return 0;}
}
