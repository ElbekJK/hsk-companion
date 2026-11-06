package android.app;
public class AlertDialog { public static class Builder {
  public Builder(Activity a){} public Builder setTitle(String s){return this;}
  public Builder setMessage(String s){return this;}
  public Builder setNegativeButton(String s,android.content.DialogInterface.OnClickListener l){return this;}
  public Builder setPositiveButton(String s,android.content.DialogInterface.OnClickListener l){return this;}
  public AlertDialog show(){return null;}
}}
