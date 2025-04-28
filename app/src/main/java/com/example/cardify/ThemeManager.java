package com.example.cardify;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeManager {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_IS_DARK_THEME = "is_dark_theme";

    public static void saveTheme(Context context, boolean isDark) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_DARK_THEME, isDark).apply();
    }

    public static boolean isDarkTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_DARK_THEME, false); // По умолчанию светлая тема
    }
}

