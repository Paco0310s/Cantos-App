package com.pacosotelo.coro.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.User;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG_GOOGLE = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth auth;
    private Pattern patron1 = Pattern.compile("([a-z0-9]+(\\.?[a-z0-9])*)+@(([a-z]+)\\.([a-z]+))+");
    private Pattern patron2 = Pattern.compile("^(?=.*[0-9])"
            + "(?=.*[a-z])(?=.*[A-Z])"
            + "(?=.*[@#*._$%^&+=])"
            + "(?=\\S+$).{8,20}$");
    private Matcher match1;
    private Matcher match2;
    private TextInputEditText etCorreo, etPass;
    private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Objects.requireNonNull(getSupportActionBar()).hide();

        etCorreo = findViewById(R.id.etCorreo);
        etPass = findViewById(R.id.etPass);
        signInButton = findViewById(R.id.btngmail);

        setGoogleButtonText(signInButton, "Google");

        signInButton.setOnClickListener(v -> iniciarConGoogle());

        findViewById(R.id.bIniciarSesion).setOnClickListener(v -> iniciarSesion());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        ingresado(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG_GOOGLE, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                Log.w(TAG_GOOGLE, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG_GOOGLE, "signInWithCredential:success");
                        FirebaseUser user = auth.getCurrentUser();
                        ingresado(user);
                    } else {
                        Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.getException());
                        ingresado(null);
                    }
                });
    }

    private void iniciarConGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void ingresado(FirebaseUser user) {
        if (user != null) {

            User newUser = new User();
            newUser.setEmail(user.getEmail());
            if(user.getPhotoUrl() == null) newUser.setFoto("");
            else newUser.setFoto(user.getPhotoUrl().toString());
            newUser.setNombre(user.getDisplayName());
            newUser.setUid(user.getUid());
            if(user.getPhoneNumber() == null) newUser.setNumber_phone("");
            else newUser.setNumber_phone(user.getPhoneNumber());

            DatabaseReference dr = FirebaseDatabase.getInstance().getReference("users");
            dr.child(newUser.getUid()).setValue(newUser);

            Intent i = new Intent(LoginActivity.this, ListaCantosActivity.class);
            startActivity(i);
            finish();
        }
    }

    public void iniciarSesion(){
        String email = etCorreo.getText().toString();
        final String contra = etPass.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(LoginActivity.this, "Introduce tu correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(contra)){
            Toast.makeText(LoginActivity.this, "Introduce tu contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        //Validación de correo
        match1 = patron1.matcher(email);
        if (!match1.find()){
            Toast.makeText(LoginActivity.this, "Formato del correo inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        //Validación de contraseña
        match2 = patron2.matcher(contra);
        if (!match2.find()){
            Toast.makeText(LoginActivity.this, "Formato de la contraseña inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        //Autenticación de usuario
        auth.signInWithEmailAndPassword(email, contra).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = auth.getCurrentUser();
                    ingresado(user);
                } else {
                    Toast.makeText(LoginActivity.this, "Datos incorrectos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void setGoogleButtonText(SignInButton signInButton, String buttonText) {
        // Encontrar el texto que esta dentro del boton
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }
}