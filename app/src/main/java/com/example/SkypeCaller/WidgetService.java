package com.example.SkypeCaller;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class WidgetService extends Service {

    String skypeId = "echo123?call";

    private ReceiveBroadcastReceiver imageChangeBroadcastReceiver;
    MultiTask multiTask;

    int LAYOUT_FLAG;
    View mFloatingView;
    WindowManager windowManager;
    ImageView imageClose;
    ImageButton button;

    float height, width;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // inflate layout
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.working, null);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        // initial position
        layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        layoutParams.x = 0;
        layoutParams.y = 0;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(mFloatingView, layoutParams);
        mFloatingView.setVisibility(View.VISIBLE);

        // button onclick event
        button = (ImageButton) mFloatingView.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callSkype(view);
            }
        });

//        we register a receiver to tell when notification has been received
        imageChangeBroadcastReceiver = new ReceiveBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.myapplication.skypemonitoring");
        registerReceiver(imageChangeBroadcastReceiver, intentFilter);

        return START_STICKY;
    }

    private void callSkype(View view) {

        // skype uri
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("skype:" + skypeId));
        startActivity(browserIntent);
        multiTask = new MultiTask();
        multiTask.execute();

        Toast.makeText(this, "Skype Call", Toast.LENGTH_SHORT).show();    // Show Message
    }

    class MultiTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            try {
                int sec = 9;
                for (int i = 0; i < sec; i++) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

//            reopen application
            Intent reopenIntent = new Intent(WidgetService.this, MainActivity.class);
            reopenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(reopenIntent);
        }
    }


    /**
     * Receive Broadcast Receiver.
     * */
    public class ReceiveBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

//            int receivedNotificationCode = intent.getIntExtra("Notification Code",-1);
//            String packages = intent.getStringExtra("package");
//            String title = intent.getStringExtra("title");
//            String text = intent.getStringExtra("text");

//            stop multitask counting down
            multiTask.cancel(true);

//            if(text != null) {
////                String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
////                        Settings.Secure.ANDROID_ID);
////                String devicemodel = android.os.Build.MANUFACTURER+android.os.Build.MODEL+android.os.Build.BRAND+android.os.Build.SERIAL;
////
////                DateFormat df = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
////                String date = df.format(Calendar.getInstance().getTime());
////
////                tvMsg.setText("Notification : " + receivedNotificationCode + "\nPackages : " + packages + "\nTitle : " + title + "\nText : " + text + "\nId : " + date+ "\nandroid_id : " + android_id+ "\ndevicemodel : " + devicemodel);
//            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(imageChangeBroadcastReceiver);
        if (mFloatingView != null) {
            windowManager.removeView(mFloatingView);
        }

        if (imageClose != null) {
            windowManager.removeView(imageClose);
        }
    }
}
