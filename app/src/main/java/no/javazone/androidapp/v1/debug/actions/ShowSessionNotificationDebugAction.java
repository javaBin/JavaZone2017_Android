package no.javazone.androidapp.v1.debug.actions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.database.ScheduleContract;
import no.javazone.androidapp.v1.debug.DebugAction;
import no.javazone.androidapp.v1.ui.activity.MapActivity;

public class ShowSessionNotificationDebugAction implements DebugAction {
    @Override
    public void run(Context context, Callback callback) {

        Intent i = new Intent(Intent.ACTION_VIEW,
                ScheduleContract.Sessions.buildSessionUri("__keynote__"));

        PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        Intent mapIntent = new Intent(context, MapActivity.class);
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        mapIntent.putExtra(MapActivity.EXTRA_ROOM, "keynote");
        PendingIntent piMap = TaskStackBuilder
                .create(context)
                .addNextIntent(mapIntent)
                .getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);

        //= PendingIntent.getActivity(context, 0, mapIntent, 0);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("test notification")
                .setContentText("yep, this is a test")
                .setTicker("hey, you got a test")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentIntent(pi)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true);
        notifBuilder.addAction(R.drawable.ic_stat_map,
                context.getString(R.string.title_map),
                piMap);

        NotificationCompat.InboxStyle richNotification = new NotificationCompat.InboxStyle(
                notifBuilder)
                .setBigContentTitle(context.getResources().getQuantityString(R.plurals.session_notification_title,
                        1,
                        8,
                        1));

        NotificationManager nm = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        nm.notify(32534, richNotification.build());


    }

    @Override
    public String getLabel() {
        return "Show \"about to start\" notif";
    }
}