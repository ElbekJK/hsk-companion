package com.example.hskcompanion;

import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import java.util.Locale;

/** Plays A, waits for the synthesis engine to finish, then switches voice and plays B.
 * A new playback invalidates the old callbacks, including delayed TTS notifications.
 * No pitch shifting: altering pitch can distort the lexical tones being taught.
 */
public final class DialogueAudioPlayer {
    public interface Listener { void finished(boolean completed); }
    private final TextToSpeech tts;
    private final Handler main = new Handler(Looper.getMainLooper());
    private long generation = 0;
    private String secondLine;
    private Voice secondVoice;
    private Listener listener;

    public DialogueAudioPlayer(TextToSpeech engine) {
        tts = engine;
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) { }
            @Override public void onDone(String id) { main.post(() -> advance(id, true)); }
            @Override public void onError(String id) { main.post(() -> advance(id, false)); }
        });
    }

    public void cancel() {
        generation++;
        listener = null;
        secondLine = null;
        secondVoice = null;
        tts.stop();
    }

    public void play(String first, String second, Voice firstVoice, Voice voiceB, Listener whenFinished) {
        cancel();
        long token = generation;
        secondLine = second;
        secondVoice = voiceB;
        listener = whenFinished;
        if (!queue(first, firstVoice, "dialogue-" + token + "-a")) finish(false);
    }

    private boolean queue(String text, Voice voice, String id) {
        try {
            // A null voice means use the installed engine's default Mandarin voice.
            int result = voice == null ? tts.setLanguage(Locale.SIMPLIFIED_CHINESE) : tts.setVoice(voice);
            if (result < TextToSpeech.SUCCESS) {
                result = tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
            }
            if (result < TextToSpeech.SUCCESS) return false;
            tts.setSpeechRate(0.90f);
            return tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id) == TextToSpeech.SUCCESS;
        } catch (RuntimeException ex) { return false; }
    }

    private void advance(String id, boolean ok) {
        if (id == null || !id.startsWith("dialogue-" + generation + "-")) return;
        if (!ok) { finish(false); return; }
        if (id.endsWith("-a")) {
            if (!queue(secondLine, secondVoice, "dialogue-" + generation + "-b")) finish(false);
        } else if (id.endsWith("-b")) {
            finish(true);
        }
    }

    private void finish(boolean ok) {
        Listener current = listener;
        listener = null;
        secondLine = null;
        secondVoice = null;
        if (current != null) current.finished(ok);
    }
}
