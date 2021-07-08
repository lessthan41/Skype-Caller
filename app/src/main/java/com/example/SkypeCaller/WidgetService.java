package com.example.SkypeCaller;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class WidgetService extends Service {
    private static final int REDIRECT_SECOND = 15;
    private static String SKYPE_ID = "live:.cid.8847eccecc234b96?call"; // wayne
//    private static String SKYPE_ID = "echo123?call";

    private ReceiveBroadcastReceiver imageChangeBroadcastReceiver;
    MultiTask multiTask;

    View mFloatingView;
    WindowManager.LayoutParams layoutParams;
    WindowManager windowManager;
    ImageButton button;

    int LAYOUT_FLAG;
    private boolean inProcess = false;

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
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                WindowManager.LayoutParams.,
                PixelFormat.TRANSLUCENT
        );

//        initial position
        layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        layoutParams.x = 0;
        layoutParams.y = 0;
//        layoutParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(mFloatingView, layoutParams);
        mFloatingView.setVisibility(View.VISIBLE);

//        button onclick event
        button = mFloatingView.findViewById(R.id.button3);
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
        if (!inProcess) {
//            skype uri
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("skype:" + SKYPE_ID));
            startActivity(browserIntent);

//            count down
            multiTask = new MultiTask();
            multiTask.execute();

            Toast.makeText(this, "Skype Call", Toast.LENGTH_SHORT).show();    // Show Message
        }
    }

     public void switchButton(int i) {
//         button.setImageResource(R.drawable.num1);
        switch (i) {
            case 5:
                button.setImageResource(R.drawable.num5);
                break;
            case 4:
                button.setImageResource(R.drawable.num4);
                break;
            case 3:
                button.setImageResource(R.drawable.num3);
                break;
            case 2:
                button.setImageResource(R.drawable.num2);
                break;
            case 1:
                button.setImageResource(R.drawable.num1);
                break;
            default:
                break;
        }
    }

    class MultiTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            inProcess = true;
            try {
                for (int i = REDIRECT_SECOND; i > 0; i--) {
                    if (i <= 5) {
                        publishProgress(i);
                    }
                    Thread.sleep(1000);
                    if (isCancelled())
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            switchButton(progress[0]);
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            inProcess = false;

//            reopen application
            Intent reopenIntent = new Intent(WidgetService.this, MainActivity.class);

            try {
                reopenIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(reopenIntent);
            } catch (Exception e) {
                reopenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(reopenIntent);
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            inProcess = false;
        }
    }

    /**
     * Receive Broadcast Receiver.
     * */
    public class ReceiveBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//                stop multitask counting down
            multiTask.cancel(true);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(imageChangeBroadcastReceiver);
        if (mFloatingView != null) {
            windowManager.removeView(mFloatingView);
        }
    }
}
