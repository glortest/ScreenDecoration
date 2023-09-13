package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FloatingImageService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "floating_image_channel";

    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private ImageView imageView;
    private boolean isMoving = false;
    private static boolean hasNotification = false;
    private int originalImageResource;

    public FloatingImageService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER | Gravity.CENTER_VERTICAL;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.floating_image_layout, null);

        imageView = floatingView.findViewById(R.id.imageView);
        originalImageResource = R.drawable.nothing;
        imageView.setImageResource(originalImageResource);


        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);

        windowManager.addView(floatingView, params);

        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isMoving = false;
                        imageView.setImageResource(R.drawable.move);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int newX = initialX + (int) (event.getRawX() - initialTouchX);
                        int newY = initialY + (int) (event.getRawY() - initialTouchY);
                        params.x = newX;
                        params.y = newY;
                        windowManager.updateViewLayout(floatingView, params);
                        isMoving = false;
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isMoving) {
                            imageView.setImageResource(originalImageResource);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        IntentFilter filter2 = new IntentFilter("com.example.myapplication.NOTIFICATION_UPDATE");
        registerReceiver(notificationUpdateReceiver, filter2);

        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Floating Image Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private BroadcastReceiver notificationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra("hasNotification")) {
                hasNotification = intent.getBooleanExtra("hasNotification", false);
                updateWidgetImage();
            }
        }
    };

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = (level / (float) scale) * 100;

            if (batteryPct < 20) {
                imageView.setImageResource(R.drawable.need_power);
                originalImageResource = R.drawable.need_power;
            } else {
                originalImageResource = R.drawable.nothing;
                imageView.setImageResource(originalImageResource);
            }
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                        || status == BatteryManager.BATTERY_STATUS_FULL;

                if (isCharging) {
                    originalImageResource = R.drawable.power;
                    imageView.setImageResource(R.drawable.power);
                } else {
                    originalImageResource = R.drawable.nothing;
                    imageView.setImageResource(originalImageResource);
                }
            }
        }
    };
    private void updateWidgetImage() {
        if (hasNotification) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
            originalImageResource = R.drawable.ic_launcher_foreground;
        } else {
            imageView.setImageResource(R.drawable.nothing);
            originalImageResource = R.drawable.nothing;
        }
    }

    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Floating Image Service")
                .setContentText("Виджет активен")
                .setSmallIcon(originalImageResource)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        return builder.build();
    }
}
