import com.example.hskcompanion.DialogueAudioPlayer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import java.util.ArrayList;
import java.util.List;

public final class DialogueVoiceSelfTest {
    private static final class FakeEngine extends TextToSpeech {
        final List<String> spoken = new ArrayList<>();
        final List<String> ids = new ArrayList<>();
        UtteranceProgressListener callback;
        FakeEngine(){super(null,null);}
        @Override public int setOnUtteranceProgressListener(UtteranceProgressListener l){callback=l;return SUCCESS;}
        @Override public int setVoice(Voice voice){return SUCCESS;}
        @Override public int setLanguage(java.util.Locale locale){return LANG_AVAILABLE;}
        @Override public int speak(String text,int mode,Object parameters,String id){spoken.add(text);ids.add(id);return SUCCESS;}
        void complete(int index){callback.onDone(ids.get(index));}
        void fail(int index){callback.onError(ids.get(index));}
    }
    public static void main(String[] args){
        FakeEngine tts=new FakeEngine();
        DialogueAudioPlayer player=new DialogueAudioPlayer(tts);
        boolean[] finished={false};
        player.play("你好", "你叫什么名字？",null,null,ok->finished[0]=ok);
        assert tts.spoken.size()==1 : "B must not be queued before A completes";
        assert tts.spoken.get(0).equals("你好");
        tts.complete(0);
        assert tts.spoken.size()==2 && tts.spoken.get(1).equals("你叫什么名字？");
        assert !finished[0];
        tts.complete(1);
        assert finished[0] : "Unlock question only after speaker B finishes";
        int before=tts.spoken.size();
        player.play("第一句","第二句",null,null,ok->{throw new AssertionError("stale callback");});
        player.cancel();
        tts.complete(before);
        assert tts.spoken.size()==before+1 : "stale completion must not queue B";
        final int[] failures={0};
        player.play("甲","乙",null,null,ok->{if(!ok)failures[0]++;});
        tts.fail(tts.spoken.size()-1);
        assert failures[0]==1 : "A failure must leave question locked";
        System.out.println("DialogueVoiceSelfTest PASS: sequential playback, completion gating, cancellation, error");
    }
}
