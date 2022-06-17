package com.pacosotelo.coro.vista;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pacosotelo.coro.R;

public class Principal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAnalytics.getInstance(this);

        Intent i = new Intent(Principal.this, Lista.class);
        startActivity(i);
        //overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        finish();
    }
}