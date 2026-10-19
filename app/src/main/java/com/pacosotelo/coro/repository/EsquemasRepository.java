package com.pacosotelo.coro.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.modelos.Esquema;

public class EsquemasRepository {
    private FirebaseDatabase database;
    private DatabaseReference esquemasRef;

    public EsquemasRepository() {
        database = FirebaseDatabase.getInstance();
        esquemasRef = database.getReference("esquemas");
    }

    public Task<DataSnapshot> getEsquemasByGrupo(String grupoId) {
        return esquemasRef.orderByChild("grupo_id").equalTo(grupoId).get();
    }

    public Query getEsquemasQueryByGrupo(String grupoId) {
        return esquemasRef.orderByChild("grupo_id").equalTo(grupoId);
    }

    public void removeListener(Query query, ValueEventListener listener) {
        if (query != null && listener != null) {
            query.removeEventListener(listener);
        }
    }

    public Task<Void> saveEsquema(Esquema esquema) {
        return esquemasRef.child(esquema.getId()).setValue(esquema);
    }

    public Task<Void> deleteEsquema(String esquemaId) {
        return esquemasRef.child(esquemaId).removeValue();
    }
}
