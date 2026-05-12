package com.example.notificationlistener;

import android.app.Notification;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;

public class AppNotificationService extends NotificationListenerService {

    public static NotificationCallback callback = null;
    public static AppNotificationService instance = null;

    public interface NotificationCallback {
        void onNotificationReceived(JSONObject notification);
    }

    @Override
    public void onListenerConnected() {
        instance = this;
    }

    @Override
    public void onListenerDisconnected() {
        instance = null;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (callback == null) return;
        JSONObject data = buildNotificationJSON(sbn);
        if (data != null) callback.onNotificationReceived(data);
    }

    // FIXED: Only one onNotificationRemoved — notifies JS so UI stays in sync
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (callback != null) {
            try {
                JSONObject data = new JSONObject();
                data.put("removed", true);
                data.put("key", sbn.getKey());
                callback.onNotificationReceived(data);
            } catch (JSONException ignored) {}
        }
    }

    public JSONObject buildNotificationJSON(StatusBarNotification sbn) {
        try {
            JSONObject data = new JSONObject();
            String packageName = sbn.getPackageName();

            data.put("id", sbn.getId());
            data.put("key", sbn.getKey());
            data.put("packageName", packageName);

            PackageManager pm = getPackageManager();
            String appName = packageName;
            try {
                ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                appName = pm.getApplicationLabel(info).toString();
            } catch (PackageManager.NameNotFoundException ignored) {}
            data.put("appName", appName);

            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;
            String title = extras.getString(Notification.EXTRA_TITLE, "");
            CharSequence textSeq = extras.getCharSequence(Notification.EXTRA_TEXT);
            String text = textSeq != null ? textSeq.toString() : "";

            data.put("title", title);
            data.put("text", text);
            data.put("time", sbn.getPostTime());

            try {
                Drawable iconDrawable = pm.getApplicationIcon(packageName);
                Bitmap bitmap = drawableToBitmap(iconDrawable);
                data.put("iconBase64", bitmapToBase64(bitmap));
            } catch (Exception e) {
                data.put("iconBase64", "");
            }

            return data;
        } catch (JSONException e) {
            return null;
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
            drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 64,
            drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 64,
            Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }
}