package com.example.hskcompanion;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

/** Local, opt-in, battery-friendly daily reminder. Not an exact-time alarm. */
public final class ReviewReminders {
    private ReviewReminders() {}
    public static final String ACTION = "com.example.hskcompanion.REVIEW_REMINDER";
    public static final int REQUEST_CODE = 731;
    private static PendingIntent intent(Context context) {
        Intent i = new Intent(context, ReviewReminderReceiver.class).setAction(ACTION);
        return PendingIntent.getBroadcast(context,REQUEST_CODE,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static boolean enabled(Context context) {
        return context.getSharedPreferences("hsk_progress_v1",Context.MODE_PRIVATE).getBoolean("reminders_enabled",false);
    }
    public static int hour(Context context) {
        return context.getSharedPreferences("hsk_progress_v1",Context.MODE_PRIVATE).getInt("reminders_hour",20);
    }
    public static void configure(Context context, boolean enabled, int hour) {
        if(hour<7 || hour>21) throw new IllegalArgumentException("Notification outside allowed 07–21 window");
        context.getSharedPreferences("hsk_progress_v1",Context.MODE_PRIVATE).edit()
                .putBoolean("reminders_enabled",enabled).putInt("reminders_hour",hour).apply();
        schedule(context);
    }
    public static void schedule(Context context) {
        AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(mgr==null)return;
        PendingIntent pi=intent(context);
        mgr.cancel(pi);
        if(!enabled(context))return;
        Calendar c=Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,hour(context));c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);
        if(c.getTimeInMillis()<=System.currentTimeMillis())c.add(Calendar.DAY_OF_YEAR,1);
        // Inexact alarm: Android may delay delivery because of battery optimization/Doze.
        mgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pi);
    }
}
