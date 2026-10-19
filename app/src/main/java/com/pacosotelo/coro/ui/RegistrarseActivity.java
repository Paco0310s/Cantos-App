package com.pacosotelo.coro.ui;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.pacosotelo.coro.R;
import com.pacosotelo.coro.databinding.ActivityRegistrarseBinding;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.tools.Transiciones;
import com.pacosotelo.coro.viewmodel.AuthViewModel;

import java.util.Objects;

public class RegistrarseActivity extends AppCompatActivity {
    private ActivityRegistrarseBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrarseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.bRegistrarse.setOnClickListener(v -> registrarse());
        binding.ibAtras.setOnClickListener(v -> regresar());
        binding.tvYaTengoCuenta.setOnClickListener(v -> regresar());

        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.bRegistrarse.setEnabled(!isLoading);
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getUserAuthenticated().observe(this, usuario -> {
            if (usuario != null) {
                finalizarRegistro(usuario);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                regresar();
            }
        });
    }

    private void regresar() {
        Intent i = new Intent(RegistrarseActivity.this, LoginActivity.class);
        startActivity(i);
        Transiciones.right(this);
        finish();
    }

    public void registrarse() {
        String nombre = Objects.requireNonNull(binding.etNombre.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.etCorreo.getText()).toString().trim();
        String pass = Objects.requireNonNull(binding.etPass.getText()).toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(this, "Introduce tu nombre", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Introduce tu correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Introduce tu contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.length() < 8) {
            Toast.makeText(this, "Mínimo 8 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        authViewModel.createUserWithEmail(nombre, email, pass, "");
    }

    private void finalizarRegistro(Usuario usuarioData) {
        Toast.makeText(RegistrarseActivity.this, getString(R.string.bienvenida) + " " + usuarioData.getNombre(), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(RegistrarseActivity.this, MenuLateralActivity.class);
        i.putExtra("usuario", usuarioData);
        startActivity(i);
        Transiciones.fade(this);
        finish();
    }
}
