package com.example.hskcompanion;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.List;

/** Runs entirely offline; no cloud service or tracking. */
public final class ReviewReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID="hsk_due_reviews_v1";
    @Override public void onReceive(Context context, Intent intent) {
        if(intent!=null && !ReviewReminders.ACTION.equals(intent.getAction())){
            ReviewReminders.schedule(context);return; // Boot/time/timezone changed
        }
        if(!ReviewReminders.enabled(context))return;
        if(Build.VERSION.SDK_INT>=33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
        ProgressStore progress=new ProgressStore(context);
        WordRepository repository=new WordRepository(context);
        List<Word> deck=repository.throughLevel("2.0",progress.level());
        int due=progress.dueCount(deck);
        if(due<=0)return; // Never nag when the learner is already caught up.
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager==null)return;
        if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(new NotificationChannel(
                CHANNEL_ID,"HSK reviews",NotificationManager.IMPORTANCE_DEFAULT));
        Intent opening=new Intent(context,MainActivity.class);
        opening.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent tap=PendingIntent.getActivity(context,732,opening,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder=Build.VERSION.SDK_INT>=26 ? new Notification.Builder(context,CHANNEL_ID):new Notification.Builder(context);
        Notification notification=builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Chinese words ready to review")
                .setContentText(due+" HSK words are ready. A short review is enough.")
                .setAutoCancel(true).setContentIntent(tap).build();
        manager.notify(731,notification);
    }
}
