var exec = require('cordova/exec');

var NotificationListener = {

    isPermissionGranted: function(success, error) {
        exec(success, error, 'NotificationListener', 'isPermissionGranted', []);
    },

    requestPermission: function(success, error) {
        exec(success, error, 'NotificationListener', 'requestPermission', []);
    },

    startListening: function(onNotification, onError) {
        exec(onNotification, onError, 'NotificationListener', 'startListening', []);
    },

    stopListening: function(success, error) {
        exec(success, error, 'NotificationListener', 'stopListening', []);
    },

    getActiveNotifications: function(success, error) {
        exec(success, error, 'NotificationListener', 'getActiveNotifications', []);
    },

    openNotification: function(key, success, error) {
        exec(success, error, 'NotificationListener', 'openNotification', [key]);
    },

    cancelNotification: function(key, success, error) {
        exec(success, error, 'NotificationListener', 'cancelNotification', [key]);
    },

    clearAllNotifications: function(success, error) {
        exec(success, error, 'NotificationListener', 'clearAllNotifications', []);
    }

};

module.exports = NotificationListener;
