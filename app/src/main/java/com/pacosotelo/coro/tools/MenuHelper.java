package com.pacosotelo.coro.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Grupo;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.ui.LoginActivity;
import com.pacosotelo.coro.viewmodel.GruposViewModel;

import java.util.ArrayList;

public class MenuHelper {

    public static void cerrarSesion(Activity activity) {
        if (activity == null) return;
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        Transiciones.fade(activity);
        activity.finish();
    }

    public static void alertaAcercaDe(Activity activity) {
        if (activity == null) return;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.CustomMaterialDialog);
        builder.setTitle(activity.getString(R.string.acerca_de));
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String email = currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "";
        String grupoActual = (Constantes.GRUPO_SELECCIONADO == null || Constantes.GRUPO_SELECCIONADO.isEmpty()) ? "Ninguno" : Constantes.GRUPO_SELECCIONADO;

        String mensaje = "© Paco Sotelo 2026\nPara el mundo, desde 2021 \n\nCréditos: \nLogo: Santiago Romo \n\n" +
                "Versión: 6.0.1 Armonía" + "\n\nUsuario: " + email + "\n\nGrupo Seleccionado: " + grupoActual;

        builder.setMessage(mensaje);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public static void toggleTheme(Context context) {
        if (context == null) return;
        SharedPreferences pref = context.getSharedPreferences("theme_pref", Context.MODE_PRIVATE);
        boolean isDark = pref.getBoolean("is_dark_mode", false);
        boolean newMode = !isDark;
        pref.edit().putBoolean("is_dark_mode", newMode).apply();
        AppCompatDelegate.setDefaultNightMode(newMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static void mostrarDialogoCrearGrupo(Activity activity, GruposViewModel gruposViewModel) {
        if (activity == null || gruposViewModel == null) return;

        // Recuperar usuario desde la Base de datos localmente si la variable global fue destruida en RAM
        Usuario usuarioAUsar = Constantes.usuario;
        if (usuarioAUsar == null) {
            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentFirebaseUser == null) {
                Toast.makeText(activity, "Inicia sesión primero", Toast.LENGTH_SHORT).show();
                return;
            }
            usuarioAUsar = new Usuario();
            usuarioAUsar.setUid(currentFirebaseUser.getUid());
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.CustomMaterialDialog);
        builder.setTitle(activity.getString(R.string.titulo_crear_grupo));

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_input_field, null);
        TextInputLayout inputLayout = dialogView.findViewById(R.id.dialogInputLayout);
        TextInputEditText inputEditText = dialogView.findViewById(R.id.dialogInputEditText);
        inputLayout.setHint(activity.getString(R.string.titulo_crear_grupo));

        builder.setView(dialogView);
        builder.setCancelable(true);
        builder.setNegativeButton(activity.getString(R.string.cancelar), (dialog, which) -> dialog.dismiss());
        final Usuario usuarioFinal = usuarioAUsar;
        builder.setPositiveButton(activity.getString(R.string.btn_crear), (dialog, which) -> {
            String nombreGrupo = inputEditText.getText() != null ? inputEditText.getText().toString().trim() : "";
            if (!nombreGrupo.isEmpty()) {
                gruposViewModel.crearNuevoGrupo(usuarioFinal, nombreGrupo);
            } else {
                Toast.makeText(activity, activity.getString(R.string.msg_ingresa_nombre_grupo), Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

    public static void mostrarDialogoUnirseGrupo(Activity activity, GruposViewModel gruposViewModel) {
        if (activity == null || gruposViewModel == null) return;

        Usuario usuarioAUsar = Constantes.usuario;
        if (usuarioAUsar == null) {
            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentFirebaseUser == null) {
                Toast.makeText(activity, "Inicia sesión primero", Toast.LENGTH_SHORT).show();
                return;
            }
            usuarioAUsar = new Usuario();
            usuarioAUsar.setUid(currentFirebaseUser.getUid());
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.CustomMaterialDialog);
        builder.setTitle(activity.getString(R.string.titulo_unirse_grupo));

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_input_field, null);
        TextInputLayout inputLayout = dialogView.findViewById(R.id.dialogInputLayout);
        TextInputEditText inputEditText = dialogView.findViewById(R.id.dialogInputEditText);
        inputLayout.setHint(activity.getString(R.string.msg_ingresa_codigo_grupo));
        inputEditText.setAllCaps(true);

        builder.setView(dialogView);
        builder.setCancelable(true);
        builder.setNegativeButton(activity.getString(R.string.cancelar), (dialog, which) -> dialog.dismiss());
        final Usuario usuarioFinal = usuarioAUsar;
        builder.setPositiveButton(activity.getString(R.string.btn_unirse), (dialog, which) -> {
            String codigo = inputEditText.getText() != null ? inputEditText.getText().toString().trim().toUpperCase() : "";
            if (codigo.length() == 6) {
                gruposViewModel.unirseAGrupo(usuarioFinal, codigo);
            } else {
                Toast.makeText(activity, activity.getString(R.string.msg_codigo_invalido), Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }
}
