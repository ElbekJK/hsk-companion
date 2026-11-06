package android.content.res;
public class AssetFileDescriptor implements java.io.Closeable {
 public long getLength(){return 1;}
 public long getStartOffset(){return 0;}
 public java.io.FileDescriptor getFileDescriptor(){return new java.io.FileDescriptor();}
 public void close(){}
}
