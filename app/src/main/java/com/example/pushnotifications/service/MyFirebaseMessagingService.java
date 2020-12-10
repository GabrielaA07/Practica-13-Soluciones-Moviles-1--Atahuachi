package com.example.pushnotifications.service;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.pushnotifications.activities.MainActivity;
import com.example.pushnotifications.app.Config;
import com.example.pushnotifications.util.NotificationsUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private NotificationsUtils notificationsUtils;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        Log.e("NEW TOKEN", s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "FROM: " + remoteMessage.getFrom());
        if (remoteMessage== null) return;

        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification body: " + remoteMessage.getNotification().getBody());
            //TODO: procesar notificación
            processNotification(remoteMessage.getNotification().getBody());
        }

        //si generamos data en la aplicacion
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data charge: " + remoteMessage.getData().toString());
            try {
                JSONObject jsonObject = new JSONObject((remoteMessage.getData().toString()));
                //TODO: interpretar la notificacion
                traduceMessage(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void processNotification(String message) {
        if (!NotificationsUtils.isAppIsInBackground(getApplicationContext())) {
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
        }
    }

    private void traduceMessage(JSONObject jsonObject){
        try {
            JSONObject data = jsonObject.getJSONObject("data");
            String title = data.getString("title");
            String message = data.getString("message");
            boolean isBackground = data.getBoolean("is_background");
            String urlImage = data.getString("image");
            String timeStamp = data.getString("timestamp");
            JSONObject payload = data.getJSONObject("payload");

            if (!NotificationsUtils.isAppIsInBackground(getApplicationContext())) {
                processNotification(message);
            } else {
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("message", message);

                if (TextUtils.isEmpty(urlImage)) {
                    notificationsUtils.showNotificationMessage(title, message, timeStamp, resultIntent);
                } else {
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    notificationsUtils.showNotificationMessage(title, message, timeStamp, resultIntent, urlImage);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}