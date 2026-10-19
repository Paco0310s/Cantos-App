package com.pacosotelo.coro.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.modelos.Canto;

public class CantosRepository {
    private FirebaseDatabase database;
    private DatabaseReference cantosRef;
    private DatabaseReference momentosRef;
    private DatabaseReference tiemposRef;

    public CantosRepository() {
        database = FirebaseDatabase.getInstance();
        cantosRef = database.getReference("cantos");
        momentosRef = database.getReference("momentos");
        tiemposRef = database.getReference("tiempos");
    }

    public Task<DataSnapshot> getCantosByGrupo(String grupoId) {
        return cantosRef.orderByChild("grupo_id").equalTo(grupoId).get();
    }

    public Query getCantosQueryByGrupo(String grupoId) {
        return cantosRef.orderByChild("grupo_id").equalTo(grupoId);
    }

    public Task<DataSnapshot> getAllCantos() {
        return cantosRef.orderByChild("nombre").get();
    }

    public Query getAllCantosQuery() {
        return cantosRef.orderByChild("nombre");
    }

    public void removeListener(Query query, ValueEventListener listener) {
        if (query != null && listener != null) {
            query.removeEventListener(listener);
        }
    }

    public Task<Void> saveCanto(Canto canto) {
        return cantosRef.child(canto.getId()).setValue(canto);
    }

    public Task<Void> deleteCanto(String cantoId) {
        return cantosRef.child(cantoId).removeValue();
    }

    public Task<DataSnapshot> getMomentos() {
        return momentosRef.get();
    }

    public Task<DataSnapshot> getTiempos() {
        return tiemposRef.get();
    }
}
