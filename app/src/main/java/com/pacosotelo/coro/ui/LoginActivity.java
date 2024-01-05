package com.pacosotelo.coro.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Usuario;
import java.util.Objects;

// Clase que nos servirá para iniciar sesión;
public class LoginActivity extends AppCompatActivity {
    // Constantes para iniciar sesión con Google
    private static final String TAG_GOOGLE = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    // Cliente de Google
    private GoogleSignInClient mGoogleSignInClient;
    // Cajas de texto del correo y contraseña
    private TextInputEditText etCorreo, etPass;
    // FirebaseAuth para la autenticación de usuarios
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Creamos nuestro GoogleSignInOptions para poder obtener el cliente
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Obtenemos el cliente de google
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Obtenemos la instancia de FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Instanciamos los componentes visuales
        SignInButton signInButton = findViewById(R.id.btngmail);
        TextView tvRegistrarse = findViewById(R.id.tvRegistrate);
        TextView tvResetear = findViewById(R.id.tvResetear);
        Button bIniciarSesion = findViewById(R.id.bIniciarSesion);
        etCorreo = findViewById(R.id.etCorreo);
        etPass = findViewById(R.id.etPass);

        // Llamamos este metodo para cambiarle el texto Sign In a Google
        setGoogleButtonText(signInButton, getString(R.string.google));

        // Si presionamos el boton inicar con google
        signInButton.setOnClickListener(v -> iniciarConGoogle());

        // Si presionamos el boton de iniciar sesión
        bIniciarSesion.setOnClickListener(v -> iniciarSesion());

        // Si presionamos el text de registrarse
        tvRegistrarse.setOnClickListener(v -> registrarse());

        tvResetear.setOnClickListener(v -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            final EditText edittext = new EditText(this);
            alert.setMessage("Ingresa tu correo electronico");
            alert.setTitle("Resetear contraseña");

            alert.setView(edittext);

            alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String correo = edittext.getText().toString();

                    if(TextUtils.isEmpty(correo)) {
                        Toast.makeText(LoginActivity.this, "Ingresa tu correo electronico", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    auth.sendPasswordResetEmail(correo).addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Te hemos enviado un correo", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Error al enviar el correo", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

            alert.show();
        });
    }

    // Metodo que nos ayudará a obtener la cuenta de google
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Si queremos iniciar con Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Obtenemos la cuenta
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG_GOOGLE, "firebaseAuthWithGoogle:" + account.getId());
                // Y mandamos el token para autenticarse con google
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Si no lo mostramos en consola
                Log.w(TAG_GOOGLE, "Google sign in failed", e);
            }
        }
    }

    // Metodo que autenticará la cuenta de google
    private void firebaseAuthWithGoogle(String idToken) {
        // Obtenemos las credenciales de Googla
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        // E iniciamos sesión con esas credenciales
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    // Si se pudo iniciar sesión
                    if (task.isSuccessful()) {
                        Log.d(TAG_GOOGLE, "signInWithCredential:success");
                        // Llamamos el metodo ingresado como un nuevo usuario
                        ingresado(true);
                    } else {
                        // Si no, mandamos un error
                        Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, R.string.error_credenciales_google,
                                Toast.LENGTH_SHORT).show();
                    }
                });
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
        final String correo = Objects.requireNonNull(etCorreo.getText()).toString();
        final String pass = Objects.requireNonNull(etPass.getText()).toString();

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
        auth.signInWithEmailAndPassword(correo, pass).addOnCompleteListener(
                LoginActivity.this, task -> {
                    // Si es correcto
                    if (task.isSuccessful()){
                        // Llamamos el metodo ingresado como un usuario ya registrado
                        ingresado(false);
                    } else {
                        // Si no, mandamos un aviso de contraseña o correo invalido
                        Toast.makeText(LoginActivity.this, R.string.datos_incorrectos,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Si el usuario decidió registrarse lo mandamos a esa activity
    public void registrarse(){
        Intent i = new Intent(LoginActivity.this, RegistrarseActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.left_in,R.anim.left_out);
        finish();
    }

    // metodo que ejecutará al haber ingresado
    // Bandera nuevo usuario
    private void ingresado(boolean nuevo) {
        // Obtenemos el usuario actual
        FirebaseUser currentUser = auth.getCurrentUser();

        // Obtenemos la referencia usuarios de la base de datos
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("usuarios");

        // Si el usuario actual es nulo, retornamos
        if (currentUser == null) return;

        // Si es un nuevo usuario
        if(nuevo) {
            // Suponemos que es de google
            // Creamos un objeto usuario y le asignamos los datos de google
            Usuario usuario = new Usuario();
            usuario.setUid(currentUser.getUid());
            usuario.setNombre(currentUser.getDisplayName());
            usuario.setEmail(currentUser.getEmail());
            usuario.setNumber_phone(currentUser.getPhoneNumber());

            // Si tiene foto le asignamos el link al objeto
            if (currentUser.getPhotoUrl() != null) {
                usuario.setFoto(currentUser.getPhotoUrl().toString());
            }

            // Lo escribimos en la base de datos
            dr.child(currentUser.getUid()).setValue(usuario);

            // Mandamos un mensaje de bienvenida
            Toast.makeText(LoginActivity.this, getString(R.string.bienvenida) +
                    " " + usuario.getNombre(), Toast.LENGTH_SHORT).show();

            // Y lo redirigimos al menu principal de la app
            Intent i = new Intent(LoginActivity.this, MenuLateralActivity.class);
            i.putExtra("usuario", usuario); // Mandamos el usuario
            startActivity(i);
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            finish();

        } else { // Si es un usuario previamente registrado
            // Intetamos obtener el usuario de la base de datos
            dr.child(currentUser.getUid()).get().addOnCompleteListener(task -> {
                // Si se pudo
                if (task.isSuccessful()) {
                    // Obtenemos el usuario
                    Usuario usuario = task.getResult().getValue(Usuario.class);

                    // Mandamos un mensaje de bienvenida
                    assert usuario != null;
                    Toast.makeText(LoginActivity.this, getString(R.string.bienvenida) +
                                    " " + usuario.getNombre(), Toast.LENGTH_SHORT).show();

                    // Redirigimos al usuario al menu principal
                    Intent i = new Intent(LoginActivity.this,
                            MenuLateralActivity.class);
                    i.putExtra("usuario", usuario); // Mandamos el usuario
                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    finish();
                } else {
                    // Si no supo obtener la cuenta mandamos un error
                    Toast.makeText(LoginActivity.this, R.string.error_cuenta +
                                    ": " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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