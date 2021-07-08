package com.example.SkypeCaller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SkypeAccessibilityService extends NotificationListenerService {

    private static final class ApplicationPackageNames {
        public static final String SKYPE_PACK_NAME = "com.skype.raider";
    }

    public static final class InterceptedNotificationCode {
        public static final int SKYPE_CODE = 1;
        public static final int OTHER_NOTIFICATIONS_CODE = 2; // We ignore all notification with code == 2
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn){

        int notificationCode = matchNotificationCode(sbn);
//        String pack = sbn.getPackageName();
        Bundle extras = sbn.getNotification().extras;
//        String title = extras.getString("android.title");
        String text = extras.getCharSequence("android.text").toString();
//        String subtext = "";

        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE)
        {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N))
            {
//                /* Used for SendBroadcast */
//                Parcelable b[] = (Parcelable[]) extras.get(Notification.EXTRA_MESSAGES);
//
//                if(b != null){
//                    for (Parcelable tmp : b){
//                        Bundle msgBundle = (Bundle) tmp;
//                        subtext = msgBundle.getString("text");
//                    }
//                    Log.d("DetailsEzra1 :", subtext);
//                }
//
//                if(subtext.isEmpty())
//                {
//                    subtext = text;
//                }
//                Log.d("DetailsEzra2 :", subtext);

                Intent intent = new Intent("com.example.myapplication.skypemonitoring");
                intent.putExtra("Notification Code", notificationCode);
//                intent.putExtra("package", pack);
//                intent.putExtra("title", title);
//                intent.putExtra("text", subtext);
//                intent.putExtra("id", sbn.getId());

                sendBroadcast(intent);
//                /* End */

                /* Used for Toast */
                if(text != null) {
                    String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                    String devicemodel = android.os.Build.MANUFACTURER+android.os.Build.MODEL+android.os.Build.BRAND+android.os.Build.SERIAL;

                    DateFormat df = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
                    String date = df.format(Calendar.getInstance().getTime());
                    /*
                    Toast.makeText(getApplicationContext(), "Notification : " + notificationCode + "\nPackages : " + pack + "\nTitle : " + title + "\nText : " + text + "\nId : " + date+ "\nandroid_id : " + android_id+ "\ndevicemodel : " + devicemodel,
                            Toast.LENGTH_LONG).show();
                    */

                    Intent intentPending = new Intent(getApplicationContext(), MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentPending, 0);

                    NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentTitle(getResources().getString( R.string.app_name ))
                            .setContentText("This is income messagess : "+text);

                    builder.setWhen(System.currentTimeMillis());
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    Bitmap largeIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_camera);
                    builder.setLargeIcon(largeIconBitmap);
                    // Make the notification max priority.
                    builder.setPriority(Notification.PRIORITY_MAX);
                    // Make head-up notification.
                    builder.setFullScreenIntent(pendingIntent, true);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(1, builder.build());
                }
                /* End Used for Toast */

            }

        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);

        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {

            /* Used Used for SendBroadcast */
            Intent intentPending = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentPending, 0);

            NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(getResources().getString( R.string.app_name ))
                    .setContentText("End Skype Call");

            builder.setWhen(System.currentTimeMillis());
            builder.setSmallIcon(R.mipmap.ic_launcher);
            Bitmap largeIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_camera);
            builder.setLargeIcon(largeIconBitmap);
            // Make the notification max priority.
            builder.setPriority(Notification.PRIORITY_MAX);
            // Make head-up notification.
            builder.setFullScreenIntent(pendingIntent, true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1, builder.build());


            /* End Used for Toast */

//            broadcast notification removed
//            Intent intent = new  Intent("com.example.myapplication.skypemonitoring");
//            intent.putExtra("Notification Code", notificationCode);
//            sendBroadcast(intent);

//            reopen application
            Intent reopenIntent = new Intent(SkypeAccessibilityService.this, MainActivity.class);
            reopenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(reopenIntent);
        }
    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        if (packageName.equals(ApplicationPackageNames.SKYPE_PACK_NAME)) {
            return(InterceptedNotificationCode.SKYPE_CODE);
        } else {
            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }
}