import com.example.hskcompanion.Exercise;
import com.example.hskcompanion.PinyinCheck;
import com.example.hskcompanion.ReviewScheduler;
import com.example.hskcompanion.SessionPlanner;
import com.example.hskcompanion.Word;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PedagogySelfTest {
    public static void main(String[] args) {
        assert ReviewScheduler.nextStage(0,0) == 0;
        assert ReviewScheduler.nextStage(0,2) == 1;
        assert ReviewScheduler.nextStage(4,3) == 6;
        assert ReviewScheduler.nextStage(6,2) == 6;
        assert ReviewScheduler.interval(0,0) == 600_000L;
        assert ReviewScheduler.interval(0,2) == ReviewScheduler.DAY;
        assert ReviewScheduler.interval(1,2) == 3 * ReviewScheduler.DAY;
        assert ReviewScheduler.dueAt(10,0,0) == 600_010;
        assert PinyinCheck.matches("ni hao", "nǐ hǎo", "你好");
        assert PinyinCheck.matches("NǏHĂO", "nǐ hǎo", "你好");
        assert PinyinCheck.matches("ni3 hao3", "nǐ hǎo", "你好");
        assert PinyinCheck.matches("lv", "lǜ", "绿");
        assert !PinyinCheck.matches("lu", "lǜ", "绿");
        assert !PinyinCheck.matches("", "hǎo", "好");
        assert PinyinCheck.matches("ma", "mā", "妈"); // tone-insensitive is documented
        List<Word> due = new ArrayList<>();
        List<Word> unseen = new ArrayList<>();
        List<Word> familiar = new ArrayList<>();
        for(int i=0;i<10;i++) {
            due.add(new Word("2.0",1,"我"+i,"wo","I"));
            unseen.add(new Word("2.0",1,"你"+i,"ni","you"));
            familiar.add(new Word("2.0",1,"他"+i,"ta","he"));
        }
        List<Exercise> plan = SessionPlanner.plan(due, unseen, familiar, false, new Random(8));
        int introductions=0;
        for(int i=0;i<plan.size();i++) {
            Exercise e=plan.get(i);
            if(e.kind==Exercise.Kind.INTRO) introductions++;
            if(e.word.hanzi.startsWith("你") && e.kind!=Exercise.Kind.INTRO) {
                boolean introduced=false;
                for (int j=0;j<i;j++) if(plan.get(j).kind==Exercise.Kind.INTRO && plan.get(j).word.id.equals(e.word.id)) introduced=true;
                assert introduced : "New item tested before introduction";
            }
        }
        assert introductions==0 : "Backlogged learners should consolidate before adding new words";
        List<Exercise> freshPlan=SessionPlanner.plan(new ArrayList<>(),unseen,familiar,false,new Random(8));
        int freshIntroductions=0;
        for(Exercise e:freshPlan)if(e.kind==Exercise.Kind.INTRO)freshIntroductions++;
        assert freshIntroductions==2 : "Quick sessions should still introduce two items when reviews are clear";
        assert plan.size() <= 15;
        assert SessionPlanner.plan(new ArrayList<>(),new ArrayList<>(),familiar,false,new Random(1)).size()>0;
        assert SessionPlanner.plan(new ArrayList<>(),new ArrayList<>(),new ArrayList<>(),false,new Random(1)).isEmpty();
        System.out.println("PedagogySelfTest passed");
    }
}
