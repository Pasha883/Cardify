package com.example.cardify;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

public class SharedPrefsHelper {
    public static Set<String> getSet(Context context, String key) {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getStringSet(key, new HashSet<>());
    }

    public static void saveSet(Context context, String key, Set<String> set) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putStringSet(key, set).apply();
    }
}

