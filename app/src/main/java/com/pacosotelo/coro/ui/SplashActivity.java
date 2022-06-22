package com.pacosotelo.coro.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pacosotelo.coro.R;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseAnalytics.getInstance(this);

        final Handler handler = new Handler();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        Intent i = new Intent(SplashActivity.this, ListaCantosActivity.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        finish();
                    }
                });
            }
        }, 2000);
    }
}