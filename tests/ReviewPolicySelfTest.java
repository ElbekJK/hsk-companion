import com.example.hskcompanion.ReviewFocus;
import com.example.hskcompanion.ReviewPolicy;
import com.example.hskcompanion.ReviewScheduler;

public class ReviewPolicySelfTest {
    public static void main(String[] args) {
        long day=20400, now=1234567890L;
        assert ReviewPolicy.stageAfter(0,2,-1,day)==1;
        assert ReviewPolicy.stageAfter(1,3,day,day)==1 : "No two promotions same day";
        assert ReviewPolicy.stageAfter(1,2,day,day+1)==2 : "Next-day retrieval promotes";
        assert ReviewPolicy.stageAfter(5,0,day,day)==0 : "A failure resets even on promotion day";
        assert ReviewPolicy.nextDue(now,1,2,1) == now+ReviewScheduler.DAY;
        assert ReviewFocus.freshWordLimit(false,0)==2;
        assert ReviewFocus.freshWordLimit(true,0)==4;
        assert ReviewFocus.freshWordLimit(true,4)==1;
        assert ReviewFocus.freshWordLimit(false,8)==0;
        assert ReviewFocus.priority(0,3,now-ReviewScheduler.DAY,now) >
               ReviewFocus.priority(2,0,now-ReviewScheduler.DAY,now);
        System.out.println("ReviewPolicySelfTest passed");
    }
}
