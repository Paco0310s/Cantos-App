package com.pacosotelo.coro.tools;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        SharedPreferences pref = getSharedPreferences("theme_pref", MODE_PRIVATE);
        boolean isDarkMode = pref.getBoolean("is_dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
