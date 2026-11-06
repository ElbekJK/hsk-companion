import com.example.hskcompanion.QuizEngine;
import com.example.hskcompanion.Word;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class QuizEngineSelfTest {
    public static void main(String[] args) {
        List<Word> deck = new ArrayList<>();
        for (int i = 0; i < 50; i++) deck.add(new Word("2.0", 1, "字" + i, "zì", "word" + i));
        assert QuizEngine.questions(deck, 10, new Random(12)).size() == 10;
        assert QuizEngine.questions(deck, 100, new Random(12)).size() == 50;
        for (Word word : deck) {
            List<Word> options = QuizEngine.choices(word, deck, new Random(99));
            assert options.size() == 4;
            assert options.contains(word);
            assert new HashSet<>(options).size() == 4;
        }
        System.out.println("QuizEngineSelfTest PASS: all questions and answer choices are valid.");
    }
}
