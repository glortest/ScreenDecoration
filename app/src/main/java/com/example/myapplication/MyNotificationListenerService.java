package com.example.myapplication;

import android.app.Notification;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class MyNotificationListenerService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        sendNotificationUpdateBroadcast(true);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        sendNotificationUpdateBroadcast(false);
    }

    private void sendNotificationUpdateBroadcast(boolean hasNotification) {
        Intent intent = new Intent("com.example.myapplication.NOTIFICATION_UPDATE");
        intent.putExtra("hasNotification", hasNotification);
        sendBroadcast(intent);
    }
}