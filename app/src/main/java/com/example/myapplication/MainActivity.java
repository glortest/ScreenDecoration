package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class MainActivity extends AppCompatActivity {
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Проверяем, предоставлено ли разрешение на SYSTEM_ALERT_WINDOW
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                    // Если разрешение не предоставлено, запрашиваем его с помощью системного диалога
                    requestSystemAlertWindowPermission();
                } else {
                    // Если разрешение уже предоставлено или устройство работает на более старой версии Android
                    // Запускаем службу для отображения изображения
                    startFloatingImageService();
                }
            }
        });
        findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFloatImageService();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestSystemAlertWindowPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // Разрешение было предоставлено, запускаем службу
                startFloatingImageService();
            } else {
                // Разрешение не было предоставлено, обработайте это соответствующим образом
                // Можете показать пользователю сообщение о том, что без разрешения приложение не сможет отображать виджеты поверх экрана.
            }
        }
    }

    private void startFloatingImageService() {
        Intent intent = new Intent(this, FloatingImageService.class);
        startService(intent);

    }
    private void closeFloatImageService(){
        Intent intent = new Intent(this, FloatingImageService.class);
        stopService(intent);
    }
}