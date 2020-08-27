package com.villagelight.app.fingerprint;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {

    private static final String PREF_NAME = "config";
    private static final String PREF_FINGERPRINT = "fingerprint";

    public static void setProtect(Context context, boolean flag) {

        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        pref.edit().putBoolean(PREF_FINGERPRINT, flag).apply();

    }

    public static boolean isProtect(Context context) {

        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return pref.getBoolean(PREF_FINGERPRINT, false);

    }
}
