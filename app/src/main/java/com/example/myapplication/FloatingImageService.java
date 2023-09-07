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
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;

public class FloatingImageService extends Service {
    private static final int NOTIFICATION_ID = 1; // Идентификатор уведомления
    private static final String NOTIFICATION_CHANNEL_ID = "floating_image_channel"; // Идентификатор канала уведомлений

    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;

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

        // Создаем параметры для отображения окна поверх всех экранов
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER | Gravity.CENTER_VERTICAL;

        // Получаем WindowManager
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Создаем макет и добавляем изображение в него
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.floating_image_layout, null);

        // Настройка изображения (здесь используется пример изображения с именем "floating_image")
        ImageView imageView = floatingView.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.picture);

        // Добавляем макет поверх всех виджетов
        windowManager.addView(floatingView, params);

        // Добавляем слушателя жестов для перемещения виджета
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
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int newX = initialX + (int) (event.getRawX() - initialTouchX);
                        int newY = initialY + (int) (event.getRawY() - initialTouchY);
                        params.x = newX;
                        params.y = newY;
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Создаем канал уведомлений (для Android 8 и выше)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        // Запускаем службу в режиме foreground, чтобы предотвратить ее автоматическое завершение
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

    // Метод для создания канала уведомлений (только для Android 8 и выше)
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

    // Метод для построения уведомления
    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Floating Image Service")
                .setContentText("Виджет активен")
                .setSmallIcon(R.drawable.picture)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        return builder.build();
    }
}
