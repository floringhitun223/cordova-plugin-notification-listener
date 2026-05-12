# cordova-plugin-notification-listener

A Cordova plugin for Android that listens to system notifications, retrieves active notifications, and allows opening or dismissing them.

## Supported Platforms

- Android (API 21+)

## Installation

```bash
cordova plugin add cordova-plugin-notification-listener
```

Or from GitHub:

```bash
cordova plugin add https://github.com/floringhitun223/cordova-plugin-notification-listener
```

---

## Android Setup

This plugin requires the **Notification Listener Service** permission, which must be granted manually by the user in Android Settings.

---

## Usage

### 1. Check if permission is granted

```javascript
cordova.plugins.NotificationListener.isPermissionGranted(
    function(granted) {
        console.log("Permission granted:", granted); // true or false
    },
    function(error) {
        console.error(error);
    }
);
```

---

### 2. Request permission (opens Android Settings)

```javascript
cordova.plugins.NotificationListener.requestPermission(
    function() {
        console.log("Settings opened");
    },
    function(error) {
        console.error(error);
    }
);
```

---

### 3. Start listening for notifications

Fires every time a notification is posted or removed.

```javascript
cordova.plugins.NotificationListener.startListening(
    function(notification) {
        if (notification.removed) {
            // Notification was dismissed
            console.log("Removed notification key:", notification.key);
        } else {
            // New notification received
            console.log("App:", notification.appName);
            console.log("Title:", notification.title);
            console.log("Text:", notification.text);
            console.log("Time:", notification.time);
            console.log("Icon (base64):", notification.iconBase64);
        }
    },
    function(error) {
        console.error(error);
    }
);
```

#### Notification object structure

| Field | Type | Description |
|-------|------|-------------|
| `id` | number | Notification ID |
| `key` | string | Unique notification key |
| `packageName` | string | App package name (e.g. `com.whatsapp`) |
| `appName` | string | Human-readable app name (e.g. `WhatsApp`) |
| `title` | string | Notification title |
| `text` | string | Notification body text |
| `time` | number | Timestamp (milliseconds) |
| `iconBase64` | string | App icon as Base64 PNG string |
| `removed` | boolean | `true` if notification was dismissed |

---

### 4. Stop listening

```javascript
cordova.plugins.NotificationListener.stopListening(
    function() {
        console.log("Stopped listening");
    },
    function(error) {
        console.error(error);
    }
);
```

---

### 5. Get all active notifications

```javascript
cordova.plugins.NotificationListener.getActiveNotifications(
    function(notifications) {
        notifications.forEach(function(n) {
            console.log(n.appName, n.title, n.text);
        });
    },
    function(error) {
        console.error(error);
    }
);
```

---

### 6. Open a notification (launches the app)

```javascript
cordova.plugins.NotificationListener.openNotification(
    notificationKey,
    function() {
        console.log("Notification opened");
    },
    function(error) {
        console.error(error);
    }
);
```

---

### 7. Cancel (dismiss) a notification

```javascript
cordova.plugins.NotificationListener.cancelNotification(
    notificationKey,
    function() {
        console.log("Notification dismissed");
    },
    function(error) {
        console.error(error);
    }
);
```

---

### 8. Clear all notifications

```javascript
cordova.plugins.NotificationListener.clearAllNotifications(
    function() {
        console.log("All notifications cleared");
    },
    function(error) {
        console.error(error);
    }
);
```

---

## Full Example

```javascript
document.addEventListener("deviceready", function() {

    cordova.plugins.NotificationListener.isPermissionGranted(function(granted) {
        if (!granted) {
            cordova.plugins.NotificationListener.requestPermission(null, null);
            return;
        }

        cordova.plugins.NotificationListener.startListening(function(notification) {
            if (notification.removed) return;

            console.log("[" + notification.appName + "] " + notification.title + ": " + notification.text);
        }, function(err) {
            console.error("Listen error:", err);
        });

    }, function(err) {
        console.error("Permission check error:", err);
    });

}, false);
```

---

## Notes

- This plugin only works on **Android**.
- The Notification Listener permission **cannot be granted programmatically** — the user must enable it manually in Settings → Apps → Special app access → Notification access.
- On **Android 16+**, opening notifications uses `ActivityOptions` with background activity start mode allowed.

---

## License

MIT
