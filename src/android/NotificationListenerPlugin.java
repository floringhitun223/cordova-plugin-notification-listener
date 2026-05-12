package com.example.notificationlistener;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.service.notification.StatusBarNotification;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationListenerPlugin extends CordovaPlugin {

    private CallbackContext listeningCallbackContext = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
            throws JSONException {

        switch (action) {
            case "isPermissionGranted":
                callbackContext.sendPluginResult(
                    new PluginResult(PluginResult.Status.OK, isPermissionGranted())
                );
                return true;

            case "requestPermission":
                openNotificationSettings();
                callbackContext.success();
                return true;

            case "getActiveNotifications":
                getActiveNotifications(callbackContext);
                return true;

            case "openNotification":
                String key = args.getString(0);
                openNotification(key, callbackContext);
                return true;

            case "startListening":
                startListening(callbackContext);
                return true;

            case "stopListening":
                stopListening();
                callbackContext.success();
                return true;

            case "cancelNotification":
                String cancelKey = args.getString(0);
                cancelNotification(cancelKey);
                callbackContext.success();
                return true;

            case "clearAllNotifications":
                clearAllNotifications();
                callbackContext.success();
                return true;
        }

        return false;
    }

    private boolean isPermissionGranted() {
        String packageName = cordova.getActivity().getPackageName();
        String flat = Settings.Secure.getString(
            cordova.getActivity().getContentResolver(),
            "enabled_notification_listeners"
        );
        if (!TextUtils.isEmpty(flat)) {
            String[] names = flat.split(":");
            for (String name : names) {
                ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && TextUtils.equals(packageName, cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void openNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cordova.getActivity().startActivity(intent);
    }

    private void getActiveNotifications(CallbackContext callbackContext) {
        try {
            AppNotificationService service = AppNotificationService.instance;
            if (service == null) {
                callbackContext.error("Service not running");
                return;
            }

            StatusBarNotification[] active = service.getActiveNotifications();
            JSONArray result = new JSONArray();

            for (StatusBarNotification sbn : active) {
                JSONObject data = service.buildNotificationJSON(sbn);
                if (data != null) result.put(data);
            }
            callbackContext.success(result);
        } catch (Exception e) {
            callbackContext.error("Error: " + e.getMessage());
        }
    }

    private void openNotification(String key, CallbackContext callbackContext) {
    AppNotificationService service = AppNotificationService.instance;
    if (service == null) {
        callbackContext.error("Service not running");
        return;
    }

    StatusBarNotification[] active = service.getActiveNotifications();
    for (StatusBarNotification sbn : active) {
        if (sbn.getKey().equals(key)) {
            PendingIntent contentIntent = sbn.getNotification().contentIntent;
            if (contentIntent == null) {
                callbackContext.error("No content intent");
                return;
            }

            cordova.getActivity().runOnUiThread(() -> {
    try {
        Intent fillIn = new Intent();
        fillIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT >= 34) {
            // Step 1: bring YOUR activity to front first
            // This establishes a foreground user-interaction context
            // that Android 16 requires before allowing the next launch
            Intent bringToFront = new Intent(cordova.getActivity(), cordova.getActivity().getClass());
            bringToFront.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            cordova.getActivity().startActivity(bringToFront);

            // Step 2: small delay to let your activity surface,
            // then fire the actual notification intent from the now-foreground context
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                try {
                    android.app.ActivityOptions options =
                        android.app.ActivityOptions.makeBasic();
                    options.setPendingIntentBackgroundActivityStartMode(
                        android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    );
                    contentIntent.send(
                        cordova.getActivity(), 0, fillIn,
                        null, null, null,
                        options.toBundle()
                    );
                    service.cancelNotification(sbn.getKey());
                    callbackContext.success();
                } catch (PendingIntent.CanceledException e) {
                    callbackContext.error("PendingIntent cancelled: " + e.getMessage());
                } catch (Exception e) {
                    callbackContext.error("Failed: " + e.getMessage());
                }
            }, 150); // 150ms is enough for the activity to surface

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentIntent.send(
                cordova.getActivity(), 0, fillIn,
                null, null, null,
                android.os.Bundle.EMPTY
            );
            service.cancelNotification(sbn.getKey());
            callbackContext.success();
        } else {
            contentIntent.send(cordova.getActivity(), 0, fillIn);
            service.cancelNotification(sbn.getKey());
            callbackContext.success();
        }

    } catch (PendingIntent.CanceledException e) {
        callbackContext.error("PendingIntent cancelled: " + e.getMessage());
    } catch (Exception e) {
        callbackContext.error("Failed to open: " + e.getMessage());
    }
});
            return;
        }
    }

    callbackContext.error("Notification not found");
}

    private void cancelNotification(String key) {
        AppNotificationService service = AppNotificationService.instance;
        if (service != null) {
            service.cancelNotification(key);
        }
    }

    private void clearAllNotifications() {
        AppNotificationService service = AppNotificationService.instance;
        if (service != null) {
            service.cancelAllNotifications();
        }
    }

    private void startListening(CallbackContext callbackContext) {
        listeningCallbackContext = callbackContext;
        AppNotificationService.callback = notification -> {
            PluginResult result = new PluginResult(PluginResult.Status.OK, notification);
            result.setKeepCallback(true);
            listeningCallbackContext.sendPluginResult(result);
        };
        PluginResult noResult = new PluginResult(PluginResult.Status.NO_RESULT);
        noResult.setKeepCallback(true);
        callbackContext.sendPluginResult(noResult);
    }

    private void stopListening() {
        AppNotificationService.callback = null;
        listeningCallbackContext = null;
    }
}