package com.pacosotelo.coro.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Usuario;

//Clase que hace un splash mientras se obtiene el usuario
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Firebase Analytics, para poder observar informacion de la app posteriormente
        FirebaseAnalytics.getInstance(this);

        // Obtenemos la instancia de FirebaseDatabase y FirebaseAuth
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        //Obtenemos la referencia de la base datos, root
        DatabaseReference dr = fd.getReference();

        //Obtenemos el usuario actual
        FirebaseUser currentUser = auth.getCurrentUser();

        // Si el usuario ya ha iniciado sesión previamente
        if(currentUser != null) {
            dr.child("usuarios").child(currentUser.getUid()).get().addOnCompleteListener(task -> {
                // Verifica si se pudo obtener el usuario
                if (task.isSuccessful()) {
                    // Si si, obtenemos los datos del usuario
                    Usuario usuario = task.getResult().getValue(Usuario.class);

                    // Y nos dirigimos al Menu lateral
                    Intent i = new Intent(SplashActivity.this,
                            MenuLateralActivity.class);
                    // Mandamos el usuario
                    i.putExtra("usuario", usuario);
                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    finish();
                } else {
                    // Si no se pudo mostramos un error
                    Toast.makeText(SplashActivity.this,
                            getString(R.string.error_obtener_cuenta) + ": " +
                                    task.getException(), Toast.LENGTH_SHORT).show();
                }
            });
        } else { // Si no ha iniciado sesión hacemos un intent al login
            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            finish();
        }
    }
}