package com.pacosotelo.coro.viewmodel;

import android.os.Build;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.repository.AuthRepository;

import java.time.LocalDateTime;

public class AuthViewModel extends ViewModel {
    private AuthRepository authRepository;

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Usuario> userAuthenticated = new MutableLiveData<>();
    private MutableLiveData<String> passwordResetStatus = new MutableLiveData<>();

    public AuthViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Usuario> getUserAuthenticated() {
        return userAuthenticated;
    }

    public LiveData<String> getPasswordResetStatus() {
        return passwordResetStatus;
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return authRepository.getCurrentUser();
    }
    
    public void signInWithEmail(String email, String password) {
        isLoading.setValue(true);
        authRepository.signInWithEmail(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    handleExistingUser();
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue(traducirErrorFirebase(task.getException()));
                }
            });
    }

    public void signInWithCredential(AuthCredential credential) {
        isLoading.setValue(true);
        authRepository.signInWithCredential(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser currentUser = authRepository.getCurrentUser();
                    if (currentUser != null && currentUser.getMetadata() != null) {
                        long creationTime = currentUser.getMetadata().getCreationTimestamp();
                        long lastSignInTime = currentUser.getMetadata().getLastSignInTimestamp();
                        boolean isNew = (lastSignInTime - creationTime) < 1000;
                        if (isNew) {
                            handleNewUser(currentUser, null, null);
                        } else {
                            handleExistingUser();
                        }
                    } else {
                        handleExistingUser();
                    }
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue(traducirErrorFirebase(task.getException()));
                }
            });
    }

    public void createUserWithEmail(String name, String email, String password, String phone) {
        isLoading.setValue(true);
        authRepository.createUserWithEmail(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser currentUser = authRepository.getCurrentUser();
                    if (currentUser != null) {
                        handleNewUser(currentUser, name, phone);
                    } else {
                        isLoading.setValue(false);
                        errorMessage.setValue("Error al crear usuario: No se pudo obtener la sesión");
                    }
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue(traducirErrorFirebase(task.getException()));
                }
            });
    }

    private String traducirErrorFirebase(Exception e) {
        if (e == null) return "Error desconocido";
        String msg = e.getMessage();
        if (msg == null) return "Error de red o conexión";

        if (msg.contains("WEAK_PASSWORD")) return "La contraseña debe tener al menos 6 caracteres.";
        if (msg.contains("INVALID_EMAIL")) return "El formato del correo electrónico no es válido.";
        if (msg.contains("EMAIL_ALREADY_IN_USE") || msg.contains("email address is already in use")) return "Este correo electrónico ya está registrado.";
        if (msg.contains("INVALID_LOGIN_CREDENTIALS") || msg.contains("WRONG_PASSWORD") || msg.contains("USER_NOT_FOUND")) return "Correo o contraseña incorrectos.";
        if (msg.contains("NETWORK_REQUEST_FAILED")) return "No hay conexión a internet.";
        
        return "Fallo en la autenticación: " + msg;
    }

    public void sendPasswordResetEmail(String email) {
        isLoading.setValue(true);
        authRepository.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                isLoading.setValue(false);
                if (task.isSuccessful()) {
                    passwordResetStatus.setValue("Te hemos enviado un correo");
                } else {
                    passwordResetStatus.setValue("Error al enviar el correo");
                }
            });
    }

    private void handleNewUser(FirebaseUser firebaseUser, String customName, String customPhone) {
        Usuario usuario = new Usuario();
        usuario.setUid(firebaseUser.getUid());
        
        usuario.setNombre(customName != null ? customName : firebaseUser.getDisplayName());
        usuario.setEmail(firebaseUser.getEmail());
        usuario.setNumber_phone(customPhone != null ? customPhone : firebaseUser.getPhoneNumber());

        LocalDateTime fechaActual = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fechaActual = LocalDateTime.now();
        }
        usuario.setFecha_creacion(fechaActual != null ? fechaActual.toString() : "");

        if (firebaseUser.getPhotoUrl() != null) {
            usuario.setFoto(firebaseUser.getPhotoUrl().toString());
        }

        authRepository.saveUserInDatabase(usuario)
            .addOnSuccessListener(aVoid -> {
                Log.d("FIREBASE_TRACE", "=====================> NUEVO USUARIO CREADO CON ÉXITO: " + firebaseUser.getUid());
                isLoading.setValue(false);
                userAuthenticated.setValue(usuario);
            })
            .addOnFailureListener(e -> {
                Log.e("FIREBASE_TRACE", "=====================> ERROR CREANDO NUEVO USUARIO: " + firebaseUser.getUid(), e);
                isLoading.setValue(false);
                errorMessage.setValue("Error al guardar datos del usuario");
            });
    }

    public void handleExistingUser() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            isLoading.setValue(false);
            errorMessage.setValue("No hay sesión activa");
            return;
        }

        authRepository.getUserFromDatabase(currentUser.getUid())
            .addOnCompleteListener(task -> {
                isLoading.setValue(false);
                if (task.isSuccessful() && task.getResult().exists()) {
                    Usuario usuario = task.getResult().getValue(Usuario.class);
                    if (usuario != null) {
                        userAuthenticated.setValue(usuario);
                    } else {
                        errorMessage.setValue("Error al parsear el usuario");
                    }
                } else {
                    errorMessage.setValue("Error de cuenta: " + (task.getException() != null ? task.getException().getMessage() : "Desconocido"));
                }
            });
    }

    public void signOut() {
        authRepository.signOut();
    }
}