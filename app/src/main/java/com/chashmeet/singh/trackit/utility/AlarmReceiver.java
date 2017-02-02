package com.chashmeet.singh.trackit.utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import io.realm.Realm;
import io.realm.RealmResults;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.activity.EpisodeDetail;
import com.chashmeet.singh.trackit.activity.MainActivity;
import com.chashmeet.singh.trackit.realm.RealmNotification;
import com.chashmeet.singh.trackit.realm.RealmSingleton;

public class AlarmReceiver extends BroadcastReceiver {

    private final StyleSpan mBoldSpan = new StyleSpan(Typeface.BOLD);
    private NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
    private RealmResults<RealmNotification> notificationList;

    private SpannableString makeNotificationLine(String title, String text) {
        final SpannableString spannableString;
        if (title != null && title.length() > 0) {
            spannableString = new SpannableString(String.format("%s  %s", title, text));
            spannableString.setSpan(mBoldSpan, 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableString = new SpannableString(text);
        }
        return spannableString;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Realm realm = RealmSingleton.getInstance().getRealm();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                String showName = intent.getStringExtra("show_name");
                String details = intent.getStringExtra("details");
                int episodeID = intent.getIntExtra("episodeID", 0);

                notificationList = realm.where(RealmNotification.class).findAll();
                RealmNotification episode = realm.createObject(RealmNotification.class);
                episode.setDetails(details);
                episode.setNotificationID(episodeID);
                episode.setShowName(showName);

                inboxStyle.addLine(makeNotificationLine(showName, details));

                for (int i = notificationList.size() - 1, count = 0; i >= 0 && count < 4; i--, count++) {
                    inboxStyle.addLine(makeNotificationLine(notificationList.get(i).getShowName(),
                            notificationList.get(i).getDetails()));
                }
                inboxStyle.setBigContentTitle("Track My Shows");
                if ((notificationList.size() - 4) > 0) {
                    inboxStyle.setSummaryText("+" + (notificationList.size() - 4) + " more");
                }

                Notification notification;
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

                if (notificationList == null || notificationList.size() == 0) {
                    Intent notificationIntent = new Intent(context, EpisodeDetail.class);
                    notificationIntent.putExtra("realm_notification", true);
                    notificationIntent.putExtra("episode_id", episodeID);
                    notificationIntent.putExtra("show_title", showName);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(EpisodeDetail.class);
                    stackBuilder.addNextIntent(notificationIntent);
                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(episodeID, PendingIntent.FLAG_UPDATE_CURRENT);
                    notification = builder.setContentTitle("Track My Shows")
                            .setContentText(makeNotificationLine(showName, details))
                            .setTicker("Show airing!")
                            .setSmallIcon(R.mipmap.ic_notification)
                            .setLargeIcon(bitmap)
                            .setAutoCancel(true)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                            .setContentIntent(pendingIntent).build();
                } else {
                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(notificationIntent);
                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    notification = builder.setContentTitle("Track My Shows")
                            .setContentText((notificationList.size() + 1) + " new episodes")
                            .setTicker("Show airing!")
                            .setSmallIcon(R.mipmap.ic_notification)
                            .setLargeIcon(bitmap)
                            .setAutoCancel(true)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                            .setStyle(inboxStyle)
                            .setContentIntent(pendingIntent).build();
                }
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(R.string.app_name, notification);
            }
        });
    }
}