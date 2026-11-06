package com.example.hskcompanion;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class ProgressStore {
    private final SharedPreferences prefs;
    public ProgressStore(Context context) {
        // Keep original v0.1 preferences so an update retains old reviews/saved words.
        prefs = context.getSharedPreferences("hsk_progress_v1", Context.MODE_PRIVATE);
    }
    SharedPreferences rawPreferences() { return prefs; }
    public int stage(Word w) { return prefs.getInt("stage_" + w.id, 0); }
    public boolean saved(Word w) { return prefs.getBoolean("save_" + w.id, false); }
    public boolean seen(Word w) { return prefs.getBoolean("seen_" + w.id, false) || stage(w) > 0 || prefs.contains("due_" + w.id); }
    public long dueTime(Word w) {
        String key = "due_" + w.id;
        if (!prefs.contains(key)) return 0;
        long old = prefs.getLong(key, 0);
        // v0.1 stored UTC epoch DAY count, not milliseconds.
        return old > 0 && old < 100_000_000L ? TimeUnit.DAYS.toMillis(old) : old;
    }
    public boolean due(Word w) {
        if (!seen(w)) return false;
        long now = System.currentTimeMillis();
        return dueTime(w) <= now || skillDue(w, "listening", now) || skillDue(w, "production", now);
    }
    private static String safeSkill(String skill) {
        if (!"reading".equals(skill) && !"listening".equals(skill) && !"production".equals(skill))
            throw new IllegalArgumentException("Unknown skill: " + skill);
        return skill;
    }
    public int skillStage(Word w, String skill) {
        skill = safeSkill(skill);
        String key = "skill_stage_" + skill + "_" + w.id;
        // Existing v0.6 word knowledge maps only to reading, NEVER assumed to be listening/production.
        return prefs.contains(key) ? prefs.getInt(key, 0) : (skill.equals("reading") ? stage(w) : 0);
    }
    public boolean skillDue(Word w, String skill, long now) {
        skill = safeSkill(skill);
        if (skill.equals("reading") && !prefs.contains("skill_due_reading_" + w.id))
            return dueTime(w) > 0 && dueTime(w) <= now;
        String key = "skill_due_" + skill + "_" + w.id;
        return prefs.contains(key) && prefs.getLong(key, Long.MAX_VALUE) <= now;
    }
    public int dueForSkill(java.util.List<Word> words, String skill) {
        long now = System.currentTimeMillis(); int count = 0;
        for (Word w:words) if (seen(w) && skillDue(w,skill,now)) count++;
        return count;
    }
    public void recordSkill(Word w, String skill, boolean correct) {
        skill = safeSkill(skill);
        long now = System.currentTimeMillis(), day = localDay(now);
        String suffix = skill + "_" + w.id;
        String dayKey = "skill_promo_day_" + suffix;
        int before = skillStage(w,skill);
        int after = ReviewPolicy.stageAfter(before, correct ? 2 : 0,
                prefs.getLong(dayKey,Long.MIN_VALUE), day);
        SharedPreferences.Editor edit=prefs.edit()
                .putInt("skill_stage_"+suffix,after)
                .putLong("skill_due_"+suffix,ReviewPolicy.nextDue(now,before,correct?2:0,after));
        if (correct && after > before) edit.putLong(dayKey,day);
        String lastKey="skill_last_attempt_"+suffix;
        long last=prefs.getLong(lastKey,0);
        if(last>0 && now-last>=TimeUnit.DAYS.toMillis(7)) {
            edit.putInt("delayed_attempts",delayedAttempts()+1)
                .putInt("delayed_correct",delayedCorrect()+(correct?1:0));
        }
        edit.putLong(lastKey,now).apply();
    }
    public int delayedAttempts(){return prefs.getInt("delayed_attempts",0);}
    public int delayedCorrect(){return prefs.getInt("delayed_correct",0);}

    public void toggleSaved(Word w) { prefs.edit().putBoolean("save_" + w.id, !saved(w)).apply(); }
    public void markIntroduced(Word w) {
        if (!seen(w)) prefs.edit().putBoolean("seen_" + w.id, true)
                .putLong("due_" + w.id, System.currentTimeMillis() + ReviewScheduler.interval(0, 0)).apply();
    }
    public int pastMisses(Word w) { return prefs.getInt("misses_" + w.id, 0); }
    public void record(Word w, int rating) {
        int before = stage(w);
        long now = System.currentTimeMillis();
        long today = localDay(now);
        String promoKey = "promo_day_" + w.id;
        long lastPromotion = prefs.getLong(promoKey, Long.MIN_VALUE);
        int after = ReviewPolicy.stageAfter(before, rating, lastPromotion, today);
        SharedPreferences.Editor edit = prefs.edit()
                .putBoolean("seen_" + w.id, true)
                .putInt("stage_" + w.id, after)
                .putLong("due_" + w.id, ReviewPolicy.nextDue(now, before, rating, after))
                .putInt("reviews", reviews() + 1);
        if (rating <= 1) edit.putInt("misses_" + w.id, pastMisses(w) + 1);
        if (rating >= 2 && after > before) edit.putLong(promoKey, today);
        edit.apply();
    }
    public void recordPractice(String skill, boolean correct) {
        if (!skill.equals("reading") && !skill.equals("listening") && !skill.equals("typing") && !skill.equals("context") && !skill.equals("tone")) return;
        long day = localDay(System.currentTimeMillis());
        prefs.edit().putInt("skill_attempt_" + skill, skillAttempts(skill)+1)
                .putInt("skill_correct_" + skill, skillCorrect(skill)+(correct ? 1 : 0))
                .putInt("practice_day_" + day, prefs.getInt("practice_day_"+day,0)+1)
                .putInt("correct_day_" + day, prefs.getInt("correct_day_"+day,0)+(correct?1:0)).apply();
    }
    public int skillAttempts(String skill){return prefs.getInt("skill_attempt_"+skill,0);}
    public int skillCorrect(String skill){return prefs.getInt("skill_correct_"+skill,0);}
    public int dayAttempts(int daysAgo){return prefs.getInt("practice_day_"+dayOffset(daysAgo),0);}
    public int dayCorrect(int daysAgo){return prefs.getInt("correct_day_"+dayOffset(daysAgo),0);}
    private long dayOffset(int daysAgo){ Calendar c=Calendar.getInstance();c.add(Calendar.DAY_OF_YEAR,-daysAgo);return localDay(c.getTimeInMillis()); }
    public int dialogueAttempts(String id){return prefs.getInt("dialogue_n_"+id,0);}
    public int dialogueCorrect(String id){return prefs.getInt("dialogue_ok_"+id,0);}
    public void recordDialogue(String id,boolean correct){
        prefs.edit().putInt("dialogue_n_"+id,dialogueAttempts(id)+1)
          .putInt("dialogue_ok_"+id,dialogueCorrect(id)+(correct?1:0)).apply();
    }
    public boolean grammarDone(String id){return prefs.getBoolean("grammar_done_"+id,false);}
    public void markGrammarDone(String id){prefs.edit().putBoolean("grammar_done_"+id,true).apply();}
    public void recordExam(int examLevel,int correct,int count,int listeningCorrect,int listeningCount){
        String prefix="mock_"+examLevel+"_";
        int pct = count == 0 ? 0 : (correct*100/count);
        prefs.edit().putInt(prefix+"count",prefs.getInt(prefix+"count",0)+1)
                .putInt(prefix+"last_pct",pct)
                .putInt(prefix+"best_pct",Math.max(pct,prefs.getInt(prefix+"best_pct",0)))
                .putInt(prefix+"last_listening", listeningCorrect)
                .putInt(prefix+"last_listening_total", listeningCount)
                .putInt(prefix+"last_reading",correct-listeningCorrect)
                .putInt(prefix+"last_reading_total",count-listeningCount)
                .apply();
    }
    public int examCount(int lvl){return prefs.getInt("mock_"+lvl+"_count",0);}
    public int examLast(int lvl){return prefs.getInt("mock_"+lvl+"_last_pct",0);}
    public int examBest(int lvl){return prefs.getInt("mock_"+lvl+"_best_pct",0);}
    public int learned(List<Word> words) { int n=0; for (Word w : words) if (seen(w)) n++; return n; }
    public int strong(List<Word> words) { int n=0; for (Word w : words) if (stage(w) >= 3) n++; return n; }
    public int dueCount(List<Word> words) { int n=0; for (Word w : words) if (due(w)) n++; return n; }
    public int reviews() { return prefs.getInt("reviews", 0); }
    public int quizzes() { return prefs.getInt("quizzes", 0); }
    public void finishQuiz() { prefs.edit().putInt("quizzes", quizzes() + 1).apply(); }
    public int best() { return prefs.getInt("best", 0); }
    public void recordScore(int score) { if (score > best()) prefs.edit().putInt("best", score).apply(); }
    public String version() { return prefs.getString("version", "2.0"); }
    public void setVersion(String version) { prefs.edit().putString("version", version).apply(); }
    public int level() { return prefs.getInt("level", 1); }
    public void setLevel(int level) { prefs.edit().putInt("level", level).apply(); }
    public String dialogueVoiceName(String speaker){
        if (!"a".equals(speaker) && !"b".equals(speaker)) return "";
        return prefs.getString("dialogue_voice_" + speaker, "");
    }
    public void setDialogueVoiceName(String speaker,String name){
        if (!"a".equals(speaker) && !"b".equals(speaker)) return;
        prefs.edit().putString("dialogue_voice_" + speaker, name == null ? "" : name).apply();
    }
    public boolean strictTones(){return prefs.getBoolean("strict_tones",false);}
    public void setStrictTones(boolean value){prefs.edit().putBoolean("strict_tones",value).apply();}
    public boolean deep() { return prefs.getBoolean("deep_lessons", false); }
    public void setDeep(boolean deep) { prefs.edit().putBoolean("deep_lessons", deep).apply(); }
    public int sessions() { return prefs.getInt("sessions_total", 0); }
    private long localDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return TimeUnit.MILLISECONDS.toDays(cal.getTimeInMillis());
    }
    public void finishSession() {
        long day = localDay(System.currentTimeMillis());
        prefs.edit().putInt("sessions_total", sessions() + 1)
                .putBoolean("day_" + day, true).apply();
    }
    public int daysInLastSeven() {
        long now = System.currentTimeMillis();
        int n = 0;
        for (int i=0; i<7; i++) {
            Calendar c = Calendar.getInstance(); c.setTimeInMillis(now); c.add(Calendar.DAY_OF_YEAR, -i);
            if (prefs.getBoolean("day_" + localDay(c.getTimeInMillis()), false)) n++;
        }
        return n;
    }
}
