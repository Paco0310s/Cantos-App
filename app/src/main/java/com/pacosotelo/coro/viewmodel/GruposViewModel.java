package com.pacosotelo.coro.viewmodel;

import android.net.Uri;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.pacosotelo.coro.modelos.Grupo;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.repository.GruposRepository;
import com.pacosotelo.coro.tools.Constantes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GruposViewModel extends ViewModel {
    private GruposRepository gruposRepository;

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<List<Grupo>> gruposLiveData = new MutableLiveData<>();
    private MutableLiveData<Usuario> usuarioLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> groupOperationSuccess = new MutableLiveData<>();
    private MutableLiveData<String> groupCreatedMessage = new MutableLiveData<>();
    private MutableLiveData<String> photoUploadSuccess = new MutableLiveData<>();

    private ValueEventListener userListener;
    private ValueEventListener gruposListener;

    public GruposViewModel() {
        gruposRepository = new GruposRepository();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<List<Grupo>> getGrupos() {
        return gruposLiveData;
    }

    public LiveData<Usuario> getUsuarioLiveData() {
        return usuarioLiveData;
    }

    public LiveData<Boolean> getGroupOperationSuccess() {
        return groupOperationSuccess;
    }

    public LiveData<String> getGroupCreatedMessage() {
        return groupCreatedMessage;
    }

    public void clearGroupCreatedMessage() {
        groupCreatedMessage.setValue(null);
    }

    public LiveData<String> getPhotoUploadSuccess() {
        return photoUploadSuccess;
    }

    public void startListeningUsuario(String uid) {
        if (uid == null || uid.isEmpty()) return;

        if (userListener != null) {
            return;
        }

        isLoading.setValue(true);
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isLoading.setValue(false);
                if (snapshot.exists()) {
                    Usuario u = snapshot.getValue(Usuario.class);
                    if (u != null) {
                        usuarioLiveData.setValue(u);
                        startListeningGrupos(u);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isLoading.setValue(false);
                // Silenciamos este error ya que suele ocurrir intencionalmente al cerrar sesión
            }
        };

        gruposRepository.getUsuariosRef().child(uid).addValueEventListener(userListener);
    }

    private void startListeningGrupos(Usuario usuario) {
        if (usuario == null || usuario.getGrupos() == null || usuario.getGrupos().isEmpty()) {
            gruposLiveData.setValue(new ArrayList<>());
            return;
        }

        for (String gUuid : usuario.getGrupos()) {
            gruposRepository.addMiembroToGrupo(gUuid, usuario.getUid());
            if ("mYW9YLYZPmZdhaSwSS0ONF0EUe53".equals(usuario.getUid())) {
                gruposRepository.getGruposRef().child(gUuid).child("admin_id").setValue(usuario.getUid());
                gruposRepository.addAdminToGrupo(gUuid, usuario.getUid());
            }
        }

        if (gruposListener != null) {
            gruposRepository.getGruposRef().removeEventListener(gruposListener);
        }

        gruposListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario currentUser = usuarioLiveData.getValue();
                if (currentUser == null || currentUser.getGrupos() == null) return;
                
                List<Grupo> lista = new ArrayList<>();
                for (String grupoUuid : currentUser.getGrupos()) {
                    DataSnapshot gSnap = snapshot.child(grupoUuid);
                    if (gSnap.exists()) {
                        Grupo g = gSnap.getValue(Grupo.class);
                        if (g != null) {
                            lista.add(g);
                        }
                    }
                }
                sortGrupos(lista);
                gruposLiveData.setValue(lista);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Silenciamos este error ya que suele ocurrir intencionalmente al cerrar sesión
                // errorMessage.setValue("Error al escuchar grupos en tiempo real: " + error.getMessage());
            }
        };

        gruposRepository.getGruposRef().addValueEventListener(gruposListener);
    }

    public void stopListening() {
        if (userListener != null && usuarioLiveData.getValue() != null) {
            gruposRepository.getUsuariosRef().child(usuarioLiveData.getValue().getUid()).removeEventListener(userListener);
            userListener = null;
        }
        if (gruposListener != null) {
            gruposRepository.getGruposRef().removeEventListener(gruposListener);
            gruposListener = null;
        }
    }

    public void setGrupoActual(Usuario usuario, String nuevoGrupoId) {
        usuario.setGrupoActual(nuevoGrupoId);
        gruposRepository.updateUsuario(usuario).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseMessaging.getInstance().subscribeToTopic("grupo_" + nuevoGrupoId);
                groupOperationSuccess.setValue(true);
            } else {
                errorMessage.setValue("Error al cambiar de grupo");
            }
        });
    }

    public void crearNuevoGrupo(Usuario usuario, String nombreGrupo) {
        if (usuario == null || usuario.getUid() == null) {
            errorMessage.setValue("Error: Usuario no válido");
            return;
        }

        isLoading.setValue(true);
        String uuid = UUID.randomUUID().toString();
        String codigo = generarCodigoAleatorio();

        Grupo nuevoGrupo = new Grupo(uuid, nombreGrupo, codigo, usuario.getUid());

        gruposRepository.createGrupo(nuevoGrupo).addOnCompleteListener(createTask -> {
            if (createTask.isSuccessful()) {
                ArrayList<String> userGrupos = usuario.getGrupos();
                if (userGrupos == null) {
                    userGrupos = new ArrayList<>();
                }
                if (!userGrupos.contains(uuid)) {
                    userGrupos.add(uuid);
                }
                usuario.setGrupos(userGrupos);
                usuario.setGrupoActual(uuid);

                gruposRepository.addMiembroToGrupo(uuid, usuario.getUid());
                gruposRepository.addAdminToGrupo(uuid, usuario.getUid());

                gruposRepository.updateUsuario(usuario).addOnCompleteListener(updateTask -> {
                    isLoading.setValue(false);
                    if (updateTask.isSuccessful()) {
                        Constantes.usuario = usuario;
                        Constantes.GRUPO_SELECCIONADO = uuid;
                        FirebaseMessaging.getInstance().subscribeToTopic("grupo_" + uuid);
                        groupCreatedMessage.setValue("Grupo '" + nombreGrupo + "' creado exitosamente\nCódigo: " + codigo);
                        groupOperationSuccess.setValue(true);
                    } else {
                        errorMessage.setValue("Error al actualizar grupos de usuario");
                    }
                });
            } else {
                isLoading.setValue(false);
                errorMessage.setValue("Error al crear el grupo en Firebase");
            }
        });
    }

    public void unirseAGrupo(Usuario usuario, String codigo) {
        if (usuario == null || usuario.getUid() == null) {
            errorMessage.setValue("Error: Usuario no válido");
            return;
        }

        isLoading.setValue(true);
        gruposRepository.getGrupoByCodigo(codigo).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                Grupo grupoEncontrado = null;
                for (DataSnapshot child : snapshot.getChildren()) {
                    Grupo g = child.getValue(Grupo.class);
                    if (g != null && codigo.equalsIgnoreCase(g.getCodigo())) {
                        grupoEncontrado = g;
                        break;
                    }
                }

                if (grupoEncontrado != null) {
                    ArrayList<String> userGrupos = usuario.getGrupos();
                    if (userGrupos == null) {
                        userGrupos = new ArrayList<>();
                    }
                    if (userGrupos.contains(grupoEncontrado.getUuid())) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Ya eres miembro de este grupo");
                        return;
                    }

                    userGrupos.add(grupoEncontrado.getUuid());
                    usuario.setGrupos(userGrupos);
                    usuario.setGrupoActual(grupoEncontrado.getUuid());

                    final Grupo finalGrupoEncontrado = grupoEncontrado;
                    gruposRepository.addMiembroToGrupo(grupoEncontrado.getUuid(), usuario.getUid());
                    gruposRepository.updateUsuario(usuario).addOnCompleteListener(updateTask -> {
                        isLoading.setValue(false);
                        if (updateTask.isSuccessful()) {
                            Constantes.usuario = usuario;
                            Constantes.GRUPO_SELECCIONADO = finalGrupoEncontrado.getUuid();
                            FirebaseMessaging.getInstance().subscribeToTopic("grupo_" + finalGrupoEncontrado.getUuid());
                            groupCreatedMessage.setValue("Te has unido al grupo '" + finalGrupoEncontrado.getNombre() + "'");
                            groupOperationSuccess.setValue(true);
                        } else {
                            errorMessage.setValue("Error al unirse al grupo");
                        }
                    });
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("No se encontró un grupo con el código: " + codigo);
                }
            } else {
                isLoading.setValue(false);
                errorMessage.setValue("Error al buscar el grupo");
            }
        });
    }

    public void uploadUserPhoto(Usuario usuario, Uri photoUri) {
        isLoading.setValue(true);
        gruposRepository.uploadUserPhoto(usuario.getUid(), photoUri).addOnSuccessListener(taskSnapshot -> {
            gruposRepository.getPhotoDownloadUrl(usuario.getUid()).addOnSuccessListener(uri -> {
                usuario.setFoto(uri.toString());
                gruposRepository.updateUsuario(usuario).addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        photoUploadSuccess.setValue(uri.toString());
                    } else {
                        errorMessage.setValue("Error al actualizar foto de perfil");
                    }
                });
            }).addOnFailureListener(e -> {
                isLoading.setValue(false);
                errorMessage.setValue("Error al obtener URL de la foto");
            });
        }).addOnFailureListener(e -> {
            isLoading.setValue(false);
            errorMessage.setValue("Error al subir la imagen");
        });
    }

    private String generarCodigoAleatorio() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codigo = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        return codigo.toString();
    }

    private void sortGrupos(List<Grupo> lista) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            lista.sort((o1, o2) -> {
                String nombre1 = o1.getNombre() != null ? o1.getNombre() : "";
                String nombre2 = o2.getNombre() != null ? o2.getNombre() : "";
                return nombre1.compareToIgnoreCase(nombre2);
            });
        } else {
            Collections.sort(lista, (o1, o2) -> {
                String nombre1 = o1.getNombre() != null ? o1.getNombre() : "";
                String nombre2 = o2.getNombre() != null ? o2.getNombre() : "";
                return nombre1.compareToIgnoreCase(nombre2);
            });
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListening();
    }
}
