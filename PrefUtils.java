package com.example.cyawait;

import android.content.Context;
import android.content.SharedPreferences;

//Can be used to dynamically get an ip address, however it does not always get the ip that the server uses thus is unreliable
public class PrefUtils {
    private static final String PREFS_NAME = "AppPrefs";
    public static final String KEY_SERVER_IP = "server_ip";

    public static void saveServerIP(Context context, String ip) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_SERVER_IP, ip).apply();
    }

    public static String getServerIP(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_SERVER_IP, "192.168.0.219"); // default IP
    }
}

