package com.pacosotelo.coro.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.modelos.Usuario;

public class AuthRepository {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("usuarios");
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public Task<AuthResult> signInWithEmail(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> createUserWithEmail(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    public Task<AuthResult> signInWithCredential(AuthCredential credential) {
        return auth.signInWithCredential(credential);
    }

    public void signOut() {
        auth.signOut();
    }

    public Task<Void> saveUserInDatabase(Usuario usuario) {
        return usersRef.child(usuario.getUid()).setValue(usuario);
    }

    public Task<DataSnapshot> getUserFromDatabase(String uid) {
        return usersRef.child(uid).get();
    }
}
