package com.pacosotelo.coro.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.tools.Constantes;

import com.pacosotelo.coro.databinding.ActivitySplashBinding;
import com.pacosotelo.coro.tools.Transiciones;

import androidx.lifecycle.ViewModelProvider;
import com.pacosotelo.coro.viewmodel.AuthViewModel;

//Clase que hace un splash mientras se obtiene el usuario
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private FirebaseDatabase fd;
    private DatabaseReference dr;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase Analytics, para poder observar informacion de la app posteriormente
        //FirebaseAnalytics.getInstance(this);

        // Obtenemos la instancia de FirebaseDatabase y FirebaseAuth
        fd = FirebaseDatabase.getInstance();
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        authViewModel.getUserAuthenticated().observe(this, usuario -> {
            if (usuario != null) {
                Intent i = new Intent(SplashActivity.this, MenuLateralActivity.class);
                i.putExtra("usuario", usuario);
                startActivity(i);
                Transiciones.fade(this);
                finish();
            }
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        //Obtenemos la referencia de la base datos, root
        dr = fd.getReference();

        dr.child("constantes").child("mantenimiento").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult().getValue() == null) mostrarErrorMantenimiento();
                else {
                    String mantenimiento_str = task.getResult().getValue().toString();
                    boolean mantenimiento = Boolean.parseBoolean(mantenimiento_str);

                    if(mantenimiento) {
                        mostrarMensajeMantenimiento();
                    }
                    else {
                        comprobarVersion();
                    }
                }
            } else {
                mostrarErrorMantenimiento();
            }
        });
    }

    private void comprobarVersion() {
        //Obtenemos la version de la ultima acualización
        dr.child("constantes").child("version").get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                if(task.getResult().getValue() == null) mostrarErrorVersion();
                else {
                    String version_str = task.getResult().getValue().toString();
                    int version = Integer.parseInt(version_str);

                    if(Constantes.VERSION >= version) {
                        redireccionar();
                    }
                    else {
                        mostrarMensajeActualización();
                    }
                }
            } else {
                mostrarErrorVersion();
            }
        });
    }

    private void mostrarMensajeMantenimiento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Aviso");
        String mensaje = "La aplicación se encuentra en mantenimiento \nIntente nuevamente mas tarde";
        builder.setMessage(mensaje);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> finishAffinity());
        AlertDialog alerta = builder.create();
        alerta.show();
    }

    private void mostrarMensajeActualización() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Aviso");
        String mensaje = "Actualice la aplicación a la versión mas reciente";
        builder.setMessage(mensaje);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> finishAffinity());
        AlertDialog alerta = builder.create();
        alerta.show();
    }

    private void mostrarErrorMantenimiento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        String mensaje = "Ocurrió un error al obtener la información de mantenimiento \nVerifique su conexión a internet";
        builder.setMessage(mensaje);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> finishAffinity());
        AlertDialog alerta = builder.create();
        alerta.show();
    }

    private void mostrarErrorVersion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        String mensaje = "Ocurrió un error al obtener la versión \nVerifique su conexión a internet";
        builder.setMessage(mensaje);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> finishAffinity());
        AlertDialog alerta = builder.create();
        alerta.show();
    }

    private void redireccionar() {
        // Si el usuario ya ha iniciado sesión previamente
        if(authViewModel.getCurrentFirebaseUser() != null) {
            authViewModel.handleExistingUser();
        } else { // Si no ha iniciado sesión hacemos un intent al login
            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(i);
            Transiciones.fade(this);
            finish();
        }
    }
}