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
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.repository.CantosRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CantosViewModel extends ViewModel {
    private CantosRepository cantosRepository;

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<List<Canto>> cantosLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Canto>> todosCantosLiveData = new MutableLiveData<>();
    
    private MutableLiveData<List<String>> momentosLiveData = new MutableLiveData<>();
    private MutableLiveData<List<String>> tiemposLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();

    private String lastGrupoId = "";
    private Query activeCantosQuery;
    private ValueEventListener activeCantosListener;

    public CantosViewModel() {
        cantosRepository = new CantosRepository();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<List<Canto>> getCantos() {
        return cantosLiveData;
    }

    public LiveData<List<Canto>> getTodosCantos() {
        return todosCantosLiveData;
    }

    public LiveData<List<String>> getMomentos() {
        return momentosLiveData;
    }

    public LiveData<List<String>> getTiempos() {
        return tiemposLiveData;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<Boolean> getDeleteSuccess() {
        return deleteSuccess;
    }

    public void loadCantosByGrupo(String grupoId) {
        startListeningCantosByGrupo(grupoId);
    }

    public void refreshCantosByGrupo(String grupoId) {
        startListeningCantosByGrupo(grupoId);
    }

    public void startListeningCantosByGrupo(String grupoId) {
        if (grupoId == null || grupoId.trim().isEmpty()) {
            isLoading.setValue(false);
            return;
        }

        // Si ya estamos escuchando este grupo en tiempo real, no recreamos el listener
        if (grupoId.equals(lastGrupoId) && activeCantosListener != null) {
            isLoading.setValue(false);
            return;
        }

        stopListeningCantos();

        lastGrupoId = grupoId;
        if (cantosLiveData.getValue() == null || cantosLiveData.getValue().isEmpty()) {
            isLoading.setValue(true);
        }

        activeCantosQuery = cantosRepository.getCantosQueryByGrupo(grupoId);
        activeCantosListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isLoading.setValue(false);
                List<Canto> lista = new ArrayList<>();
                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    if (objSnap != null) {
                        try {
                            Canto c = objSnap.getValue(Canto.class);
                            if (c != null && c.getId() != null) {
                                lista.add(c);
                            }
                        } catch (Exception e) {
                            Log.e("CantosViewModel", "Error al parsear canto en tiempo real (ignorado para prevenir crash): " + objSnap.getKey(), e);
                        }
                    }
                }
                sortCantos(lista);
                cantosLiveData.setValue(lista);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isLoading.setValue(false);
                errorMessage.setValue("Error al escuchar cantos en tiempo real: " + error.getMessage());
            }
        };

        activeCantosQuery.addValueEventListener(activeCantosListener);
    }

    public void stopListeningCantos() {
        if (activeCantosQuery != null && activeCantosListener != null) {
            cantosRepository.removeListener(activeCantosQuery, activeCantosListener);
            activeCantosQuery = null;
            activeCantosListener = null;
        }
    }

    public void loadAllCantos() {
        isLoading.setValue(true);
        cantosRepository.getAllCantos().addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                List<Canto> lista = new ArrayList<>();
                for (DataSnapshot objSnap : task.getResult().getChildren()) {
                    if (objSnap != null) {
                        try {
                            Canto c = objSnap.getValue(Canto.class);
                            if (c != null) {
                                lista.add(c);
                            }
                        } catch (Exception e) {
                            Log.e("CantosViewModel", "Error al parsear el canto", e);
                        }
                    }
                }
                todosCantosLiveData.setValue(lista);
            } else {
                errorMessage.setValue("Error al cargar todos los cantos");
            }
        });
    }

    public void loadMomentosYTiempos() {
        cantosRepository.getMomentos().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> lista = new ArrayList<>();
                for (DataSnapshot objSnap : task.getResult().getChildren()) {
                    Object val = objSnap.child("momento").getValue();
                    if(val != null) lista.add(val.toString());
                }
                momentosLiveData.setValue(lista);
            } else {
                Log.e("CantosViewModel", "Error cargando momentos");
            }
        });

        cantosRepository.getTiempos().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> lista = new ArrayList<>();
                for (DataSnapshot objSnap : task.getResult().getChildren()) {
                    Object val = objSnap.child("tiempo").getValue();
                    if(val != null) lista.add(val.toString());
                }
                tiemposLiveData.setValue(lista);
            } else {
                Log.e("CantosViewModel", "Error cargando tiempos");
            }
        });
    }

    public void saveCanto(Canto canto) {
        isLoading.setValue(true);
        cantosRepository.saveCanto(canto).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                saveSuccess.setValue(true);
            } else {
                saveSuccess.setValue(false);
                errorMessage.setValue("Error al guardar: " + (task.getException() != null ? task.getException().getMessage() : "Desconocido"));
            }
        });
    }

    public void deleteCanto(String cantoId) {
        isLoading.setValue(true);
        cantosRepository.deleteCanto(cantoId).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                deleteSuccess.setValue(true);
            } else {
                deleteSuccess.setValue(false);
                errorMessage.setValue("Error al eliminar el canto");
            }
        });
    }

    private void sortCantos(List<Canto> lista) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            lista.sort((c1, c2) -> c1.getNombre().compareToIgnoreCase(c2.getNombre()));
        } else {
            Collections.sort(lista, (c1, c2) -> c1.getNombre().compareToIgnoreCase(c2.getNombre()));
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListeningCantos();
    }
}
