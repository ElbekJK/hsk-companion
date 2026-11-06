import com.example.hskcompanion.*;
import java.util.*;
public class ToneTrainerSelfTest {
  public static void main(String[] args) {
    assert PinyinCheck.toneMatches("ma3","mǎ");
    assert PinyinCheck.toneMatches("nǐ hǎo","ni3 hao3");
    assert !PinyinCheck.toneMatches("ma4","mǎ");
    assert !PinyinCheck.toneMatches("ma","mǎ");
    List<Word> vocab=Arrays.asList(new Word("2.0",2,"买","mǎi","buy"),new Word("2.0",2,"卖","mài","sell"));
    assert ToneTrainer.pairs(vocab).size()==1;
    System.out.println("ToneTrainerSelfTest PASS");
  }
}
