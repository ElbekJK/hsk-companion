import com.example.hskcompanion.OfflineAudioPlayer;
public class OfflineAudioKeySelfTest {
 public static void main(String[] args) {
    assert "audio/a/2dad5bf8bb28acd5eaff0bdd.mp3".equals(OfflineAudioPlayer.filename("a","你好，很高兴认识你。"));
    assert !OfflineAudioPlayer.filename("a","你好").equals(OfflineAudioPlayer.filename("b","你好"));
    assert OfflineAudioPlayer.filename("a"," 你好 ").equals(OfflineAudioPlayer.filename("a","你好"));
    System.out.println("OfflineAudioKeySelfTest PASS: stable text/voice keys");
 }
}
