package android.media;
public class MediaPlayer {
 public interface OnPreparedListener {void onPrepared(MediaPlayer mp);}
 public interface OnCompletionListener {void onCompletion(MediaPlayer mp);}
 public interface OnErrorListener {boolean onError(MediaPlayer mp,int what,int extra);}
 public void setDataSource(java.io.FileDescriptor fd,long offset,long length) throws java.io.IOException {}
 public void setOnPreparedListener(OnPreparedListener l){}
 public void setOnCompletionListener(OnCompletionListener l){}
 public void setOnErrorListener(OnErrorListener l){}
 public void prepareAsync(){}
 public void start(){}
 public void stop(){}
 public void release(){}
}
