package com.pacosotelo.coro.repository;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pacosotelo.coro.modelos.Grupo;
import com.pacosotelo.coro.modelos.Usuario;

public class GruposRepository {
    private FirebaseDatabase database;
    private DatabaseReference gruposRef;
    private DatabaseReference usuariosRef;
    private FirebaseStorage storage;

    public GruposRepository() {
        database = FirebaseDatabase.getInstance();
        gruposRef = database.getReference("grupos");
        usuariosRef = database.getReference("usuarios");
        storage = FirebaseStorage.getInstance();
    }

    public DatabaseReference getUsuariosRef() {
        return usuariosRef;
    }

    public DatabaseReference getGruposRef() {
        return gruposRef;
    }

    public void removeListener(Query query, ValueEventListener listener) {
        if (query != null && listener != null) {
            query.removeEventListener(listener);
        }
    }

    public Task<DataSnapshot> getGrupos() {
        return gruposRef.get();
    }

    public Task<DataSnapshot> getGrupoByCodigo(String codigo) {
        return gruposRef.orderByChild("codigo").equalTo(codigo).get();
    }

    public Task<Void> createGrupo(Grupo grupo) {
        return gruposRef.child(grupo.getUuid()).setValue(grupo);
    }

    public Task<Void> addMiembroToGrupo(String grupoUuid, String userUid) {
        return gruposRef.child(grupoUuid).child("miembros").child(userUid).setValue(true);
    }

    public Task<Void> removeMiembroFromGrupo(String grupoUuid, String userUid) {
        return gruposRef.child(grupoUuid).child("miembros").child(userUid).removeValue();
    }

    public Task<Void> addAdminToGrupo(String grupoUuid, String userUid) {
        return gruposRef.child(grupoUuid).child("admins").child(userUid).setValue(true);
    }

    public Task<Void> removeAdminFromGrupo(String grupoUuid, String userUid) {
        return gruposRef.child(grupoUuid).child("admins").child(userUid).removeValue();
    }

    public Task<DataSnapshot> getUsuario(String uid) {
        return usuariosRef.child(uid).get();
    }

    public Task<Void> updateUsuario(Usuario usuario) {
        return usuariosRef.child(usuario.getUid()).setValue(usuario);
    }

    public UploadTask uploadUserPhoto(String userId, Uri fileUri) {
        StorageReference photoRef = storage.getReference().child("Fotos").child("IMG-USER-" + userId);
        return photoRef.putFile(fileUri);
    }

    public Task<Uri> getPhotoDownloadUrl(String userId) {
        StorageReference photoRef = storage.getReference().child("Fotos").child("IMG-USER-" + userId);
        return photoRef.getDownloadUrl();
    }
}
