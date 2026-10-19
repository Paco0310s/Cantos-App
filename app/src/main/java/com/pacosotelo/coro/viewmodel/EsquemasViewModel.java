package com.pacosotelo.coro.viewmodel;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.repository.EsquemasRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EsquemasViewModel extends ViewModel {
    private EsquemasRepository esquemasRepository;

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<List<Esquema>> esquemasLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();

    private String lastGrupoId = "";
    private Query activeEsquemasQuery;
    private ValueEventListener activeEsquemasListener;

    public EsquemasViewModel() {
        esquemasRepository = new EsquemasRepository();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<List<Esquema>> getEsquemas() {
        return esquemasLiveData;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<Boolean> getDeleteSuccess() {
        return deleteSuccess;
    }

    public void loadEsquemasByGrupo(String grupoId) {
        startListeningEsquemasByGrupo(grupoId);
    }

    public void refreshEsquemasByGrupo(String grupoId) {
        startListeningEsquemasByGrupo(grupoId);
    }

    public void startListeningEsquemasByGrupo(String grupoId) {
        if (grupoId == null || grupoId.trim().isEmpty()) {
            isLoading.setValue(false);
            return;
        }

        // Si ya estamos escuchando este grupo en tiempo real, no recreamos el listener
        if (grupoId.equals(lastGrupoId) && activeEsquemasListener != null) {
            isLoading.setValue(false);
            return;
        }

        stopListeningEsquemas();

        lastGrupoId = grupoId;
        if (esquemasLiveData.getValue() == null || esquemasLiveData.getValue().isEmpty()) {
            isLoading.setValue(true);
        }

        activeEsquemasQuery = esquemasRepository.getEsquemasQueryByGrupo(grupoId);
        activeEsquemasListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isLoading.setValue(false);
                List<Esquema> lista = new ArrayList<>();
                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    if (objSnap != null) {
                        try {
                            Esquema e = objSnap.getValue(Esquema.class);
                            if (e != null && e.getId() != null) {
                                lista.add(e);
                            }
                        } catch (Exception ex) {
                            Log.e("EsquemasViewModel", "Error al parsear esquema en tiempo real (ignorado para prevenir crash): " + objSnap.getKey(), ex);
                        }
                    }
                }
                sortEsquemas(lista);
                esquemasLiveData.setValue(lista);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isLoading.setValue(false);
                errorMessage.setValue("Error al escuchar esquemas en tiempo real: " + error.getMessage());
            }
        };

        activeEsquemasQuery.addValueEventListener(activeEsquemasListener);
    }

    public void stopListeningEsquemas() {
        if (activeEsquemasQuery != null && activeEsquemasListener != null) {
            esquemasRepository.removeListener(activeEsquemasQuery, activeEsquemasListener);
            activeEsquemasQuery = null;
            activeEsquemasListener = null;
        }
    }

    public void saveEsquemaSilencioso(Esquema esquema) {
        try {
            if (esquema != null) {
                esquemasRepository.saveEsquema(esquema);
            }
        } catch (Exception e) {
            Log.e("EsquemasViewModel", "Error en guardado silencioso: " + e.getMessage());
        }
    }

    public void saveEsquema(Esquema esquema) {
        isLoading.setValue(true);
        esquemasRepository.saveEsquema(esquema).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                saveSuccess.setValue(true);
            } else {
                saveSuccess.setValue(false);
                errorMessage.setValue("Error al guardar el esquema: " + (task.getException() != null ? task.getException().getMessage() : "Desconocido"));
            }
        });
    }

    public void deleteEsquema(String esquemaId) {
        isLoading.setValue(true);
        esquemasRepository.deleteEsquema(esquemaId).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                deleteSuccess.setValue(true);
            } else {
                deleteSuccess.setValue(false);
                errorMessage.setValue("Error al eliminar el esquema");
            }
        });
    }

    private void sortEsquemas(List<Esquema> lista) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            lista.sort((o1, o2) -> o1.getNombre().compareToIgnoreCase(o2.getNombre()));
        } else {
            Collections.sort(lista, (o1, o2) -> o1.getNombre().compareToIgnoreCase(o2.getNombre()));
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListeningEsquemas();
    }
}
