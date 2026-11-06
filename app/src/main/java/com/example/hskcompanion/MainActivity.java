package com.example.hskcompanion;

import android.app.Activity;
import android.Manifest;
import android.os.Build;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.app.AlertDialog;
import android.view.WindowInsets;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    // Palette is deliberately semantic and switched as a whole. WHITE is the
    // card surface, not an invariant literal white, so dark screens remain legible.
    private int BG, INK, MUTED, PRIMARY, PALE, SUCCESS_BG, WHITE, BORDER, RED, GREEN;
    private int ERROR_BG, WARNING_BG, INFO_BG, HERO_NOTE, HERO_BG, HERO_INK;
    private int ACCENT, ACCENT_INK;
    private boolean darkMode = false;

    private boolean shouldUseDarkMode() {
        String pref = getSharedPreferences("hsk_progress_v1", MODE_PRIVATE)
            .getString("ui_theme_mode", "system");
        if ("dark".equals(pref)) return true;
        if ("light".equals(pref)) return false;
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            == Configuration.UI_MODE_NIGHT_YES;
    }

    private void setPalette(boolean dark) {
        darkMode = dark;
        BG      = Color.parseColor(dark ? "#0F172A" : "#FAF9F6");
        INK     = Color.parseColor(dark ? "#F8FAFC" : "#1F2937");
        MUTED   = Color.parseColor(dark ? "#BAC6D8" : "#536174");
        PRIMARY = Color.parseColor(dark ? "#7FB3FF" : "#2F5DA8");
        PALE    = Color.parseColor(dark ? "#1B2A42" : "#EAF1FC");
        SUCCESS_BG = Color.parseColor(dark ? "#163C38" : "#E5F5F2");
        WHITE   = Color.parseColor(dark ? "#1E293B" : "#FFFFFF");
        BORDER  = Color.parseColor(dark ? "#3A4960" : "#D9E2EE");
        RED     = Color.parseColor(dark ? "#FFB3BB" : "#A12E40");
        GREEN   = Color.parseColor(dark ? "#9BE1C9" : "#146B5F");
        ERROR_BG = Color.parseColor(dark ? "#482C3A" : "#FFF0F2");
        WARNING_BG = Color.parseColor(dark ? "#463427" : "#FFF4E8");
        INFO_BG = Color.parseColor(dark ? "#23334B" : "#E9F3FD");
        HERO_NOTE = Color.parseColor(dark ? "#E3EEFF" : "#E3EEFF");
        HERO_BG = Color.parseColor(dark ? "#254A83" : "#2F5DA8");
        HERO_INK = Color.parseColor(dark ? "#FFFFFF" : "#FFFFFF");
        ACCENT = Color.parseColor(dark ? "#FFB86C" : "#F4A261");
        ACCENT_INK = Color.parseColor(dark ? "#172234" : "#26252C");
    }

    private void applySystemBars() {
        if (Build.VERSION.SDK_INT < 35) {
            getWindow().setStatusBarColor(BG);
            getWindow().setNavigationBarColor(BG);
        }
        getWindow().getDecorView().setSystemUiVisibility(darkMode ? 0 :
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }

    private void changeTheme(String next) {
        getSharedPreferences("hsk_progress_v1", MODE_PRIVATE)
            .edit().putString("ui_theme_mode", next).apply();
        // Recreation re-selects a native dark/light dialog, text input and keyboard theme.
        // Existing lesson state is preserved by onSaveInstanceState().
        recreate();
    }

    private final Random random = new Random();
    private WordRepository repository;
    private ExampleRepository examples;
    private GrammarRepository grammarRepository;
    private DialogueRepository dialogueRepository;
    private CurriculumRepository curriculumRepository;
    private String pathUnitId = "";
    private ProgressStore progress;
    private TextToSpeech tts;
    private DialogueAudioPlayer dialoguePlayer;
    private OfflineAudioPlayer offlineAudio;
    private final List<Voice> mandarinVoices = new ArrayList<>();
    private Voice defaultMandarinVoice;
    private boolean dialoguePlaying = false;
    private boolean ttsReady = false;
    private String ttsStatus = "Initializing Android speech engine…";
    private String ttsEngine = "unknown";
    private LinearLayout root;
    private LinearLayout content;
    private String page = "home";
    private static final int BACKUP_EXPORT_REQUEST = 7060;
    private static final int BACKUP_IMPORT_REQUEST = 7061;
    private Map<String,Object> pendingRestore = null;
    private String version;
    private int level;
    private Word currentCard;
    private boolean cardFlipped = false;
    private boolean onlySaved = false;
    private int wordLimit = 40;
    private String wordSearch = "";
    private String quizMode = "reading";
    private final List<Word[]> tonePairs = new ArrayList<>();
    private int toneIndex=0, toneScore=0;
    private boolean toneHeard=false, toneAnswered=false;
    private int toneChoice=-1;

    // An unofficial exam-length vocabulary assessment, not an official HSK mock paper.
    private final Handler examTimer = new Handler(Looper.getMainLooper());
    private final List<Word> examQuestions = new ArrayList<>();
    private final List<Boolean> examAnswers = new ArrayList<>();
    private int examIndex = 0;
    private int examLevel = 1;
    private long examDeadlineMs = 0;
    private boolean examFinished = false;
    private boolean examAudioPlayed = false;
    private boolean examShowMistakes = false;
    private Runnable examTimeUp;
    private String dialogueId = "";
    private boolean dialogueReading = false;
    private boolean dialoguePlayed = false;
    private int dialogueAnswer = -1;
    private String grammarSelected = "";
    private int grammarChosen = -1;
    private boolean grammarAnswered = false;
    private boolean grammarTesting = false;
    private List<Word> quizQuestions = new ArrayList<>();
    private int quizIndex = 0;
    private int quizScore = 0;
    private boolean answered = false;
    private String chosenId = "";
    private final List<Exercise> lessonSteps = new ArrayList<>();
    private final Set<String> retryPlanned = new HashSet<>();
    private int lessonPosition = 0;
    private int lessonCorrect = 0;
    private int lessonAttempts = 0;
    private boolean lessonRevealed = false;
    private boolean lessonAnswered = false;
    private boolean lessonAudioPlayed = false;
    private boolean lessonWasCorrect = false;
    private String lessonAnswerText = "";
    private String lessonChoice = "";
    private static final List<String> FOUNDATION = Arrays.asList(
        "你", "好", "我", "是", "他", "她", "吗", "什么", "名字", "叫",
        "谢谢", "不", "有", "在", "去", "来", "喝", "水", "茶", "吃",
        "米饭", "喜欢", "学习", "汉语", "会", "说", "听", "看", "书", "写",
        "读", "朋友", "老师", "学生", "学校", "今天", "明天", "现在", "天气",
        "大", "小", "买", "钱", "多少", "几", "工作", "认识", "高兴",
        "再见", "对不起", "不客气", "中国", "家", "人", "饭店", "电影"
    );

    @Override public void onCreate(Bundle savedInstanceState) {
        boolean useDark = shouldUseDarkMode();
        // Apply the native dialog/input theme before Activity views are inflated.
        if (useDark) setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setPalette(useDark);
        applySystemBars();
        repository = new WordRepository(this);
        examples = new ExampleRepository(this);
        grammarRepository = new GrammarRepository(this);
        dialogueRepository = new DialogueRepository(this);
        curriculumRepository = new CurriculumRepository(this);
        progress = new ProgressStore(this);
        offlineAudio = new OfflineAudioPlayer(this);
        ReviewReminders.schedule(this);
        version = "2.0";
        if (!"2.0".equals(progress.version())) progress.setVersion("2.0");
        level = progress.level();
        if (repository.count(version, level) == 0) {
            version = "2.0";
            level = 1;
        }
        if(savedInstanceState!=null) restoreSession(savedInstanceState);
        tts = new TextToSpeech(this, this);
        render();
    }

    @Override public void onInit(int status) {
        ttsReady = false;
        if (status == TextToSpeech.SUCCESS && tts != null) {
            try {
                String engine = tts.getDefaultEngine();
                ttsEngine = engine == null ? "unknown" : engine;
                int available = tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
                ttsReady = available >= TextToSpeech.LANG_AVAILABLE;
                if (ttsReady) {
                    tts.setSpeechRate(0.85f);
                    defaultMandarinVoice=tts.getVoice();
                    findMandarinVoices();
                    dialoguePlayer = new DialogueAudioPlayer(tts);
                    ttsStatus = "Mandarin (zh-CN) ready · " + mandarinVoices.size() + " available voice(s).";
                } else if (available == TextToSpeech.LANG_MISSING_DATA) {
                    ttsStatus = "The selected TTS engine needs Mandarin voice data.";
                } else if (available == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsStatus = "The selected TTS engine does not support Mandarin (zh-CN).";
                } else {
                    ttsStatus = "Mandarin setup returned error code " + available + ".";
                }
            } catch (RuntimeException ex) {
                ttsStatus = "TTS error: " + ex.getClass().getSimpleName();
            }
        } else {
            ttsStatus = "Android could not initialize its speech engine (code " + status + ").";
        }
        // Initialization is asynchronous. Refresh a visible Settings page once it finishes.
        runOnUiThread(() -> {
            if (!isFinishing() && (page.equals("settings") || page.equals("dialogue_detail"))) render();
        });
    }

    private void findMandarinVoices(){
        mandarinVoices.clear();
        try {
            Set<Voice> available=tts.getVoices();
            if(available!=null) for(Voice v:available){
                Locale loc=v.getLocale();
                if(loc!=null && "zh".equals(loc.getLanguage()) &&
                    ("CN".equalsIgnoreCase(loc.getCountry()) ||
                     (loc.getCountry().isEmpty() && v.getName().toLowerCase(Locale.ROOT).contains("zh-cn")))){
                    mandarinVoices.add(v);
                }
            }
        } catch(RuntimeException ignored){ }
        if(defaultMandarinVoice!=null && mandarinVoices.isEmpty()){
            mandarinVoices.add(defaultMandarinVoice);
        }
        Collections.sort(mandarinVoices,(a,b)->{
            if(a.isNetworkConnectionRequired()!=b.isNetworkConnectionRequired())
                return a.isNetworkConnectionRequired()?1:-1;
            return a.getName().compareToIgnoreCase(b.getName());
        });
    }

    private Voice speakerVoice(String speaker){
        if(mandarinVoices.isEmpty()) return defaultMandarinVoice;
        String saved=progress.dialogueVoiceName(speaker);
        for(Voice voice:mandarinVoices) if(voice.getName().equals(saved)) return voice;
        return mandarinVoices.get("b".equals(speaker) && mandarinVoices.size()>1 ? 1 : 0);
    }

    private String voiceLabel(String speaker){
        Voice v=speakerVoice(speaker);
        if(v==null) return "default Mandarin voice";
        String name=v.getName();
        if(name.length()>45)name=name.substring(0,45)+"…";
        return name+(v.isNetworkConnectionRequired()?" · network":" · offline-capable");
    }

    private void cycleSpeakerVoice(String speaker){
        if(mandarinVoices.size()<2 && !hasBundledAudio()){showToast("Only one Mandarin voice is currently available.");return;}
        Voice old=speakerVoice(speaker);
        int index=mandarinVoices.indexOf(old);
        progress.setDialogueVoiceName(speaker,mandarinVoices.get((index+1)%mandarinVoices.size()).getName());
        render();
    }

    private void speakSpeakerPreview(String speaker){
        if(offlineAudio!=null && offlineAudio.has(speaker,"你好，很高兴认识你。")){
            if(dialoguePlayer!=null)dialoguePlayer.cancel();
            if(tts!=null)tts.stop();
            if(offlineAudio.play(speaker,"你好，很高兴认识你。"))return;
        }
        if(!ttsReady || tts==null){showToast("No bundled preview or Mandarin TTS available.");return;}
        if(dialoguePlayer!=null)dialoguePlayer.cancel();
        Voice voice=speakerVoice(speaker);
        if(voice==null || tts.setVoice(voice)<0)tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
        tts.setSpeechRate(0.90f);
        tts.speak("你好，很高兴认识你。",TextToSpeech.QUEUE_FLUSH,null,"preview-"+speaker);
    }

    private boolean hasBundledAudio(){
        return offlineAudio != null && offlineAudio.hasPreviews();
    }
    private boolean canPlayDialogue(Dialogue d){
        return (offlineAudio!=null && offlineAudio.hasDialogue(d.a,d.b)) ||
            (ttsReady && dialoguePlayer!=null);
    }

    private void playTwoSpeakers(Dialogue d){
        if(!canPlayDialogue(d)){showToast("Audio unavailable. Try reading practice or install an offline audio pack.");return;}
        dialoguePlayed=false;
        dialoguePlaying=true;
        render();
        DialogueAudioPlayer.Listener done=success->{
            if(!page.equals("dialogue_detail") || !d.id.equals(dialogueId))return;
            dialoguePlaying=false;
            dialoguePlayed=success;
            if(!success)showToast("Audio interrupted. Replay the dialogue or use reading mode.");
            render();
        };
        if(offlineAudio!=null && offlineAudio.hasDialogue(d.a,d.b)){
            if(dialoguePlayer!=null)dialoguePlayer.cancel();
            if(tts!=null)tts.stop();
            if(offlineAudio.playDialogue(d.a,d.b,done::finished)) return;
        }
        if(ttsReady && dialoguePlayer!=null){
            dialoguePlayer.play(d.a,d.b,speakerVoice("a"),speakerVoice("b"),done);
        }else{dialoguePlaying=false;showToast("Offline audio unavailable; check the audio pack.");render();}
    }

    @Override protected void onSaveInstanceState(Bundle out){
        super.onSaveInstanceState(out);
        out.putString("page",page);
        out.putString("dialogue_id",dialogueId);
        out.putString("path_unit_id",pathUnitId);
        out.putBoolean("dialogue_reading",dialogueReading);
        out.putBoolean("dialogue_played",dialoguePlayed);
        out.putInt("dialogue_answer",dialogueAnswer);
        out.putString("grammar_id",grammarSelected);
        out.putInt("grammar_choice",grammarChosen);
        out.putBoolean("grammar_answered",grammarAnswered);
        out.putBoolean("grammar_testing",grammarTesting);
        if(!examQuestions.isEmpty()){
            ArrayList<String> ids=new ArrayList<>();
            for(Word w:examQuestions) ids.add(w.id);
            boolean[] values=new boolean[examAnswers.size()];
            for(int i=0;i<values.length;i++)values[i]=examAnswers.get(i);
            out.putStringArrayList("exam_ids",ids);
            out.putBooleanArray("exam_answers",values);
            out.putInt("exam_index",examIndex);
            out.putInt("exam_level",examLevel);
            out.putLong("exam_deadline",examDeadlineMs);
            out.putBoolean("exam_finished",examFinished);
            out.putBoolean("exam_audio_played",examAudioPlayed);
            out.putBoolean("exam_show_mistakes",examShowMistakes);
        }
    }
    private void restoreSession(Bundle saved){
        page=saved.getString("page","home");
        dialogueId=saved.getString("dialogue_id","");
        pathUnitId=saved.getString("path_unit_id","");
        dialogueReading=saved.getBoolean("dialogue_reading",false);
        dialoguePlayed=saved.getBoolean("dialogue_played",false);
        dialoguePlaying=false;
        dialogueAnswer=saved.getInt("dialogue_answer",-1);
        grammarSelected=saved.getString("grammar_id","");
        grammarChosen=saved.getInt("grammar_choice",-1);
        grammarAnswered=saved.getBoolean("grammar_answered",false);
        grammarTesting=saved.getBoolean("grammar_testing",false);
        ArrayList<String> ids=saved.getStringArrayList("exam_ids");
        if(ids==null || ids.isEmpty()){
            // A guided mission is intentionally session-scoped; show Home on recreation.
            if(page.equals("lesson") || page.equals("done"))page="home";
            return;
        }
        List<Word> bank=deck();
        for(String id:ids){
            Word found=null;
            for(Word w:bank)if(w.id.equals(id)){found=w;break;}
            if(found==null){examQuestions.clear();page="home";return;}
            examQuestions.add(found);
        }
        examLevel=saved.getInt("exam_level",level);
        examIndex=saved.getInt("exam_index",0);
        boolean[] results=saved.getBooleanArray("exam_answers");
        if(results!=null)for(boolean b:results)examAnswers.add(b);
        examDeadlineMs=saved.getLong("exam_deadline",0);
        examFinished=saved.getBoolean("exam_finished",false);
        examAudioPlayed=saved.getBoolean("exam_audio_played",false);
        examShowMistakes=saved.getBoolean("exam_show_mistakes",false);
        if(examIndex>=examQuestions.size())examFinished=true;
        if(!examFinished){
            long remaining=examDeadlineMs-SystemClock.elapsedRealtime();
            examTimeUp=()->{if(!examFinished && !examQuestions.isEmpty()){
                finishMock();if(page.equals("mock"))render();
            }};
            examTimer.postDelayed(examTimeUp,Math.max(0,remaining));
        }
    }

    @Override protected void onResume(){
        super.onResume();
        if(!examQuestions.isEmpty() && !examFinished && examDeadlineMs>0 &&
            SystemClock.elapsedRealtime()>=examDeadlineMs){
            finishMock();if(page.equals("mock"))render();
        }
    }

    @Override public void onBackPressed() {
        if (offlineAudio != null) offlineAudio.stop();
        if (dialoguePlayer != null) dialoguePlayer.cancel();
        dialoguePlaying = false;
        switch (page) {
            case "home": super.onBackPressed(); return;
            case "path_unit": page = "path"; break;
            case "grammar_detail": page = "grammar"; break;
            case "dialogue_detail": page = "dialogues"; break;
            case "words": case "grammar": case "dialogues": case "path":
                page = "learn_hub"; break;
            case "cards": case "quiz": case "tones": case "mock":
                page = "practice_hub"; break;
            case "lesson": case "done": case "learn_hub": case "practice_hub":
            case "stats": case "settings":
            default: page = "home"; break;
        }
        render();
    }

    @Override public void onDestroy() {
        examTimer.removeCallbacksAndMessages(null);
        if (offlineAudio != null) offlineAudio.stop();
        if (dialoguePlayer != null) dialoguePlayer.cancel();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    private int dp(float v) { return (int) (getResources().getDisplayMetrics().density * v + 0.5f); }
    private GradientDrawable bg(int color, float radius, int line) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        if (line != 0) drawable.setStroke(dp(1), line);
        return drawable;
    }
    private LinearLayout vertical(int pad) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(pad), dp(pad), dp(pad), dp(pad));
        return layout;
    }
    private LinearLayout horizontal() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }
    private TextView text(String string, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(string);
        view.setTextColor(color);
        view.setTextSize(sp);
        view.setIncludeFontPadding(true);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }
    private LinearLayout card(int fill) {
        LinearLayout layout = vertical(16);
        layout.setBackground(bg(fill, 16, fill == WHITE ? BORDER : 0));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(10));
        layout.setLayoutParams(params);
        return layout;
    }
    private void gap(LinearLayout parent, int height) {
        View spacer = new View(this);
        parent.addView(spacer, new LinearLayout.LayoutParams(1, dp(height)));
    }
    private void add(LinearLayout parent, View child) { parent.addView(child); }
    private void section(LinearLayout parent, String title, String subtitle) {
        add(parent, text(title, 23, INK, true));
        if (!subtitle.isEmpty()) { gap(parent, 4); add(parent, text(subtitle, 13, MUTED, false)); }
        gap(parent, 13);
    }
    private Button button(String title, boolean active, boolean outline) {
        Button b = new Button(this);
        b.setText(title);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setMinHeight(dp(48));
        b.setMinimumHeight(dp(48));
        b.setPadding(dp(10), dp(11), dp(10), dp(11));
        b.setTextColor(active ? WHITE : PRIMARY);
        b.setBackground(bg(active ? PRIMARY : WHITE, 12, active ? 0 : outline ? BORDER : 0));
        b.setElevation(0);
        return b;
    }
    // Orange is reserved for the primary learning action. Secondary controls remain blue.
    private Button accentButton(String title) {
        Button b = button(title, false, false);
        b.setTextColor(ACCENT_INK);
        b.setBackground(bg(ACCENT, 12, 0));
        return b;
    }
    private void weighted(LinearLayout row, View child, float weight, int rightMargin) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, weight);
        p.setMargins(0, 0, dp(rightMargin), 0);
        row.addView(child, p);
    }
    private void showToast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
    private List<Word> deck() { return repository.throughLevel(version, level); }
    private boolean canHear(Word w) {
        return ttsReady || (offlineAudio!=null && offlineAudio.has("a",w.hanzi));
    }
    private boolean canHearEntireDeck(){
        if(ttsReady)return true;
        if(offlineAudio==null)return false;
        for(Word w:deck()) if(!offlineAudio.has("a",w.hanzi))return false;
        return true;
    }
    private void speak(Word w) { speakChinese(w.hanzi, w.id); }
    private void speakChinese(String chinese, String utteranceId) {
        if(offlineAudio!=null && offlineAudio.has("a",chinese)){
            if(dialoguePlayer!=null)dialoguePlayer.cancel();
            if(tts!=null)tts.stop();
            if(offlineAudio.play("a",chinese))return;
        }
        if (!ttsReady || tts == null) {
            showToast("No bundled recording for this item. See Settings → Mandarin voice.");
            return;
        }
        if(dialoguePlayer!=null)dialoguePlayer.cancel();
        if(defaultMandarinVoice!=null && tts.setVoice(defaultMandarinVoice)<0)
            tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
        tts.setSpeechRate(0.85f);
        tts.speak(chinese, TextToSpeech.QUEUE_FLUSH, null, "hsk-" + utteranceId);
    }
    private void changeDeck(String nextVersion, int nextLevel) {
        if (repository.count(nextVersion, nextLevel) == 0) {
            showToast("This deck is not bundled. Run ./scripts/fetch_vocab.sh on Debian, then rebuild.");
            return;
        }
        version = nextVersion;
        level = nextLevel;
        progress.setVersion(version);
        progress.setLevel(level);
        currentCard = null;
        cardFlipped = false;
        wordLimit = 40;
        wordSearch = "";
        quizQuestions.clear();
        examQuestions.clear(); examAnswers.clear(); examTimer.removeCallbacksAndMessages(null);
        lessonSteps.clear();
        page = "home";
        render();
    }
    private void open(String next) {
        if(page.equals("dialogue_detail") && !next.equals("dialogue_detail")){
            if(dialoguePlayer!=null)dialoguePlayer.cancel();
            if(offlineAudio!=null)offlineAudio.stop();
            dialoguePlaying=false;
        }
        page = next;
        if (next.equals("cards")) currentCard = null;
        render();
    }

    /** Stable top-level sections; each tool has one home rather than being repeated on Today. */
    private String sectionForPage(String current) {
        switch (current) {
            case "learn_hub": case "path": case "path_unit": case "words":
            case "grammar": case "grammar_detail": case "dialogues": case "dialogue_detail":
                return "learn_hub";
            case "practice_hub": case "cards": case "quiz": case "tones": case "mock":
                return "practice_hub";
            case "stats": return "stats";
            case "settings": return "settings";
            default: return "home";
        }
    }

    private void render() {
        root = vertical(0);
        root.setBackgroundColor(BG);
        if (Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                root.setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
                return insets;
            });
        }
        setContentView(root);
        LinearLayout header = vertical(0);
        header.setPadding(dp(20), dp(11), dp(20), dp(12));
        LinearLayout top = horizontal();
        TextView brand = text("汉  HSK Companion", 17, PRIMARY, true);
        brand.setContentDescription("HSK Companion, Chinese learning");
        weighted(top, brand, 1, 0);
        Button selectedLevel = button("HSK " + level + "  ▾", false, true);
        selectedLevel.setTextSize(12);
        selectedLevel.setOnClickListener(v -> open("settings"));
        top.addView(selectedLevel);
        header.addView(top);
        root.addView(header);

        ScrollView scroller = new ScrollView(this);
        scroller.setFillViewport(true);
        scroller.setClipToPadding(false);
        scroller.setVerticalScrollBarEnabled(false);
        content = vertical(20);
        content.setPadding(dp(20), dp(8), dp(20), dp(22));
        scroller.addView(content);
        root.addView(scroller, new LinearLayout.LayoutParams(-1, 0, 1f));
        if (page.equals("home")) home();
        else if (page.equals("learn_hub")) learnHub();
        else if (page.equals("practice_hub")) practiceHub();
        else if (page.equals("lesson")) lesson();
        else if (page.equals("done")) lessonFinished();
        else if (page.equals("words")) words();
        else if (page.equals("cards")) cards();
        else if (page.equals("quiz")) quiz();
        else if (page.equals("grammar")) grammar();
        else if (page.equals("grammar_detail")) grammarDetail();
        else if (page.equals("dialogues")) dialogues();
        else if (page.equals("path")) learningPath();
        else if (page.equals("path_unit")) learningUnit();
        else if (page.equals("tones")) toneLab();
        else if (page.equals("dialogue_detail")) dialogueDetail();
        else if (page.equals("mock")) mockExam();
        else if (page.equals("stats")) stats();
        else settings();
        footer();
    }

    private void footer() {
        LinearLayout tabs = horizontal();
        tabs.setPadding(dp(6), dp(8), dp(6), dp(8));
        tabs.setBackground(bg(WHITE, 0, BORDER));
        String[] names = {"Today", "Learn", "Practice", "Progress", "Settings"};
        String[] keys = {"home", "learn_hub", "practice_hub", "stats", "settings"};
        String[] glyphs = {"◉", "学", "练", "▥", "⚙"};
        String active = sectionForPage(page);
        for (int i = 0; i < names.length; i++) {
            final String key = keys[i];
            LinearLayout tile = vertical(2);
            tile.setGravity(Gravity.CENTER);
            tile.setPadding(dp(2), dp(4), dp(2), dp(4));
            int color = active.equals(key) ? PRIMARY : MUTED;
            TextView symbol = text(glyphs[i], 19, color, active.equals(key));
            symbol.setGravity(Gravity.CENTER);
            tile.setBackground(bg(active.equals(key) ? PALE : WHITE, 12, 0));
            TextView label = text(names[i], 12, color, active.equals(key));
            label.setGravity(Gravity.CENTER);
            tile.addView(symbol);
            tile.addView(label);
            // 48 dp minimum tap height: accessible even on narrow phones.
            tile.setMinimumHeight(dp(48));
            tile.setContentDescription(names[i] + " tab");
            tile.setOnClickListener(v -> { if(!page.equals(key)) open(key); });
            weighted(tabs, tile, 1, 0);
        }
        root.addView(tabs);
    }

    private void home() {
        List<Word> words = deck();
        int dueNow = progress.dueCount(words);
        int learned = progress.learned(words);
        int strong = progress.strong(words);
        section(content,"Today","HSK " + level + " · One focused session at a time");
        LinearLayout hero = card(HERO_BG);
        add(hero,text("YOUR NEXT STEP",12,HERO_NOTE,true));
        gap(hero,8);
        add(hero,text(!lessonSteps.isEmpty() && lessonPosition < lessonSteps.size() ?
                "Pick up where you left off" : dueNow > 0 ?
                "Ready to remember?" : "Build your Mandarin",25,HERO_INK,true));
        gap(hero,8);
        add(hero,text(dueNow > 0 ? dueNow + " words ready for review" :
                "You're up to date. New words are waiting.",14,HERO_INK,false));
        gap(hero,14);
        Button study=accentButton(!lessonSteps.isEmpty() && lessonPosition < lessonSteps.size() ?
                "Continue mission  →" : "Start " + (progress.deep()?"8":"4") + "-minute mission  →");
        study.setOnClickListener(v->beginLesson());
        add(hero,study);
        add(content,hero);
        if(dueNow > 0){
            Button review=button("Review due words only",false,true);
            review.setOnClickListener(v->beginReviewOnly());
            add(content,review);
            gap(content,12);
        }
        LinearLayout snapshot=card(WHITE);
        add(snapshot,text("YOUR LEARNING PULSE",12,MUTED,true));
        gap(snapshot,12);
        LinearLayout metrics=horizontal();
        statNumber(metrics,String.valueOf(dueNow),"Due",1);
        statNumber(metrics,String.valueOf(learned),"Started",1);
        statNumber(metrics,String.valueOf(strong),"Strong",1);
        add(snapshot,metrics);
        gap(snapshot,11);
        add(snapshot,text("" + learned + " / " + words.size() + " words introduced  ·  " +
                progress.daysInLastSeven()+" study days this week",12,MUTED,false));
        add(content,snapshot);
        add(content,text("Practice at your pace. Missing a day never resets your progress.",13,MUTED,false));
    }

    private void learnHub() {
        section(content,"Learn","Build a foundation with words, grammar and everyday conversation.");
        CurriculumUnit next=null;
        for(CurriculumUnit unit:curriculumRepository.throughLevel(level)){
            boolean complete=true;
            for(String id:unit.grammar)if(!progress.grammarDone(id))complete=false;
            for(String id:unit.dialogues)if(progress.dialogueAttempts(id)==0)complete=false;
            if(!complete){next=unit;break;}
        }
        LinearLayout focus=card(PALE);
        add(focus,text("STRUCTURED PATH",12,PRIMARY,true));
        gap(focus,7);
        add(focus,text(next == null ? "You've explored every unit" : next.title,22,INK,true));
        gap(focus,5);
        add(focus,text(next == null ? "You can revisit any unit for more practice." : next.goal,14,MUTED,false));
        gap(focus,12);
        final CurriculumUnit nextUnit=next;
        Button path=button(nextUnit==null ? "Review learning units  →" : "Continue this unit  →",true,false);
        path.setOnClickListener(v->{
            if(nextUnit==null)open("path");
            else {pathUnitId=nextUnit.id;open("path_unit");}
        });
        add(focus,path);
        gap(focus,7);
        Button allUnits=button("Browse all learning units",false,true);
        allUnits.setOnClickListener(v->open("path"));
        add(focus,allUnits);
        add(content,focus);
        section(content,"Study materials","Explore one learning resource at a time.");
        optionTile("字","Vocabulary library","Search characters, meanings, pinyin and examples",PALE,"words");
        optionTile("文","Grammar studio","Short explanations with a question to check recall",PALE,"grammar");
        optionTile("对","Mini conversations","Two-speaker audio with comprehension questions",PALE,"dialogues");
    }

    private void practiceHub() {
        section(content,"Practice","Choose a skill. Each activity has a distinct purpose.");
        optionTile("▣","Flashcards","Self-test recognition and recall",PALE,"cards");
        optionTile("听","Listening & reading MCQs","Short, targeted drills with feedback",PALE,"quiz");
        optionTile("声","Tone lab","Distinguish Mandarin tones by listening",PALE,"tones");
        optionTile("◷","Exam-length practice","Timed HSK-style vocabulary assessment",PALE,"mock");
        LinearLayout note=card(PALE);
        add(note,text("A note about exam scores",15,INK,true));
        gap(note,6);
        add(note,text("Our questions are unofficial vocabulary practice, not an exact replica or a validated prediction of an HSK score.",13,MUTED,false));
        add(content,note);
    }

    private void statNumber(LinearLayout row, String value, String label, float weight) {
        LinearLayout box=vertical(0);
        TextView count = text(value, 23, PRIMARY, true);
        count.setGravity(Gravity.CENTER);
        TextView name = text(label, 12, MUTED, false);
        name.setGravity(Gravity.CENTER);
        add(box,count);add(box,name);
        weighted(row,box,weight,0);
    }

    private void optionTile(String glyph, String title, String subtitle, int color, String destination) {
        LinearLayout tile=card(WHITE);
        LinearLayout row=horizontal();
        TextView mark=text(glyph,22,PRIMARY,true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(bg(color,12,0));
        row.addView(mark,new LinearLayout.LayoutParams(dp(42),dp(42)));
        LinearLayout label=vertical(0);
        label.setPadding(dp(12),0,dp(4),0);
        add(label,text(title,16,INK,true));
        gap(label,3);add(label,text(subtitle,13,MUTED,false));
        row.addView(label,new LinearLayout.LayoutParams(0,-2,1));
        add(row,text("›",22,PRIMARY,false));
        add(tile,row);
        tile.setOnClickListener(v->open(destination));
        add(content,tile);
    }

    private void words() {
        section(content, "Vocabulary library", "Tap a word to hear its pronunciation. ★ saves it.");
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setTextColor(INK);
        search.setHint("Search 汉字, pinyin or English...");
        search.setHintTextColor(MUTED);
        search.setPadding(dp(15), dp(12), dp(15), dp(12));
        search.setBackground(bg(WHITE, 14, BORDER));
        search.setText(wordSearch);
        add(content, search);
        gap(content, 10);
        Button savedButton = button(onlySaved ? "★ Saved words only" : "☆ Show saved only", onlySaved, true);
        savedButton.setOnClickListener(v -> { onlySaved = !onlySaved; wordLimit = 40; render(); });
        add(content, savedButton);
        gap(content, 16);
        LinearLayout holder = vertical(0);
        add(content, holder);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                wordSearch = s.toString(); wordLimit = 40; fillWordRows(holder);
            }
            @Override public void afterTextChanged(Editable e) {}
        });
        fillWordRows(holder);
    }

    private void fillWordRows(LinearLayout holder) {
        holder.removeAllViews();
        List<Word> matched = repository.search(version, level, wordSearch);
        if (onlySaved) {
            for (int i = matched.size() - 1; i >= 0; i--) if (!progress.saved(matched.get(i))) matched.remove(i);
        }
        add(holder, text(matched.size() + " words found", 12, MUTED, false));
        gap(holder, 10);
        for (int i = 0; i < Math.min(wordLimit, matched.size()); i++) {
            Word w = matched.get(i);
            LinearLayout tile = card(WHITE);
            LinearLayout row = horizontal();
            LinearLayout info = vertical(0);
            add(info, text(w.hanzi, 27, INK, true));
            add(info, text(w.pinyin, 14, PRIMARY, false));
            gap(info, 3);
            add(info, text(w.english, 13, MUTED, false));
            row.addView(info, new LinearLayout.LayoutParams(0, -2, 1f));
            TextView star = text(progress.saved(w) ? "★" : "☆", 26, PRIMARY, true);
            star.setPadding(dp(9), dp(8), dp(9), dp(8));
            star.setOnClickListener(v -> { progress.toggleSaved(w); fillWordRows(holder); });
            row.addView(star);
            add(tile, row);
            tile.setOnClickListener(v -> speak(w));
            add(holder, tile);
        }
        if (wordLimit < matched.size()) {
            Button more = button("Load 40 more words ↓", false, true);
            more.setOnClickListener(v -> { wordLimit += 40; fillWordRows(holder); });
            add(holder, more);
        }
        if (matched.isEmpty()) add(holder, text("No matches. Try another search or level.", 14, MUTED, false));
    }

    private Word nextDueCard() {
        ArrayList<Word> due = new ArrayList<>();
        for (Word w : deck()) if (progress.due(w)) due.add(w);
        if (due.isEmpty()) return null;
        if (currentCard != null && due.size() > 1) {
            for (int i = due.size() - 1; i >= 0; i--) if (due.get(i).id.equals(currentCard.id)) due.remove(i);
        }
        return due.get(random.nextInt(due.size()));
    }

    private void cards() {
        section(content, "Flashcard review", "See Chinese → reveal the answer → rate your recall.");
        List<Word> words = deck();
        add(content, text(progress.dueCount(words) + " cards due  ·  " + progress.learned(words) + " started", 13, MUTED, false));
        gap(content, 13);
        if (currentCard == null) currentCard = nextDueCard();
        if (currentCard == null) {
            LinearLayout finished = card(SUCCESS_BG);
            add(finished, text("All caught up! 🎉", 25, INK, true));
            gap(finished, 8);
            add(finished, text("Nothing is overdue. Start a guided mission for new vocabulary, or practice familiar words by choice.", 15, MUTED, false));
            gap(finished, 12);
            Button continueLearning = button("Learn new words  →", true, false);
            continueLearning.setOnClickListener(v -> beginLesson());
            add(finished, continueLearning);
            add(content, finished);
            return;
        }
        Word word = currentCard;
        LinearLayout flip = card(WHITE);
        flip.setGravity(Gravity.CENTER);
        flip.setMinimumHeight(dp(285));
        TextView label = text(cardFlipped ? "ANSWER" : "WHAT DOES THIS MEAN?", 12, MUTED, true);
        label.setGravity(Gravity.CENTER);
        add(flip, label);
        gap(flip, 20);
        TextView hanzi = text(word.hanzi, word.hanzi.length() > 6 ? 38 : 50, INK, true);
        hanzi.setGravity(Gravity.CENTER);
        add(flip, hanzi);
        gap(flip, 10);
        if (cardFlipped) {
            TextView pinyin = text(word.pinyin, 19, PRIMARY, false);
            pinyin.setGravity(Gravity.CENTER);
            add(flip, pinyin);
            gap(flip, 16);
            TextView answer = text(word.english, 20, INK, false);
            answer.setGravity(Gravity.CENTER);
            add(flip, answer);
        } else {
            TextView prompt = text("Tap to reveal", 15, MUTED, false);
            prompt.setGravity(Gravity.CENTER);
            add(flip, prompt);
        }
        flip.setOnClickListener(v -> { cardFlipped = !cardFlipped; render(); });
        add(content, flip);
        LinearLayout tools = horizontal();
        Button audio = button("▶ Listen", false, true);
        audio.setOnClickListener(v -> speak(word));
        Button reveal = button(cardFlipped ? "Hide answer" : "Reveal answer", true, false);
        reveal.setOnClickListener(v -> { cardFlipped = !cardFlipped; render(); });
        weighted(tools, audio, 1, 8);
        weighted(tools, reveal, 1, 0);
        add(content, tools);
        gap(content, 18);
        if (cardFlipped) {
            add(content, text("How well did you remember?", 16, INK, true));
            gap(content, 10);
            LinearLayout ratings = horizontal();
            Button again = button("Again", false, true);
            Button good = button("Good", true, false);
            Button easy = button("Easy", false, true);
            again.setOnClickListener(v -> rate(word, 0));
            good.setOnClickListener(v -> rate(word, 1));
            easy.setOnClickListener(v -> rate(word, 2));
            weighted(ratings, again, 1, 7);
            weighted(ratings, good, 1, 7);
            weighted(ratings, easy, 1, 0);
            add(content, ratings);
        } else {
            add(content, text("Try to recall the English meaning before revealing it. Ratings schedule future reviews.", 13, MUTED, false));
        }
    }

    private void rate(Word w, int quality) {
        progress.record(w, quality == 0 ? 0 : quality == 1 ? 2 : 3);
        progress.recordSkill(w, "reading", quality >= 1);
        currentCard = nextDueCard();
        cardFlipped = false;
        render();
    }

    private void quiz() {
        section(content, "Mini exam", "10 vocabulary questions · 4 choices · immediate feedback");
        if (quizQuestions.isEmpty()) {
            LinearLayout intro = card(WHITE);
            add(intro, text("Choose your practice mode", 19, INK, true));
            gap(intro, 13);
            LinearLayout modes = horizontal();
            Button reading = button("Reading", quizMode.equals("reading"), true);
            Button listening = button("Listening", quizMode.equals("listening"), true);
            reading.setOnClickListener(v -> { quizMode = "reading"; render(); });
            listening.setOnClickListener(v -> { quizMode = "listening"; render(); });
            weighted(modes, reading, 1, 8);
            weighted(modes, listening, 1, 0);
            add(intro, modes);
            gap(intro, 15);
            add(intro, text(quizMode.equals("reading") ?
                "See the Chinese word and choose its English meaning." :
                "Listen to a Mandarin word, then choose its meaning. A Chinese TTS voice is required.", 14, MUTED, false));
            gap(intro, 18);
            Button start = button("Start 10 questions  →", true, false);
            start.setOnClickListener(v -> startQuiz());
            add(intro, start);
            add(content, intro);
            Button longer=button("Try exam-length practice →",false,true);
            longer.setOnClickListener(v->open("mock"));add(content,longer);
            return;
        }
        if (quizIndex >= quizQuestions.size()) {
            showQuizResults();
            return;
        }
        Word question = quizQuestions.get(quizIndex);
        add(content, text("Question " + (quizIndex + 1) + " / " + quizQuestions.size() + "    ·    Score: " + quizScore, 13, MUTED, false));
        gap(content, 16);
        LinearLayout prompt = card(PALE);
        if (quizMode.equals("listening")) {
            add(prompt, text("LISTEN AND SELECT", 12, MUTED, true));
            gap(prompt, 16);
            Button play = button("▶ Play Chinese audio", true, false);
            play.setOnClickListener(v -> speak(question));
            add(prompt, play);
            gap(prompt, 12);
            add(prompt, text(answered ? question.hanzi + "  ·  " + question.pinyin : "The Chinese word is hidden until you answer.", 14, PRIMARY, false));
        } else {
            add(prompt, text("CHOOSE THE ENGLISH MEANING", 12, MUTED, true));
            gap(prompt, 13);
            add(prompt, text(question.hanzi, 44, INK, true));
            gap(prompt, 5);
            if (answered) add(prompt, text(question.pinyin, 17, PRIMARY, false));
        }
        add(content, prompt);
        List<Word> options = QuizEngine.choices(question, deck(), new Random(question.id.hashCode() + quizIndex));
        for (Word option : options) {
            boolean selected = option.id.equals(chosenId);
            boolean correct = option.id.equals(question.id);
            LinearLayout choice = card(answered && correct ? SUCCESS_BG : answered && selected ? ERROR_BG : WHITE);
            LinearLayout row = horizontal();
            TextView label = text(option.english, 16, answered && correct ? GREEN : answered && selected ? RED : INK, selected || answered && correct);
            row.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
            add(row, text(answered && correct ? "✓" : answered && selected ? "×" : "○", 19, PRIMARY, true));
            add(choice, row);
            if (!answered) choice.setOnClickListener(v -> chooseAnswer(option, question));
            add(content, choice);
        }
        if (answered) {
            add(content, text(chosenId.equals(question.id) ? "Correct! 很好！" : "Not quite. " + question.hanzi + " means: " + question.english,
                    15, chosenId.equals(question.id) ? GREEN : RED, true));
            gap(content, 14);
            Button next = button(quizIndex == quizQuestions.size() - 1 ? "See results  →" : "Next question  →", true, false);
            next.setOnClickListener(v -> {
                quizIndex++;
                answered = false;
                chosenId = "";
                if (quizIndex == quizQuestions.size()) { progress.finishQuiz(); progress.recordScore(quizScore); }
                render();
            });
            add(content, next);
        }
    }

    private void startQuiz() {
        if (deck().size() < 4) { showToast("At least four words needed to generate a quiz."); return; }
        if (quizMode.equals("listening") && !canHearEntireDeck()) {
            showToast("Complete offline audio pack or a Mandarin Android voice is needed for listening.");
            return;
        }
        quizQuestions = new ArrayList<>(QuizEngine.questions(deck(), 10, random));
        quizIndex = 0;
        quizScore = 0;
        answered = false;
        chosenId = "";
        render();
    }
    private void chooseAnswer(Word selected, Word answer) {
        if (answered) return;
        chosenId = selected.id;
        answered = true;
        if (selected.id.equals(answer.id)) quizScore++;
        progress.record(answer, selected.id.equals(answer.id) ? 2 : 0);
        progress.recordSkill(answer, quizMode.equals("listening") ? "listening" : "reading",selected.id.equals(answer.id));
        progress.recordPractice(quizMode, selected.id.equals(answer.id));
        render();
    }
    private void showQuizResults() {
        LinearLayout summary = card(SUCCESS_BG);
        add(summary, text("Session completed!", 24, INK, true));
        gap(summary, 15);
        add(summary, text(quizScore + " / " + quizQuestions.size(), 48, PRIMARY, true));
        gap(summary, 12);
        add(summary, text("This is a short vocabulary drill, not an official HSK exam or score prediction.", 14, MUTED, false));
        gap(summary, 18);
        Button retry = button("Practice again", true, false);
        retry.setOnClickListener(v -> { quizQuestions.clear(); render(); });
        add(summary, retry);
        add(content, summary);
    }

    private String skillRate(String skill) {
        int total = progress.skillAttempts(skill);
        return total == 0 ? "No attempts yet" : progress.skillCorrect(skill) + "/" + total + " · " + (100*progress.skillCorrect(skill)/total) + "%";
    }

    private void learningPath(){
        section(content,"Learning path","Ten practical units · study in order or choose a situation you need.");
        LinearLayout intro=card(PALE);
        add(intro,text("Learn → hear → answer → review",18,INK,true));
        gap(intro,6);
        add(intro,text("Complete a grammar explanation and comprehension conversation. Progress is carried over from earlier versions; no new accounts or resets.",13,MUTED,false));
        add(content,intro);
        int index=0;
        for(CurriculumUnit unit:curriculumRepository.throughLevel(level)){
            int total=unit.grammar.size()+unit.dialogues.size();
            int complete=0;
            for(String id:unit.grammar)if(progress.grammarDone(id))complete++;
            for(String id:unit.dialogues)if(progress.dialogueAttempts(id)>0)complete++;
            LinearLayout tile=card(WHITE);
            add(tile,text(String.format(Locale.ROOT,"%02d",++index)+"   "+unit.title,19,INK,true));
            gap(tile,6);
            add(tile,text(unit.goal,14,MUTED,false));
            gap(tile,7);
            add(tile,text(complete+" / "+total+" activities attempted · HSK "+unit.level,12,PRIMARY,false));
            tile.setOnClickListener(v->{pathUnitId=unit.id;open("path_unit");});
            add(content,tile);
        }
    }

    private void learningUnit(){
        CurriculumUnit selected=null;
        for(CurriculumUnit u:curriculumRepository.throughLevel(level))if(u.id.equals(pathUnitId)){selected=u;break;}
        if(selected==null){page="path";learningPath();return;}
        final CurriculumUnit u=selected;
        section(content,u.title,u.goal);
        add(content,text("STUDY THE PATTERNS",12,PRIMARY,true));
        gap(content,9);
        for(String id:u.grammar){
            for(GrammarRule g:grammarRepository.throughLevel(level))if(g.id.equals(id)){
                LinearLayout tile=card(WHITE);
                add(tile,text((progress.grammarDone(g.id)?"✓ ":"○ ")+g.title,17,INK,true));
                gap(tile,4);
                add(tile,text(g.pattern,13,MUTED,false));
                tile.setOnClickListener(v->{grammarSelected=g.id;grammarAnswered=false;grammarTesting=false;grammarChosen=-1;open("grammar_detail");});
                add(content,tile);
            }
        }
        gap(content,5);
        add(content,text("LISTEN AND UNDERSTAND",12,PRIMARY,true));
        gap(content,9);
        for(String id:u.dialogues){
            for(Dialogue d:dialogueRepository.throughLevel(level))if(d.id.equals(id)){
                LinearLayout tile=card(WHITE);
                add(tile,text((progress.dialogueAttempts(d.id)>0?"✓ ":"○ ")+d.title,17,INK,true));
                gap(tile,4);
                add(tile,text(progress.dialogueAttempts(d.id)>0 ?
                    progress.dialogueCorrect(d.id)+" correct / "+progress.dialogueAttempts(d.id)+" attempts" :
                    "Two voices when available · listening MCQ",13,MUTED,false));
                tile.setOnClickListener(v->{dialogueId=d.id;dialogueAnswer=-1;dialoguePlayed=false;
                    dialoguePlaying=false;dialogueReading=!canPlayDialogue(d);open("dialogue_detail");});
                add(content,tile);
            }
        }
        Button back=button("← All learning units",false,true);
        back.setOnClickListener(v->open("path"));
        add(content,back);
    }

    private void dialogues() {
        section(content,"Mini conversations","Original short dialogues · listen before you read · HSK 1–2");
        LinearLayout intro=card(PALE);
        add(intro,text("How to practise",17,INK,true));
        gap(intro,6);
        add(intro,text("Play the dialogue, answer without seeing the transcript, then check what you missed. You can switch to reading when speech is unavailable.",13,MUTED,false));
        add(content,intro);
        for(Dialogue d:dialogueRepository.throughLevel(level)){
            LinearLayout item=card(WHITE);
            add(item,text(d.title,18,INK,true));
            gap(item,5);
            add(item,text("HSK "+d.level+" · "+progress.dialogueCorrect(d.id)+" / "+progress.dialogueAttempts(d.id)+" correct",12,MUTED,false));
            item.setOnClickListener(v->{dialogueId=d.id;dialogueAnswer=-1;
                dialoguePlayed=false;dialoguePlaying=false;dialogueReading=!canPlayDialogue(d);page="dialogue_detail";render();});
            add(content,item);
        }
    }

    private void dialogueDetail() {
        Dialogue selected=null;
        for(Dialogue d:dialogueRepository.throughLevel(level)) if(d.id.equals(dialogueId)){selected=d;break;}
        if(selected==null){page="dialogues";dialogues();return;}
        final Dialogue d=selected;
        section(content,d.title,"HSK "+d.level+" · Listen → recall → check");
        LinearLayout prompt=card(PALE);
        add(prompt,text("TWO-SPEAKER PRACTICE",11,PRIMARY,true));
        gap(prompt,12);
        if(canPlayDialogue(d)){
            Button audio=button("▶ Play both lines",true,false);
            audio.setOnClickListener(v->playTwoSpeakers(d));
            add(prompt,audio);
            gap(prompt,7);
            add(prompt,text(dialoguePlaying ? "Playing speaker A, then speaker B…" :
                ((offlineAudio!=null && offlineAudio.hasDialogue(d.a,d.b)) ?
                "Two offline AI-generated Mandarin voices (female A, male B). " :
                "Android voice fallback. A: " + voiceLabel("a") + " / B: " + voiceLabel("b") + ". ") +
                "Audio must finish before the question unlocks.",12,MUTED,false));
        } else {
            add(prompt,text("Mandarin audio unavailable — use reading practice.",13,MUTED,false));
        }
        if((dialogueReading||dialogueAnswer>=0)){
            gap(prompt,13);
            add(prompt,text("A: "+d.a,22,INK,true));
            add(prompt,text("B: "+d.b,22,INK,true));
            if(dialogueAnswer>=0){
                gap(prompt,9);
                add(prompt,text("A: "+d.pa+" · "+d.ea,14,MUTED,false));
                gap(prompt,3);
                add(prompt,text("B: "+d.pb+" · "+d.eb,14,MUTED,false));
            }
        }
        add(content,prompt);
        if(dialogueAnswer<0 && !dialogueReading){
            Button read=button("Practise by reading instead",false,true);
            read.setOnClickListener(v->{dialogueReading=true;render();});
            add(content,read);
        }
        if(dialogueReading||dialoguePlayed||dialogueAnswer>=0){
            section(content,"What did you understand?","Choose the best answer without guessing from the transcript.");
            add(content,text(d.question,19,INK,true));
            gap(content,12);
            for(int i=0;i<d.choices.size();i++){
                final int choice=i;
                LinearLayout option=card(dialogueAnswer>=0&&i==d.answer?SUCCESS_BG:WHITE);
                add(option,text(d.choices.get(i)+(dialogueAnswer>=0&&i==d.answer?"  ✓":""),16,INK,i==dialogueAnswer));
                if(dialogueAnswer<0)option.setOnClickListener(v->{
                    dialogueAnswer=choice;
                    boolean correct=choice==d.answer;
                    progress.recordDialogue(d.id,correct);
                    progress.recordPractice(!dialogueReading?"listening":"reading",correct);
                    render();
                });
                add(content,option);
            }
        }else{
            add(content,text("Play the dialogue to unlock the comprehension question.",13,MUTED,false));
        }
        if(dialogueAnswer>=0){
            LinearLayout feedback=card(WHITE);
            add(feedback,text(dialogueAnswer==d.answer?"Correct understanding":"Review the meaning",17,
                    dialogueAnswer==d.answer?GREEN:RED,true));
            gap(feedback,7);
            add(feedback,text(d.feedback,14,INK,false));
            add(content,feedback);
            Button again=button("Practise again",false,true);
            again.setOnClickListener(v->{if(dialoguePlayer!=null)dialoguePlayer.cancel();dialogueAnswer=-1;dialoguePlayed=false;dialoguePlaying=false;dialogueReading=!canPlayDialogue(d);render();});
            add(content,again);
        }
        gap(content,8);
        Button back=button("← All conversations",false,true);
        back.setOnClickListener(v->open("dialogues"));
        add(content,back);
    }

    private void grammar() {
        section(content,"Grammar studio","Original HSK 1–2 patterns · short explanations and a retrieval check.");
        List<GrammarRule> rules=grammarRepository.throughLevel(level);
        int completed=0;
        for(GrammarRule rule:rules) if(progress.grammarDone(rule.id)) completed++;
        LinearLayout summary=card(PALE);
        add(summary,text(completed + " / " + rules.size() + " patterns checked",21,PRIMARY,true));
        gap(summary,6);
        add(summary,text("Try to answer before viewing the explanation again. No penalty for retries.",13,MUTED,false));
        add(content,summary);
        for(GrammarRule rule:rules){
            LinearLayout tile=card(WHITE);
            add(tile,text((progress.grammarDone(rule.id)?"✓ ":"○ ")+rule.title,18,INK,true));
            gap(tile,6);
            add(tile,text(rule.pattern,15,PRIMARY,false));
            gap(tile,5);
            add(tile,text("HSK "+rule.level+" · Tap to study",12,MUTED,false));
            tile.setOnClickListener(v->{grammarSelected=rule.id;grammarAnswered=false;grammarTesting=false;grammarChosen=-1;page="grammar_detail";render();});
            add(content,tile);
        }
    }

    private void grammarDetail(){
        GrammarRule rule=null;
        for(GrammarRule r:grammarRepository.throughLevel(level)) if(r.id.equals(grammarSelected)) {rule=r;break;}
        if(rule==null){page="grammar";grammar();return;}
        final GrammarRule selected=rule;
        section(content,rule.title,"HSK "+rule.level+" · Learn → retrieve → apply");
        if(!grammarTesting){
        LinearLayout lesson=card(PALE);
        add(lesson,text("PATTERN",12,PRIMARY,true));
        gap(lesson,8);
        add(lesson,text(rule.pattern,21,INK,true));
        gap(lesson,14);
        add(lesson,text(rule.explain,15,INK,false));
        add(content,lesson);
        LinearLayout sentence=card(WHITE);
        add(sentence,text(rule.hanzi,27,INK,true));
        gap(sentence,5);
        add(sentence,text(rule.pinyin,16,PRIMARY,false));
        gap(sentence,5);
        add(sentence,text(rule.translation,15,MUTED,false));
        gap(sentence,10);
        Button hear=button("▶ Hear example",false,true);
        hear.setOnClickListener(v->speakChinese(selected.hanzi,selected.id));
        add(sentence,hear);add(content,sentence);
        Button test=button("I’m ready — test from memory →",true,false);
        test.setOnClickListener(v->{grammarTesting=true;grammarAnswered=false;grammarChosen=-1;render();});
        add(content,test);
        return;
        }
        section(content,"Check your understanding","Retrieve the pattern before revealing the explanation.");
        add(content,text(rule.question,18,INK,true));
        gap(content,14);
        for(int i=0;i<rule.choices.size();i++){
            final int index=i;
            LinearLayout choice=card(grammarAnswered && i==rule.answer ? SUCCESS_BG :
                grammarAnswered && i==grammarChosen ? ERROR_BG: WHITE);
            add(choice,text((i+1)+".  "+rule.choices.get(i),16,INK,grammarAnswered && i==rule.answer));
            if(!grammarAnswered) choice.setOnClickListener(v->{
                grammarAnswered=true;grammarChosen=index;
                if(index==selected.answer) progress.markGrammarDone(selected.id);
                render();
            });
            add(content,choice);
        }
        if(grammarAnswered){
            LinearLayout feedback=card(grammarChosen==rule.answer?SUCCESS_BG:WARNING_BG);
            add(feedback,text(grammarChosen==rule.answer ? "✓ You recognized the pattern" : "Not yet — review the example and retry",16,INK,true));
            gap(feedback,8);
            add(feedback,text(rule.feedback,14,INK,false));
            add(content,feedback);
            LinearLayout model=card(WHITE);
            add(model,text("MODEL & EXPLANATION",12,PRIMARY,true));
            gap(model,7);add(model,text(rule.hanzi,24,INK,true));
            gap(model,5);add(model,text(rule.pinyin,15,PRIMARY,false));
            gap(model,5);add(model,text(rule.translation,14,MUTED,false));
            gap(model,9);add(model,text(rule.explain,14,INK,false));
            add(content,model);
            if(grammarChosen!=rule.answer){
                Button again=button("Try once more",true,false);
                again.setOnClickListener(v->{grammarAnswered=false;grammarChosen=-1;render();});add(content,again);
            }
        }
        Button back=button("← All grammar patterns",false,true);
        back.setOnClickListener(v->{page="grammar";grammarTesting=false;render();});add(content,back);
        Button revisit=button("Study the explanation again",false,true);
        revisit.setOnClickListener(v->{grammarTesting=false;grammarAnswered=false;grammarChosen=-1;render();});add(content,revisit);
    }

    private void mockExam(){
        if(examQuestions.isEmpty()){
            section(content,"Exam-length practice","Unofficial, classic HSK word recognition · not an official exam replica.");
            LinearLayout introduction=card(WHITE);
            add(introduction,text("HSK "+level+" · "+ExamBlueprint.total(level)+" questions",23,INK,true));
            gap(introduction,12);
            add(introduction,text(ExamBlueprint.listeningCount(level)+" listening · "+ExamBlueprint.readingCount(level)+" reading · "+ExamBlueprint.minutes(level)+" minute limit",16,PRIMARY,true));
            gap(introduction,12);
            add(introduction,text("Questions use generated vocabulary prompts, not the full range of official listening dialogues, pictures and reading tasks. Scores are for personal practice only.",14,MUTED,false));
            gap(introduction,8);
            add(introduction,text("Mandarin audio is required (offline recordings or device TTS). Answers are hidden until the end; the test advances after each choice. No penalty for taking a break before starting.",14,MUTED,false));
            gap(introduction,12);
            add(introduction,text("This practice follows classic HSK 2.0 counts only; it does not represent the upcoming revised HSK 3.0 examination.",13,RED,false));
            gap(introduction,16);
            Button start=button("Start timed practice →",true,false);
            start.setOnClickListener(v->startMock());add(introduction,start);
            add(content,introduction);
            return;
        }
        if(!examFinished && SystemClock.elapsedRealtime()>=examDeadlineMs) finishMock();
        if(examFinished){showMockResult();return;}
        Word w=examQuestions.get(examIndex);
        final int questionPosition=examIndex;
        boolean listening=examIndex < ExamBlueprint.listeningCount(examLevel);
        long remaining=Math.max(0,(examDeadlineMs-SystemClock.elapsedRealtime())/1000);
        add(content,text("HSK "+examLevel+" · "+(listening?"LISTENING":"READING")+" · "+(examIndex+1)+"/"+examQuestions.size(),13,PRIMARY,true));
        gap(content,6);
        add(content,text("Time left: "+String.format(Locale.US,"%02d:%02d",remaining/60,remaining%60),15,MUTED,true));
        gap(content,14);
        LinearLayout prompt=card(PALE);
        if(listening){
            add(prompt,text("Listen and choose the English meaning.",15,INK,true));
            gap(prompt,12);
            Button play=button("▶ Play Mandarin",true,false);
            play.setOnClickListener(v->{examAudioPlayed=true;speak(w);render();});add(prompt,play);
            gap(prompt,9);
            add(prompt,text("You can replay the word. Its characters are hidden.",12,MUTED,false));
        }else{
            add(prompt,text("Read the Chinese and choose its meaning.",15,INK,true));
            gap(prompt,14);
            TextView hanzi=text(w.hanzi,w.hanzi.length()>6?32:43,INK,true);
            hanzi.setGravity(Gravity.CENTER);add(prompt,hanzi);
        }
        add(content,prompt);
        List<Word> options=QuizEngine.choices(w,deck(),new Random(w.id.hashCode()+examIndex));
        if(listening && !examAudioPlayed){
            add(content,text("Play the Mandarin audio before choosing an answer.",13,MUTED,false));
        }
        for(Word option:options){
            if(listening && !examAudioPlayed) continue;
            LinearLayout choice=card(WHITE);
            add(choice,text(option.english,16,INK,false));
            choice.setOnClickListener(v->{
                if(examFinished || SystemClock.elapsedRealtime()>=examDeadlineMs){finishMock();render();return;}
                if(questionPosition != examIndex) return;
                examAnswers.add(option.id.equals(w.id));
                examAudioPlayed=false;
                examIndex++;
                if(examIndex>=examQuestions.size()) finishMock();
                render();
            });
            add(content,choice);
        }
        gap(content,8);
        add(content,text("No feedback during the assessment. Your results appear at the end.",12,MUTED,false));
        gap(content,6);
        Button end=button("End practice early",false,true);
        end.setOnClickListener(v->{finishMock();render();});add(content,end);
    }

    private void startMock(){
        if(!version.equals("2.0")){
            showToast("Switch to Classic HSK 2.0 in Settings to use this exam-length mode.");return;
        }
        if(!canHearEntireDeck()){showToast("Complete offline word audio or Android Mandarin TTS is needed for the listening exam.");return;}
        if(deck().size()<ExamBlueprint.total(level)){
            showToast("Download the complete vocabulary pack with the Debian build script first.");return;
        }
        examQuestions.clear();examAnswers.clear();
        examQuestions.addAll(ExamBlueprint.questions(deck(),level,random));
        examLevel=level;examIndex=0;examFinished=false;examAudioPlayed=false;examShowMistakes=false;
        examDeadlineMs=SystemClock.elapsedRealtime()+ExamBlueprint.minutes(level)*60_000L;
        examTimer.removeCallbacksAndMessages(null);
        examTimeUp=()->{if(!examFinished && !examQuestions.isEmpty()){finishMock();if(page.equals("mock"))render();}};
        examTimer.postDelayed(examTimeUp,ExamBlueprint.minutes(level)*60_000L);
        page="mock";render();
    }
    private void finishMock(){
        if(examFinished || examQuestions.isEmpty())return;
        examFinished=true;
        examTimer.removeCallbacksAndMessages(null);
        int right=0,listeningRight=0;
        int lc=ExamBlueprint.listeningCount(examLevel);
        for(int i=0;i<examAnswers.size();i++)if(examAnswers.get(i)){
            right++;if(i<lc)listeningRight++;
        }
        progress.recordExam(examLevel,right,examQuestions.size(),listeningRight,lc);
    }
    private void showMockResult(){
        int right=0,listen=0;
        int lc=ExamBlueprint.listeningCount(examLevel);
        for(int i=0;i<examAnswers.size();i++)if(examAnswers.get(i)){right++;if(i<lc)listen++;}
        LinearLayout result=card(SUCCESS_BG);
        add(result,text("Practice finished",23,INK,true));
        gap(result,14);
        add(result,text(right+" / "+examQuestions.size()+" correct",36,PRIMARY,true));
        gap(result,10);
        add(result,text("Listening: "+listen+"/"+lc,16,INK,false));
        add(result,text("Reading: "+(right-listen)+"/"+ExamBlueprint.readingCount(examLevel),16,INK,false));
        add(result,text("Answered: "+examAnswers.size()+"/"+examQuestions.size(),14,MUTED,false));
        gap(result,9);
        int readingCorrect=right-listen;
        int readingTotal=ExamBlueprint.readingCount(examLevel);
        boolean listeningNeedsWork = listen*readingTotal <= readingCorrect*lc;
        add(result,text("Suggested next focus: "+(listeningNeedsWork?"Listening MCQs":"Reading MCQs"),14,PRIMARY,true));
        Button focusButton=button("Practise "+(listeningNeedsWork?"listening":"reading")+" now  →",false,true);
        focusButton.setOnClickListener(v->{quizMode=listeningNeedsWork?"listening":"reading";quizQuestions.clear();open("quiz");});
        add(result,focusButton);
        gap(result,12);
        add(result,text("Not an official HSK score or prediction. The question types are simplified vocabulary prompts; use the results only to identify practice needs.",13,MUTED,false));
        gap(result,16);
        int misses = examQuestions.size()-right;
        if(misses>0){
            Button mistakes=button((examShowMistakes?"Hide":"Review")+" missed items ("+misses+")",false,true);
            mistakes.setOnClickListener(v->{examShowMistakes=!examShowMistakes;render();});
            add(content,mistakes);
            if(examShowMistakes){
                LinearLayout missed=card(WHITE);
                add(missed,text("Words to revisit",19,INK,true));gap(missed,10);
                int shown=0;
                for(int i=0;i<examQuestions.size();i++){
                    if(i<examAnswers.size() && examAnswers.get(i))continue;
                    Word w=examQuestions.get(i);
                    add(missed,text(w.hanzi+" · "+w.pinyin+" · "+w.english,14,INK,false));
                    gap(missed,9);
                    if(++shown>=20)break;
                }
                if(misses>shown)add(missed,text("Showing first "+shown+" missed items.",12,MUTED,false));
                add(content,missed);
            }
        }
        Button again=button("Start a new practice",true,false);
        again.setOnClickListener(v->{examQuestions.clear();examAnswers.clear();examFinished=false;examShowMistakes=false;render();});add(result,again);
        add(content,result);
    }

    private void stats() {
        section(content, "Your progress", "Progress is stored locally on this Android device.");
        for (int lvl = 1; lvl <= 2; lvl++) {
            List<Word> list = repository.throughLevel(version, lvl);
            LinearLayout tile = card(WHITE);
            add(tile, text("HSK " + lvl, 21, INK, true));
            gap(tile, 8);
            add(tile, text(progress.learned(list) + " started   ·   " + progress.strong(list) + " strong   ·   " + list.size() + " total", 15, PRIMARY, false));
            gap(tile, 5);
            add(tile, text(progress.dueCount(list) + " cards currently due", 13, MUTED, false));
            add(content, tile);
        }
        LinearLayout all = card(PALE);
        add(all, text("Lifetime study", 18, INK, true));
        gap(all, 8);
        add(all, text("Retrieval attempts: " + progress.reviews(), 15, INK, false));
        add(all, text("Guided missions completed: " + progress.sessions(), 15, INK, false));
        add(all, text("Days practiced in the last 7: " + progress.daysInLastSeven(), 15, INK, false));
        add(all, text("Quizzes finished: " + progress.quizzes(), 15, INK, false));
        add(all, text("Best mini exam score: " + progress.best() + "/10", 15, INK, false));
        add(content, all);
        section(content,"Memory by skill","Existing review history is credited only to reading; listening and production develop independently.");
        LinearLayout independent = card(WHITE);
        int rDue=progress.dueForSkill(deck(),"reading"), lDue=progress.dueForSkill(deck(),"listening"), pDue=progress.dueForSkill(deck(),"production");
        add(independent,text("Reading due: " + rDue + "  ·  Listening due: " + lDue + "  ·  Production due: " + pDue,15,INK,true));
        gap(independent,7);
        int delayed=progress.delayedAttempts();
        add(independent,text(delayed==0?"Seven-day retention: awaiting first delayed re-test":"Seven-day return accuracy: "+progress.delayedCorrect()+"/"+delayed,13,MUTED,false));
        add(content,independent);
        section(content,"Practice accuracy","These are observed practice answers, not a prediction of exam performance.");
        LinearLayout skills=card(WHITE);
        for(String skill:Arrays.asList("reading","listening","typing","context","tone")){
            add(skills,text(skill.substring(0,1).toUpperCase(Locale.ROOT)+skill.substring(1)+": "+skillRate(skill),15,INK,false));
            gap(skills,8);
        }
        add(content,skills);
        int dialogueAttempts=0, dialogueCorrect=0;
        for(Dialogue d:dialogueRepository.throughLevel(level)){
            dialogueAttempts+=progress.dialogueAttempts(d.id);
            dialogueCorrect+=progress.dialogueCorrect(d.id);
        }
        LinearLayout transfer=card(WHITE);
        add(transfer,text("Conversation comprehension",18,INK,true));
        gap(transfer,6);
        add(transfer,text(dialogueAttempts==0 ? "Try a short dialogue to start tracking comprehension."
                : dialogueCorrect + " / " + dialogueAttempts + " answers correct",14,MUTED,false));
        add(content,transfer);
        section(content,"Last seven days","Questions checked per day · most recent first.");
        LinearLayout week=card(WHITE);
        for(int daysAgo=0;daysAgo<7;daysAgo++){
            final int attempted=progress.dayAttempts(daysAgo);
            final int correct=progress.dayCorrect(daysAgo);
            LinearLayout row=horizontal();
            String day=daysAgo==0?"Today":daysAgo==1?"Yesterday":daysAgo+" days ago";
            TextView caption=text(day,13,MUTED,false);
            weighted(row,caption,1,5);
            add(row,text(attempted==0?"—":correct+"/"+attempted,14,INK,true));
            add(week,row);gap(week,9);
        }
        add(content,week);
        for(int i=1;i<=2;i++){
            if(progress.examCount(i)>0){
                LinearLayout attempt=card(PALE);
                add(attempt,text("Unofficial HSK "+i+" exam-length practice",17,INK,true));
                gap(attempt,5);
                add(attempt,text(progress.examCount(i)+" attempts · latest "+progress.examLast(i)+"% · best "+progress.examBest(i)+"%",14,PRIMARY,false));
                add(content,attempt);
            }
        }
    }

    private void beginLesson() { beginLesson(false); }
    private void beginReviewOnly() { beginLesson(true); }
    private void beginLesson(boolean reviewOnly) {
        // Keep an unfinished lesson when the learner navigates home and returns.
        if (!lessonSteps.isEmpty() && lessonPosition < lessonSteps.size()) {
            page = "lesson"; render(); return;
        }
        List<Word> due = new ArrayList<>();
        List<Word> unseen = new ArrayList<>();
        List<Word> familiar = new ArrayList<>();
        for (Word w : deck()) {
            if (!progress.seen(w)) unseen.add(w);
            else if (progress.due(w)) due.add(w);
            else familiar.add(w);
        }
        long now = System.currentTimeMillis();
        Collections.sort(due, (a, b) -> {
            long pa=ReviewFocus.priority(progress.stage(a),progress.pastMisses(a),progress.dueTime(a),now);
            long pb=ReviewFocus.priority(progress.stage(b),progress.pastMisses(b),progress.dueTime(b),now);
            return pa==pb ? Long.compare(progress.dueTime(a),progress.dueTime(b)) : Long.compare(pb,pa);
        });
        Collections.sort(unseen, Comparator.comparingInt(w -> {
            int priority = FOUNDATION.indexOf(w.hanzi);
            return priority < 0 ? 1000 : priority;
        }));
        lessonSteps.clear();
        if (reviewOnly && due.isEmpty()) {
            showToast("No reviews are due yet. Try a regular mission instead.");
            return;
        }
        lessonSteps.addAll(SessionPlanner.plan(due, reviewOnly ? Collections.emptyList() : unseen,
                reviewOnly ? Collections.emptyList() : familiar, progress.deep(), random));
        // Include applied reading practice for two recently introduced words.
        int clozeCount = 0;
        for (Exercise e : new ArrayList<>(lessonSteps)) {
            if ((e.kind == Exercise.Kind.INTRO || (reviewOnly && e.kind == Exercise.Kind.RECALL))
                    && examples.find(e.word) != null && clozeCount < 2) {
                lessonSteps.add(new Exercise(Exercise.Kind.CLOZE, e.word));
                clozeCount++;
            }
        }
        lessonPosition = 0;
        lessonCorrect = 0;
        lessonAttempts = 0;
        lessonRevealed = false;
        lessonAnswered = false;
        lessonAudioPlayed = false;
        lessonWasCorrect = false;
        lessonChoice = "";
        lessonAnswerText = "";
        retryPlanned.clear();
        if (lessonSteps.isEmpty()) { showToast("No vocabulary available in this deck."); return; }
        page = "lesson";
        render();
    }

    private void lesson() {
        if (lessonSteps.isEmpty()) {
            section(content, "Start learning", "Choose your HSK level first.");
            Button start = button("Create today's mission", true, false);
            start.setOnClickListener(v -> beginLesson());
            add(content, start);
            return;
        }
        if (lessonPosition >= lessonSteps.size()) { page = "done"; render(); return; }
        Exercise ex = lessonSteps.get(lessonPosition);
        Word w = ex.word;
        String kindLabel;
        switch (ex.kind) {
            case INTRO: kindLabel = "DISCOVER"; break;
            case LISTEN: kindLabel = "LISTEN"; break;
            case TYPE: kindLabel = "PRODUCE"; break;
            case CLOZE: kindLabel = "IN CONTEXT"; break;
            case SHADOW: kindLabel = "SPEAK"; break;
            default: kindLabel = ex.isRetry ? "SECOND CHANCE" : "RECALL"; break;
        }
        int total = lessonSteps.size();
        add(content, text(kindLabel + "   •   " + (lessonPosition + 1) + " / " + total, 13, PRIMARY, true));
        gap(content, 11);
        FrameLayout track = new FrameLayout(this);
        track.setBackground(bg(PALE, 9, 0));
        content.addView(track, new LinearLayout.LayoutParams(-1, dp(9)));
        View fill = new View(this);
        fill.setBackground(bg(PRIMARY, 9, 0));
        track.addView(fill, new FrameLayout.LayoutParams(-1, -1));
        track.post(() -> {
            ViewGroup.LayoutParams p = fill.getLayoutParams();
            p.width = (int)(track.getWidth() * ((double)lessonPosition / total));
            fill.setLayoutParams(p);
        });
        gap(content, 20);
        SentenceExample sample = examples.find(w);
        if (ex.kind == Exercise.Kind.INTRO) {
            section(content, "Meet a new word", "Look, listen, and connect it to a meaningful sentence.");
            LinearLayout reveal = card(WHITE);
            TextView hz = text(w.hanzi, w.hanzi.length() > 5 ? 40 : 56, INK, true);
            hz.setGravity(Gravity.CENTER); add(reveal, hz);
            TextView py = text(w.pinyin, 20, PRIMARY, false); py.setGravity(Gravity.CENTER); add(reveal, py);
            gap(reveal, 12);
            TextView meaning = text(w.english, 19, INK, false); meaning.setGravity(Gravity.CENTER); add(reveal, meaning);
            gap(reveal, 16);
            Button audio = button("▶ Hear Mandarin", false, true);
            audio.setOnClickListener(v -> speak(w)); add(reveal, audio);
            add(content, reveal);
            if (sample != null) examplePanel(sample, false);
            add(content, text("Tip: say the word once, picture its meaning, then continue. We'll ask you to recall it later.", 14, MUTED, false));
            gap(content, 14);
            Button next = button("I’m ready — continue  →", true, false);
            next.setOnClickListener(v -> { progress.markIntroduced(w); advanceLesson(); });
            add(content, next);
            return;
        }
        if (ex.kind == Exercise.Kind.RECALL) {
            section(content, "Recall the meaning", "Try an answer from memory before revealing it.");
            LinearLayout prompt = card(WHITE);
            prompt.setMinimumHeight(dp(230)); prompt.setGravity(Gravity.CENTER);
            TextView hz = text(w.hanzi, w.hanzi.length() > 6 ? 38 : 54, INK, true);
            hz.setGravity(Gravity.CENTER); add(prompt, hz);
            gap(prompt, 8);
            if (lessonRevealed) {
                TextView py = text(w.pinyin, 19, PRIMARY, false); py.setGravity(Gravity.CENTER); add(prompt, py);
                gap(prompt, 10);
                TextView meaning = text(w.english, 19, INK, true); meaning.setGravity(Gravity.CENTER); add(prompt, meaning);
            } else {
                TextView hint = text("What does it mean? Say it before revealing.", 14, MUTED, false);
                hint.setGravity(Gravity.CENTER); add(prompt, hint);
            }
            add(content, prompt);
            Button hear = button("▶ Hear the word", false, true);
            hear.setOnClickListener(v -> speak(w)); add(content, hear);
            gap(content, 13);
            if (!lessonRevealed) {
                Button reveal = button("Reveal the answer", true, false);
                reveal.setOnClickListener(v -> { lessonRevealed = true; render(); }); add(content, reveal);
            } else {
                if (sample != null) examplePanel(sample, false);
                selfRating(w, "How clearly did you recall it?");
            }
            return;
        }
        if (ex.kind == Exercise.Kind.SHADOW) {
            section(content, "Speak it aloud", "Hear the word, repeat it, then compare with the model.");
            LinearLayout speakPanel = card(PALE);
            add(speakPanel, text("SHADOW THE SOUND", 12, PRIMARY, true));
            gap(speakPanel, 18);
            if (canHear(w)) {
                Button play = button("▶ Play pronunciation", true, false);
                play.setOnClickListener(v -> speak(w)); add(speakPanel, play);
            } else {
                add(speakPanel, text("Mandarin TTS is not installed. Use the written model below.", 14, MUTED, false));
            }
            gap(speakPanel, 14);
            if (lessonRevealed) {
                add(speakPanel, text(w.hanzi + "   " + w.pinyin, 26, INK, true));
                add(speakPanel, text(w.english, 16, MUTED, false));
            } else {
                add(speakPanel, text(canHear(w) ? "Repeat the sound out loud twice." : w.hanzi,
                        17, INK, false));
            }
            add(content, speakPanel);
            if (!lessonRevealed) {
                Button model = button("Show written model", true, false);
                model.setOnClickListener(v -> { lessonRevealed = true; render(); }); add(content, model);
            } else {
                if (sample != null) examplePanel(sample, false);
                add(content, text("Self-rating only — the app does not assess speech accuracy.", 12, MUTED, false));
                gap(content, 10);
                selfRating(w, "Could you repeat it confidently?");
            }
            return;
        }
        if (ex.kind == Exercise.Kind.TYPE) {
            section(content, "Produce it from memory", "Translate the meaning into Chinese pinyin.");
            LinearLayout prompt = card(PALE);
            add(prompt, text("ENGLISH → PINYIN", 12, PRIMARY, true));
            gap(prompt, 15);
            add(prompt, text(w.english, 24, INK, true));
            add(content, prompt);
            if (!lessonAnswered) {
                EditText input = new EditText(this);
                input.setSingleLine(true);
                input.setTextSize(19);
                input.setTextColor(INK);
                input.setHint("Type pinyin, e.g. ni hao");
                input.setHintTextColor(MUTED);
                input.setPadding(dp(15), dp(14), dp(15), dp(14));
                input.setBackground(bg(WHITE, 14, BORDER));
                add(content, input);
                gap(content, 10);
                add(content, text(progress.strictTones() ?
                        "Tone marks or tone numbers required, e.g. nǐ hǎo or ni3 hao3."
                        : "Beginner mode: tones are optional. You may enable tone-aware typing in Settings.", 12, MUTED, false));
                gap(content, 12);
                Button check = button("Check my answer  →", true, false);
                check.setOnClickListener(v -> {
                    String attempt = input.getText().toString().trim();
                    if (attempt.isEmpty()) { showToast("Try an answer first; you can always guess."); return; }
                    gradeObjective(ex, progress.strictTones() ? PinyinCheck.toneMatches(attempt,w.pinyin) : PinyinCheck.matches(attempt, w.pinyin, w.hanzi), attempt);
                });
                add(content, check);
            } else {
                add(content, text("You wrote: " + lessonAnswerText, 14, MUTED, false));
                answerPanel(w, sample);
                nextLessonButton();
            }
            return;
        }
        // Both listening and cloze are objective recognition tasks with immediate feedback.
        boolean cloze = ex.kind == Exercise.Kind.CLOZE && sample != null;
        section(content, cloze ? "Fill the sentence" : "Listen and understand",
                cloze ? "Choose the Chinese word that fits the context." : "Listen before reading the choices.");
        LinearLayout prompt = card(PALE);
        if (cloze) {
            add(prompt, text("READ THE SENTENCE", 12, PRIMARY, true));
            gap(prompt, 12);
            add(prompt, text(sample.cloze(), 27, INK, true));
            gap(prompt, 10);
            add(prompt, text(sample.english, 15, MUTED, false));
        } else if (canHear(w)) {
            add(prompt, text("AUDIO → MEANING", 12, PRIMARY, true));
            gap(prompt, 17);
            Button audio = button("▶ Play Chinese audio", true, false);
            audio.setOnClickListener(v -> {lessonAudioPlayed=true;speak(w);render();}); add(prompt, audio);
            gap(prompt, 10);
            add(prompt, text("The characters are hidden until you answer.", 13, MUTED, false));
        } else {
            add(prompt, text("READING FALLBACK", 12, PRIMARY, true));
            gap(prompt, 12);
            add(prompt, text(w.hanzi, 39, INK, true));
            gap(prompt, 10);
            add(prompt, text("Mandarin TTS unavailable; this task uses reading instead.", 12, MUTED, false));
        }
        add(content, prompt);
        List<Word> choices = QuizEngine.choices(w, deck(), new Random(w.id.hashCode() + lessonPosition));
        for (Word choice : choices) {
            boolean correct = choice.id.equals(w.id);
            boolean selected = choice.id.equals(lessonChoice);
            LinearLayout item = card(lessonAnswered && correct ? SUCCESS_BG :
                    lessonAnswered && selected ? ERROR_BG : WHITE);
            LinearLayout row = horizontal();
            TextView label = text(cloze ? choice.hanzi : choice.english, 17,
                    lessonAnswered && correct ? GREEN : lessonAnswered && selected ? RED : INK,
                    lessonAnswered && correct);
            row.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
            add(row, text(lessonAnswered && correct ? "✓" : lessonAnswered && selected ? "×" : "○",
                    19, PRIMARY, true));
            add(item, row); add(content, item);
            item.setOnClickListener(v -> {
                if (!lessonAnswered && (cloze || !canHear(w) || lessonAudioPlayed)) gradeObjective(ex, correct, choice.id);
                else if(!lessonAnswered)showToast("Listen to the word first.");
            });
        }
        if (lessonAnswered) {
            answerPanel(w, sample);
            nextLessonButton();
        }
    }

    private void examplePanel(SentenceExample sample, boolean hideTarget) {
        LinearLayout panel = card(INFO_BG);
        add(panel, text("IN A REAL SENTENCE", 11, PRIMARY, true));
        gap(panel, 8);
        add(panel, text(hideTarget ? sample.cloze() : sample.chinese, 23, INK, true));
        gap(panel, 5);
        add(panel, text(sample.pinyin, 14, PRIMARY, false));
        gap(panel, 4);
        add(panel, text(sample.english, 14, MUTED, false));
        gap(panel, 8);
        Button hearSentence = button("▶ Hear sentence", false, true);
        hearSentence.setOnClickListener(v -> speakChinese(sample.chinese, "sentence-" + sample.target));
        add(panel, hearSentence);
        add(content, panel);
    }

    private void answerPanel(Word w, SentenceExample sample) {
        LinearLayout panel = card(lessonWasCorrect ? SUCCESS_BG : WARNING_BG);
        add(panel, text(lessonWasCorrect ? "✓ Well retrieved" : "↗ Useful mistake — now you know", 15,
                lessonWasCorrect ? GREEN : RED, true));
        gap(panel, 12);
        add(panel, text(w.hanzi + "  ·  " + w.pinyin, 26, INK, true));
        gap(panel, 5);
        add(panel, text(w.english, 17, INK, false));
        gap(panel, 10);
        Button listen = button("▶ Listen again", false, true);
        listen.setOnClickListener(v -> speak(w)); add(panel, listen);
        add(content, panel);
        if (sample != null) examplePanel(sample, false);
        if (!lessonWasCorrect) add(content, text("We'll give you a second chance near the end of this mission.", 12, MUTED, false));
    }

    private void selfRating(Word w, String heading) {
        add(content, text(heading, 16, INK, true));
        gap(content, 10);
        LinearLayout row1 = horizontal();
        Button again = button("Not yet", false, true);
        Button hard = button("Almost", false, true);
        again.setOnClickListener(v -> finishLessonStep(0));
        hard.setOnClickListener(v -> finishLessonStep(1));
        weighted(row1, again, 1, 9); weighted(row1, hard, 1, 0); add(content, row1);
        gap(content, 8);
        LinearLayout row2 = horizontal();
        Button good = button("I recalled it", true, false);
        Button easy = button("Effortless", false, true);
        good.setOnClickListener(v -> finishLessonStep(2));
        easy.setOnClickListener(v -> finishLessonStep(3));
        weighted(row2, good, 1, 9); weighted(row2, easy, 1, 0); add(content, row2);
    }

    private void finishLessonStep(int rating) {
        Exercise e = lessonSteps.get(lessonPosition);
        progress.record(e.word, rating);
        progress.recordSkill(e.word, e.kind == Exercise.Kind.SHADOW ? "production" : "reading", rating >= 2);
        lessonAttempts++;
        if (rating >= 2) lessonCorrect++;
        if (rating <= 1) appendRetry(e);
        advanceLesson();
    }

    private void gradeObjective(Exercise exercise, boolean correct, String selection) {
        if (lessonAnswered) return;
        lessonAnswered = true;
        lessonWasCorrect = correct;
        lessonAnswerText = selection;
        lessonChoice = selection;
        progress.record(exercise.word, correct ? 2 : 0);
        progress.recordSkill(exercise.word, exercise.kind==Exercise.Kind.TYPE ? "production" :
                exercise.kind==Exercise.Kind.LISTEN && canHear(exercise.word) ? "listening" : "reading", correct);
        String skill = exercise.kind == Exercise.Kind.TYPE ? "typing" : exercise.kind == Exercise.Kind.CLOZE ? "context" :
                       exercise.kind == Exercise.Kind.LISTEN && canHear(exercise.word) ? "listening" : "reading";
        progress.recordPractice(skill, correct);
        lessonAttempts++;
        if (correct) lessonCorrect++;
        else appendRetry(exercise);
        render();
    }

    private void appendRetry(Exercise e) {
        if (!e.isRetry && retryPlanned.add(e.word.id) && lessonSteps.size() < 28) {
            lessonSteps.add(new Exercise(Exercise.Kind.RECALL, e.word, true));
        }
    }

    private void nextLessonButton() {
        Button next = button("Continue  →", true, false);
        next.setOnClickListener(v -> advanceLesson());
        add(content, next);
    }

    private void advanceLesson() {
        lessonPosition++;
        lessonRevealed = false;
        lessonAnswered = false;
        lessonAudioPlayed = false;
        lessonWasCorrect = false;
        lessonAnswerText = "";
        lessonChoice = "";
        if (lessonPosition >= lessonSteps.size()) {
            progress.finishSession();
            page = "done";
        }
        render();
    }

    private void lessonFinished() {
        LinearLayout celebration = card(SUCCESS_BG);
        add(celebration, text("SESSION COMPLETE", 13, GREEN, true));
        gap(celebration, 12);
        add(celebration, text("Session complete.", 27, INK, true));
        gap(celebration, 8);
        add(celebration, text("You practised retrieving Chinese across several formats. Come back on a later day to check retention.", 15, INK, false));
        gap(celebration, 18);
        add(celebration, text(lessonCorrect + " / " + Math.max(1, lessonAttempts) + " successful recalls", 26, PRIMARY, true));
        add(celebration, text("Mistakes are part of learning; reviewed words will return automatically.", 12, MUTED, false));
        add(content, celebration);
        LinearLayout next = card(WHITE);
        add(next, text("A good stopping point", 21, INK, true));
        gap(next, 8);
        add(next, text("Rest helps consolidate memory. You can stop here or choose another short session.", 14, MUTED, false));
        gap(next, 12);
        Button home = button("Finish for now  ✓", true, false);
        home.setOnClickListener(v -> { lessonSteps.clear(); page = "home"; render(); });
        add(next, home);
        gap(next, 8);
        Button more = button("One more mission (optional)", false, true);
        more.setOnClickListener(v -> { lessonSteps.clear(); beginLesson(); }); add(next, more);
        add(content, next);
    }

    private void toneLab() {
        section(content,"Tone listening lab","Audio-first choices using similar-sounding HSK 1–2 characters.");
        if (!canHearEntireDeck()) {
            add(content,text("A complete offline word-audio pack or Mandarin Android voice is required for tone listening.",15,MUTED,false));
            return;
        }
        if(tonePairs.isEmpty()){
            tonePairs.addAll(ToneTrainer.pairs(deck()));
            Collections.shuffle(tonePairs,random);
            toneIndex=0;toneScore=0;toneAnswered=false;toneHeard=false;toneChoice=-1;
        }
        if(tonePairs.isEmpty()){
            add(content,text("No tone contrast pairs are available for this HSK level.",15,MUTED,false));return;
        }
        int total=Math.min(10,tonePairs.size());
        if(toneIndex>=total){
            LinearLayout done=card(SUCCESS_BG);
            add(done,text("TONE SESSION COMPLETE",12,PRIMARY,true));
            gap(done,10);
            add(done,text(toneScore+" / "+total+" contrasts identified",23,INK,true));
            gap(done,8);
            add(done,text("This tests tone perception using offline Azure voices when installed, otherwise device TTS; not your own pronunciation.",13,MUTED,false));
            Button repeat=button("Start another tone session",true,false);
            repeat.setOnClickListener(v->{tonePairs.clear();render();});add(done,repeat);add(content,done);return;
        }
        Word[] pair=tonePairs.get(toneIndex);
        Word target=pair[(toneIndex+1)%2];
        LinearLayout prompt=card(PALE);
        add(prompt,text("CONTRAST "+(toneIndex+1)+" / "+total,12,PRIMARY,true));
        gap(prompt,12);
        add(prompt,text("Which tone did you hear?",22,INK,true));
        gap(prompt,12);
        Button play=button("▶ Play Mandarin word",true,false);
        play.setOnClickListener(v->{toneHeard=true;speak(target);render();});add(prompt,play);
        gap(prompt,6);
        add(prompt,text("Listen first. The characters and meanings are revealed after answering.",12,MUTED,false));
        add(content,prompt);
        for(int i=0;i<2;i++){
            final int choice=i;
            Word candidate=pair[i];
            LinearLayout tile=card(toneAnswered && candidate==target ? SUCCESS_BG : WHITE);
            add(tile,text(candidate.pinyin,22,INK,true));
            if(toneAnswered) {gap(tile,5);add(tile,text(candidate.hanzi+" — "+candidate.english,14,MUTED,false));}
            tile.setOnClickListener(v->{
                if(!toneHeard){showToast("Play the sound before answering.");return;}
                if(toneAnswered)return;
                toneChoice=choice;toneAnswered=true;
                boolean ok=candidate==target;
                if(ok)toneScore++;
                progress.recordPractice("tone",ok);
                render();
            });
            add(content,tile);
        }
        if(toneAnswered){
            LinearLayout result=card(WHITE);
            add(result,text(toneChoice==(toneIndex+1)%2?"✓ Tone recognized":"Try hearing the tonal contrast again",16,INK,true));
            gap(result,6);
            add(result,text("Correct: "+target.hanzi+" · "+target.pinyin+" · "+target.english,15,PRIMARY,false));
            Button again=button("▶ Listen to correct word",false,true);again.setOnClickListener(v->speak(target));add(result,again);
            gap(result,9);
            Button next=button("Next contrast  →",true,false);
            next.setOnClickListener(v->{toneIndex++;toneHeard=false;toneAnswered=false;toneChoice=-1;render();});
            add(result,next);add(content,result);
        }
    }

    private static final int NOTIFICATION_PERMISSION=442;
    private void enableReminders() {
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFICATION_PERMISSION);
            return;
        }
        ReviewReminders.configure(this,true,ReviewReminders.hour(this));
        render();
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==NOTIFICATION_PERMISSION){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                ReviewReminders.configure(this,true,ReviewReminders.hour(this));
            else showToast("Permission denied. You can enable notifications later in Android Settings.");
            render();
        }
    }

    /** Android document picker: user chooses the backup location, no storage permission. */
    private void exportProgress() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_TITLE, "HSK_Companion_progress_backup.json");
        startActivityForResult(intent, BACKUP_EXPORT_REQUEST);
    }

    private void importProgress() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, BACKUP_IMPORT_REQUEST);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            if (requestCode == BACKUP_EXPORT_REQUEST) {
                byte[] bytes = ProgressBackup.exportJson(progress.rawPreferences()).getBytes(StandardCharsets.UTF_8);
                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    if (out == null) throw new IllegalStateException("Unable to open destination file");
                    out.write(bytes);
                }
                showToast("Progress backup saved successfully.");
            } else if (requestCode == BACKUP_IMPORT_REQUEST) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                try (InputStream in = getContentResolver().openInputStream(uri)) {
                    if (in == null) throw new IllegalStateException("Unable to open backup file");
                    byte[] part = new byte[8192];
                    int count;
                    while ((count = in.read(part)) != -1) {
                        if (buffer.size() + count > ProgressBackup.MAX_BYTES)
                            throw new IllegalArgumentException("Backup exceeds the 2 MB size limit");
                        buffer.write(part, 0, count);
                    }
                }
                pendingRestore = ProgressBackup.parseJson(new String(buffer.toByteArray(), StandardCharsets.UTF_8));
                new AlertDialog.Builder(this)
                    .setTitle("Replace local learning history?")
                    .setMessage("This backup contains " + pendingRestore.size() + " settings and progress records. It will replace the current local data. Export your current progress first if you want to keep a copy.")
                    .setNegativeButton("Cancel", (dialog, which) -> pendingRestore = null)
                    .setPositiveButton("Restore", (dialog, which) -> {
                        if (pendingRestore == null) return;
                        try {
                            ProgressBackup.restore(progress.rawPreferences(), pendingRestore);
                            pendingRestore = null;
                            // Restore selected deck and refresh reminders without resetting app identity.
                            version = "2.0";
                            level = progress.level();
                            if (repository.count(version, level) == 0) level = 1;
                            ReviewReminders.schedule(this);
                            currentCard = null;
                            lessonSteps.clear();
                            page = "home";
                            showToast("Learning progress restored.");
                            recreate();
                        } catch (RuntimeException problem) {
                            showToast("Restore failed: " + problem.getMessage());
                        }
                    }).show();
            }
        } catch (Exception problem) {
            pendingRestore = null;
            showToast("Backup error: " + problem.getMessage());
        }
    }

    private void settings() {
        section(content, "Settings", "A comfortable place to learn, day or night.");
        LinearLayout appearance = card(WHITE);
        add(appearance, text("Appearance", 18, INK, true));
        gap(appearance, 5);
        add(appearance, text("Use your phone's theme automatically, or choose a comfortable reading mode.", 13, MUTED, false));
        gap(appearance, 12);
        String themeChoice = getSharedPreferences("hsk_progress_v1", MODE_PRIVATE)
            .getString("ui_theme_mode", "system");
        LinearLayout themeChoices = horizontal();
        for (String value : new String[]{"system", "light", "dark"}) {
            final String selected = value;
            String label = value.equals("system") ? "System" : value.equals("light") ? "Light" : "Dark";
            Button option = button(label, value.equals(themeChoice), true);
            option.setContentDescription(label + " theme" + (value.equals(themeChoice) ? ", selected" : ""));
            option.setOnClickListener(v -> changeTheme(selected));
            weighted(themeChoices, option, 1, value.equals("dark") ? 0 : 6);
        }
        add(appearance, themeChoices);
        add(content, appearance);
        LinearLayout pace = card(WHITE);
        add(pace, text("Daily mission length", 18, INK, true));
        gap(pace, 6);
        add(pace, text("Short sessions are easier to repeat. Both lengths mix new words and reviews.", 13, MUTED, false));
        gap(pace, 12);
        LinearLayout choices = horizontal();
        Button shortLesson = button("Quick · ~4 min", !progress.deep(), true);
        Button deepLesson = button("Deep · ~8 min", progress.deep(), true);
        shortLesson.setOnClickListener(v -> { progress.setDeep(false); render(); });
        deepLesson.setOnClickListener(v -> { progress.setDeep(true); render(); });
        weighted(choices, shortLesson, 1, 8); weighted(choices, deepLesson, 1, 0);
        add(pace, choices); add(content, pace);
        LinearLayout choose = card(WHITE);
        add(choose, text("HSK learning track", 18, INK, true));
        gap(choose, 8);
        add(choose, text("Study the classic HSK 1 or HSK 2 vocabulary track. Changing level keeps your saved progress.", 13, MUTED, false));
        gap(choose, 10);
        LinearLayout levels=horizontal();
        Button hsk1=button("HSK 1",level==1,true);
        Button hsk2=button("HSK 2",level==2,true);
        hsk1.setOnClickListener(v->changeDeck("2.0",1));
        hsk2.setOnClickListener(v->changeDeck("2.0",2));
        weighted(levels,hsk1,1,8);weighted(levels,hsk2,1,0);
        add(choose,levels);
        gap(choose,9);
        add(choose, text("Vocabulary: " + repository.count("2.0", level) + " words at this level.", 13, PRIMARY, true));
        add(content, choose);
        LinearLayout reminders=card(WHITE);
        add(reminders,text("Gentle review reminders",18,INK,true));
        gap(reminders,7);
        add(reminders,text("Off by default. One optional reminder per day, only when you have overdue words. Never during late-night hours; Android may deliver it later to save battery.",13,MUTED,false));
        gap(reminders,10);
        boolean enabled=ReviewReminders.enabled(this);
        Button toggle=button(enabled?"✓ Daily reminders enabled":"Enable daily reminders",enabled,true);
        toggle.setOnClickListener(v->{ if(ReviewReminders.enabled(this)){ReviewReminders.configure(this,false,ReviewReminders.hour(this));render();}
                                    else enableReminders(); });
        add(reminders,toggle);
        if(enabled){
            gap(reminders,9);
            add(reminders,text("Preferred time (local)",13,MUTED,true));
            gap(reminders,6);
            LinearLayout hours=horizontal();
            for(int h:new int[]{18,20,21}){
                final int chosen=h;
                Button time=button(String.format(java.util.Locale.ROOT,"%02d:00",h),ReviewReminders.hour(this)==h,true);
                time.setOnClickListener(v->{ReviewReminders.configure(this,true,chosen);render();});
                weighted(hours,time,1,5);
            }
            add(reminders,hours);
            gap(reminders,6);
            add(reminders,text("Current words due: "+progress.dueCount(deck())+". No notification is sent if nothing is due.",12,MUTED,false));
        }
        add(content,reminders);
        LinearLayout toneSettings=card(WHITE);
        add(toneSettings,text("Pinyin tone grading",18,INK,true));
        gap(toneSettings,7);
        add(toneSettings,text("Beginner mode ignores tone marks while you learn syllables. Strict mode requires tones in typed pinyin; listening tone MCQs are available at any time.",13,MUTED,false));
        gap(toneSettings,10);
        Button strict=button(progress.strictTones()?"✓ Require correct tones":"Enable strict tone typing",progress.strictTones(),true);
        strict.setOnClickListener(v->{progress.setStrictTones(!progress.strictTones());render();});
        add(toneSettings,strict);add(content,toneSettings);
        LinearLayout bundled=card(WHITE);
        add(bundled,text("Offline Azure voices",18,INK,true));
        gap(bundled,6);
        add(bundled,text(hasBundledAudio() ?
            "Pre-generated female (A) and male (B) Mandarin previews are installed. Packaged recordings play without internet or an Azure account. Synthetic speech; verify pronunciation before public release." :
            "No Azure audio pack installed yet. Generate recordings with prepare_azure_audio.sh on Debian, then rebuild. Android speech remains the fallback.",13,MUTED,false));
        gap(bundled,10);
        Button azureA=button("▶ Preview female A (Azure if bundled)",false,true);
        azureA.setOnClickListener(v->speakSpeakerPreview("a"));add(bundled,azureA);
        gap(bundled,6);
        Button azureB=button("▶ Preview male B (Azure if bundled)",false,true);
        azureB.setOnClickListener(v->speakSpeakerPreview("b"));add(bundled,azureB);
        add(content,bundled);
        LinearLayout audio = card(WHITE);
        add(audio, text("Android voice fallback", 18, INK, true));
        gap(audio, 7);
        add(audio, text(ttsStatus, 13, ttsReady ? GREEN : MUTED, false));
        gap(audio, 7);
        add(audio, text("Android TTS engine: " + ttsEngine, 12, MUTED, false));
        gap(audio,12);
        add(audio,text("Dialogue speaker voices",16,INK,true));
        gap(audio,5);
        add(audio,text("Used only for missing offline recordings. Installed Android voices may be limited to one; the separate Azure voices are fixed, pre-generated recordings.",12,MUTED,false));
        for(String speaker:new String[]{"a","b"}){
            gap(audio,8);
            Button voicePick=button("Speaker " + speaker.toUpperCase(Locale.ROOT) + ": " + voiceLabel(speaker),false,true);
            voicePick.setOnClickListener(v->cycleSpeakerVoice(speaker));
            add(audio,voicePick);
            Button preview=button("▶ Preview speaker " + speaker.toUpperCase(Locale.ROOT),false,true);
            preview.setOnClickListener(v->speakSpeakerPreview(speaker));
            add(audio,preview);
        }
        if(mandarinVoices.size()<2 && !hasBundledAudio()){
            gap(audio,5);
            add(audio,text("Only one Mandarin voice detected: install another Mandarin voice in Android's TTS settings for a true two-voice conversation. We do not alter pitch because that could distort Mandarin tones.",12,MUTED,false));
        }
        gap(audio, 11);
        Button tryVoice = button("▶ Test audio (你好)", false, true);
        tryVoice.setOnClickListener(v -> speak(new Word("2.0", 1, "你好", "nǐ hǎo", "hello")));
        add(audio, tryVoice);
        gap(audio, 8);
        Button retryVoice = button("↻ Retry voice detection", false, true);
        retryVoice.setOnClickListener(v -> {
            ttsReady = false;
            ttsStatus = "Checking Android speech engine…";
            ttsEngine = "unknown";
            if(dialoguePlayer!=null)dialoguePlayer.cancel();
            dialoguePlayer=null;
            mandarinVoices.clear();
            defaultMandarinVoice=null;
            if (tts != null) { tts.stop(); tts.shutdown(); }
            tts = new TextToSpeech(this, this);
            render();
        });
        add(audio, retryVoice);
        gap(audio, 8);
        add(audio, text("On OnePlus 13: Settings → Accessibility → Vision → Text-to-speech settings. Select Google or another Mandarin-capable engine, install a Chinese (China) voice, then return and retry. Menu names vary by OxygenOS/ColorOS version.", 12, MUTED, false));
        add(content, audio);
        LinearLayout safe = card(WHITE);
        add(safe, text("Your learning data", 18, INK, true));
        gap(safe, 7);
        add(safe, text("Your vocabulary reviews and scores stay on this device. Export a portable JSON backup before changing phones or reinstalling. Files are unencrypted; keep them private.", 13, MUTED, false));
        gap(safe, 11);
        Button export = button("Export progress backup", true, false);
        export.setOnClickListener(v -> exportProgress());
        add(safe, export);
        gap(safe, 7);
        Button restore = button("Restore from backup…", false, true);
        restore.setOnClickListener(v -> importProgress());
        add(safe, restore);
        add(content, safe);
        // Donations are entirely optional, handled outside the app by the user's
        // browser. No funding link is shown unless the publisher configures it.
        String donationLink = DonationLinks.configuredUrl(this);
        if (!donationLink.isEmpty()) {
            LinearLayout support = card(WHITE);
            add(support, text("Support HSK Companion", 18, INK, true));
            gap(support, 7);
            add(support, text("Every lesson and feature is free. If this project helps you learn, you may choose to support its development. No contribution is required or rewarded with special access.", 13, MUTED, false));
            gap(support, 11);
            Button donate = button("❤ Optional donation (opens browser)", false, true);
            donate.setContentDescription("Optional donation, opens external website in browser");
            donate.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(donationLink)));
                } catch (RuntimeException problem) {
                    showToast("No browser could open the donation link.");
                }
            });
            add(support, donate);
            add(content, support);
        }
        LinearLayout info = card(WHITE);
        add(info, text("About and attribution", 18, INK, true));
        gap(info, 8);
        add(info, text("Unofficial app; not affiliated with HSK test authorities. Vocabulary list data: NihaoLearn / Complete HSK Vocabulary (MIT; see nihaolearn.com/licenses and github.com/drkameleon/complete-hsk-vocabulary). English glosses adapted from CC-CEDICT contributors, CC BY-SA 4.0 (mdbg.net/chinese/dictionary?page=cc-cedict). Changes: TSV records restructured as JSON, POS omitted, glosses may be simplified. Full license and attribution details are in NOTICE_DATA.md in the source ZIP. Grammar lessons are original app content.", 13, MUTED, false));
        add(content, info);
    }
}
