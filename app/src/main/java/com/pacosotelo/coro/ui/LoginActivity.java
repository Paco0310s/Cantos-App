package com.pacosotelo.coro.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.tools.Transiciones;
import com.pacosotelo.coro.viewmodel.AuthViewModel;

import java.util.Objects;

import com.pacosotelo.coro.databinding.ActivityLoginBinding;

// Clase que nos servirá para iniciar sesión;
public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    // Constantes para iniciar sesión con Google
    private static final String TAG_GOOGLE = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    // Cliente de Google
    private GoogleSignInClient mGoogleSignInClient;
    // ViewModel para la autenticación
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Creamos nuestro GoogleSignInOptions para poder obtener el cliente
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Obtenemos el cliente de google
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Inicializamos el ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observamos el estado de carga
        authViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                // Podríamos mostrar un ProgressBar aquí
                binding.bIniciarSesion.setEnabled(false);
                binding.btngmail.setEnabled(false);
            } else {
                binding.bIniciarSesion.setEnabled(true);
                binding.btngmail.setEnabled(true);
            }
        });

        // Observamos errores
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                new MaterialAlertDialogBuilder(this, R.style.CustomMaterialDialog)
                    .setTitle("Error de Autenticación")
                    .setMessage(error)
                    .setPositiveButton("Aceptar", null)
                    .show();
            }
        });

        // Observamos éxito en inicio de sesión
        authViewModel.getUserAuthenticated().observe(this, usuario -> {
            if (usuario != null) {
                Toast.makeText(this, getString(R.string.bienvenida) + " " + usuario.getNombre(), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(LoginActivity.this, MenuLateralActivity.class);
                i.putExtra("usuario", usuario);
                startActivity(i);
                Transiciones.fade(this);
                finish();
            }
        });

        // Observamos el estado de restablecer contraseña
        authViewModel.getPasswordResetStatus().observe(this, status -> {
            if (status != null && !status.isEmpty()) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            }
        });

        // Llamamos este metodo para cambiarle el texto Sign In a Google
        setGoogleButtonText(binding.btngmail, getString(R.string.google));

        // Si presionamos el boton inicar con google
        binding.btngmail.setOnClickListener(v -> iniciarConGoogle());

        // Si presionamos el boton de iniciar sesión
        binding.bIniciarSesion.setOnClickListener(v -> iniciarSesion());

        // Si presionamos el text de registrarse
        binding.tvRegistrate.setOnClickListener(v -> registrarse());

        binding.tvResetear.setOnClickListener(v -> {
            MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this, R.style.CustomMaterialDialog);
            alert.setTitle("Resetear contraseña");

            View inputView = getLayoutInflater().inflate(R.layout.dialog_input_field, null);
            TextInputLayout inputLayout = inputView.findViewById(R.id.dialogInputLayout);
            TextInputEditText inputEditText = inputView.findViewById(R.id.dialogInputEditText);
            inputLayout.setHint("Ingresa tu correo electrónico");
            inputEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            alert.setView(inputView);
            alert.setPositiveButton("Aceptar", (dialog, whichButton) -> {
                String correo = inputEditText.getText() != null ? inputEditText.getText().toString().trim() : "";

                if(TextUtils.isEmpty(correo)) {
                    Toast.makeText(LoginActivity.this, "Ingresa tu correo electrónico", Toast.LENGTH_SHORT).show();
                    return;
                }

                authViewModel.sendPasswordResetEmail(correo);
            });
            alert.setNegativeButton("Cancelar", null);

            alert.show();
        });
    }

    // Metodo que nos ayudará a obtener la cuenta de google
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Si queremos iniciar con Google
        if (requestCode == RC_SIGN_IN) {
            if (data == null) {
                Toast.makeText(this, "Google Sign-In: Sin datos recibidos (ResultCode: " + resultCode + ")", Toast.LENGTH_LONG).show();
                return;
            }

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Obtenemos la cuenta
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    Log.d(TAG_GOOGLE, "firebaseAuthWithGoogle:" + account.getId());
                    // Y mandamos el token para autenticarse con google
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Toast.makeText(this, "Error: No se pudo obtener el ID Token de Google (ResultCode: " + resultCode + ")", Toast.LENGTH_LONG).show();
                }
            } catch (ApiException e) {
                Log.w(TAG_GOOGLE, "Google sign in failed. Code: " + e.getStatusCode(), e);
                String errorDetails;
                switch (e.getStatusCode()) {
                    case 10:
                        errorDetails = "DEVELOPER_ERROR: Revisa que el SHA-1 registrado en Firebase Console coincida con esta app y descarga de nuevo el google-services.json.";
                        break;
                    case 12500:
                        errorDetails = "SIGN_IN_FAILED: Revisa la pantalla de consentimiento de OAuth en Google Cloud Console.";
                        break;
                    case 12501:
                        errorDetails = "SIGN_IN_CANCELLED: Inicio de sesión cancelado por el usuario.";
                        break;
                    case 7:
                        errorDetails = "NETWORK_ERROR: Revisa tu conexión a internet.";
                        break;
                    default:
                        errorDetails = e.getMessage() != null ? e.getMessage() : "Error desconocido";
                        break;
                }
                new AlertDialog.Builder(this)
                    .setTitle("Google Sign-In (Código " + e.getStatusCode() + ")")
                    .setMessage(errorDetails)
                    .setPositiveButton("Aceptar", null)
                    .show();
            }
        }
    }

    // Metodo que autenticará la cuenta de google
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        authViewModel.signInWithCredential(credential);
    }

    // Metodo para iniciar sesión con google
    private void iniciarConGoogle() {
        // Hacemos un intent a la API de Google para obtener la cuenta
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Metodo para iniciar sesión con correo y contraseña
    public void iniciarSesion(){
        // Obtenemos el correo y la contraseña de sus respectivos campos
        final String correo = Objects.requireNonNull(binding.etCorreo.getText()).toString();
        final String pass = Objects.requireNonNull(binding.etPass.getText()).toString();

        // Si el correo está vacío
        if (TextUtils.isEmpty(correo)){
            // Le mandamos un aviso al usuario para que introduzca su correo
            Toast.makeText(LoginActivity.this, R.string.introduce_correo,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // Si la contraseña está vacía
        if (TextUtils.isEmpty(pass)){
            // Le mandamos un aviso al usuario para que introduzca su contraseña
            Toast.makeText(LoginActivity.this, R.string.introduce_contrasena,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Verficamos que la contraseña sea mayor de 8 carácteres
        if(pass.length() < 8) {
            // Si no, mandamos un aviso al usuario
            Toast.makeText(LoginActivity.this, R.string.minimo_ocho_caracteres,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //Autenticación de usuario mediante correo y contraseña
        authViewModel.signInWithEmail(correo, pass);
    }

    // Si el usuario decidió registrarse lo mandamos a esa activity
    public void registrarse(){
        Intent i = new Intent(LoginActivity.this, RegistrarseActivity.class);
        startActivity(i);
        Transiciones.left(this);
        finish();
    }



    // Metodo para cambiarle el texto Sign In a Google al boton de Google
    protected void setGoogleButtonText(SignInButton signInButton, String texto) {
        // Encontrar el texto que esta dentro del boton
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);
            // Si es un text view
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(texto); // Le cambiamos el texto
                return;
            }
        }
    }
}