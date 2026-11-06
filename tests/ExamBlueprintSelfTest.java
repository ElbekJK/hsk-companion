import com.example.hskcompanion.*;
import java.util.*;
public final class ExamBlueprintSelfTest {
    public static void main(String[] args){
        for(int level=1;level<=2;level++){
            int count=ExamBlueprint.total(level);
            if(count != (level==1?40:60))throw new AssertionError("Exam item count");
            if(ExamBlueprint.listeningCount(level)+ExamBlueprint.readingCount(level)!=count)throw new AssertionError("Section counts");
            List<Word> words=new ArrayList<>();
            for(int i=0;i<150;i++)words.add(new Word("2.0",level,"词"+i,"ci"+i,"meaning"+i));
            List<Word> q=ExamBlueprint.questions(words,level,new Random(42));
            if(q.size()!=count || new HashSet<>(q).size()!=count)throw new AssertionError("Duplicate exam question");
        }
        System.out.println("ExamBlueprintSelfTest passed");
    }
}
