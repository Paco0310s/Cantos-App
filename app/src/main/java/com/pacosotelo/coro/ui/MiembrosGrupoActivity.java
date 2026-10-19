package com.pacosotelo.coro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Grupo;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.tools.AdaptadorMiembros;
import com.pacosotelo.coro.tools.Constantes;
import com.pacosotelo.coro.tools.Transiciones;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MiembrosGrupoActivity extends AppCompatActivity {

    private Grupo grupo;
    private RecyclerView rvMiembros;
    private ProgressBar pbMiembros;
    private AdaptadorMiembros adaptador;
    private List<Usuario> listaMiembros = new ArrayList<>();
    private List<String> listaAdmins = new ArrayList<>();
    private FirebaseUser currentUser;
    private ValueEventListener miembrosListener;
    private ValueEventListener adminsListener;
    private DatabaseReference grupoMiembrosRef;
    private DatabaseReference grupoAdminsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miembros_grupo);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                regresar();
            }
        });

        grupo = (Grupo) getIntent().getSerializableExtra("grupo");
        if (grupo == null) {
            Toast.makeText(this, "Error al cargar el grupo", Toast.LENGTH_SHORT).show();
            regresar();
            return;
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Detalles del Grupo");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView tvNombreGrupoHeader = findViewById(R.id.tvNombreGrupoHeader);
        TextView tvCodigoGrupoHeader = findViewById(R.id.tvCodigoGrupoHeader);
        MaterialButton bCompartirCodigo = findViewById(R.id.bCompartirCodigo);

        if (tvNombreGrupoHeader != null) tvNombreGrupoHeader.setText(grupo.getNombre());
        if (tvCodigoGrupoHeader != null) tvCodigoGrupoHeader.setText("Código: " + (grupo.getCodigo() != null ? grupo.getCodigo() : "Sin código"));

        if (bCompartirCodigo != null) {
            bCompartirCodigo.setOnClickListener(v -> {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, "¡Únete a nuestro coro " + grupo.getNombre() + " en la app Cantos! Código de grupo: " + grupo.getCodigo());
                startActivity(Intent.createChooser(sendIntent, "Invitar al grupo vía..."));
            });
        }

        rvMiembros = findViewById(R.id.rvMiembros);
        pbMiembros = findViewById(R.id.pbMiembros);

        rvMiembros.setLayoutManager(new LinearLayoutManager(this));

        cargarAdminsYMiembros();
    }

    private void cargarAdminsYMiembros() {
        pbMiembros.setVisibility(View.VISIBLE);

        grupoAdminsRef = FirebaseDatabase.getInstance().getReference("grupos")
                .child(grupo.getUuid())
                .child("admins");

        if (grupo.getAdmin_id() != null) {
            grupoAdminsRef.child(grupo.getAdmin_id()).setValue(true);
        }

        adminsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaAdmins.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (child.getKey() != null) {
                            listaAdmins.add(child.getKey());
                        }
                    }
                }
                if (grupo.getAdmin_id() != null && !listaAdmins.contains(grupo.getAdmin_id())) {
                    listaAdmins.add(grupo.getAdmin_id());
                }

                adaptador = new AdaptadorMiembros(
                        MiembrosGrupoActivity.this,
                        listaMiembros,
                        listaAdmins,
                        grupo.getAdmin_id()
                );
                rvMiembros.setAdapter(adaptador);

                cargarMiembrosEnTiempoReal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                cargarMiembrosEnTiempoReal();
            }
        };

        grupoAdminsRef.addValueEventListener(adminsListener);
    }

    private void cargarMiembrosEnTiempoReal() {
        grupoMiembrosRef = FirebaseDatabase.getInstance().getReference("grupos")
                .child(grupo.getUuid())
                .child("miembros");

        if (grupo.getAdmin_id() != null) {
            grupoMiembrosRef.child(grupo.getAdmin_id()).setValue(true);
        }
        if (currentUser != null) {
            grupoMiembrosRef.child(currentUser.getUid()).setValue(true);
        }

        miembrosListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> uids = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (child.getKey() != null) {
                            uids.add(child.getKey());
                        }
                    }
                }

                if (uids.isEmpty()) {
                    if (currentUser != null) {
                        uids.add(currentUser.getUid());
                    }
                    if (grupo.getAdmin_id() != null && !uids.contains(grupo.getAdmin_id())) {
                        uids.add(grupo.getAdmin_id());
                    }
                }

                DatabaseReference usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");
                AtomicInteger pending = new AtomicInteger(uids.size());
                listaMiembros.clear();

                for (String uid : uids) {
                    usuariosRef.child(uid).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            Usuario u = task.getResult().getValue(Usuario.class);
                            if (u != null) {
                                synchronized (listaMiembros) {
                                    boolean yaExiste = false;
                                    for (Usuario existente : listaMiembros) {
                                        if (existente.getUid().equals(u.getUid())) {
                                            yaExiste = true;
                                            break;
                                        }
                                    }
                                    if (!yaExiste) {
                                        listaMiembros.add(u);
                                    }
                                }
                            }
                        }

                        if (pending.decrementAndGet() == 0) {
                            ordenarYMostrarMiembros();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbMiembros.setVisibility(View.GONE);
                Toast.makeText(MiembrosGrupoActivity.this, "Error al cargar miembros: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        grupoMiembrosRef.addValueEventListener(miembrosListener);
    }

    private void ordenarYMostrarMiembros() {
        listaMiembros.sort((u1, u2) -> u1.getNombre().compareToIgnoreCase(u2.getNombre()));

        Usuario adminUser = null;
        for (int i = 0; i < listaMiembros.size(); i++) {
            if (listaMiembros.get(i).getUid().equals(grupo.getAdmin_id())) {
                adminUser = listaMiembros.remove(i);
                break;
            }
        }
        if (adminUser != null) {
            listaMiembros.add(0, adminUser);
        }

        if (adaptador != null) {
            adaptador.notifyDataSetChanged();
        }
        pbMiembros.setVisibility(View.GONE);
    }

    private void nombrarAdmin(Usuario miembro) {
        FirebaseDatabase.getInstance().getReference("grupos")
                .child(grupo.getUuid())
                .child("admins")
                .child(miembro.getUid())
                .setValue(true)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, miembro.getNombre() + " ahora es Administrador", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al nombrar administrador", Toast.LENGTH_SHORT).show());
    }

    private void quitarAdmin(Usuario miembro) {
        FirebaseDatabase.getInstance().getReference("grupos")
                .child(grupo.getUuid())
                .child("admins")
                .child(miembro.getUid())
                .removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Permiso de administrador retirado", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al retirar administrador", Toast.LENGTH_SHORT).show());
    }

    private void expulsarMiembro(Usuario miembro) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomMaterialDialog);
        builder.setTitle("Expulsar miembro");
        builder.setMessage("¿Estás seguro de que quieres expulsar a " + miembro.getNombre() + " del grupo?");
        builder.setPositiveButton("Expulsar", (dialog, which) -> {
            pbMiembros.setVisibility(View.VISIBLE);
            
            ArrayList<String> gruposDelUsuario = miembro.getGrupos();
            if (gruposDelUsuario != null) {
                gruposDelUsuario.remove(grupo.getUuid());
                miembro.setGrupos(gruposDelUsuario);
                
                if (grupo.getUuid().equals(miembro.getGrupoActual())) {
                    miembro.setGrupoActual(gruposDelUsuario.isEmpty() ? "" : gruposDelUsuario.get(0));
                }

                FirebaseDatabase.getInstance().getReference("grupos")
                        .child(grupo.getUuid())
                        .child("miembros")
                        .child(miembro.getUid())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference("grupos")
                        .child(grupo.getUuid())
                        .child("admins")
                        .child(miembro.getUid())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference("usuarios").child(miembro.getUid()).setValue(miembro)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, miembro.getNombre() + " removido del grupo", Toast.LENGTH_SHORT).show();
                            if (currentUser != null && miembro.getUid().equals(currentUser.getUid())) {
                                Constantes.usuario = miembro;
                                Constantes.GRUPO_SELECCIONADO = miembro.getGrupoActual();
                                regresar();
                            }
                        })
                        .addOnFailureListener(e -> {
                            pbMiembros.setVisibility(View.GONE);
                            Toast.makeText(this, "Error al expulsar", Toast.LENGTH_SHORT).show();
                        });
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            regresar();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (grupoMiembrosRef != null && miembrosListener != null) {
            grupoMiembrosRef.removeEventListener(miembrosListener);
        }
        if (grupoAdminsRef != null && adminsListener != null) {
            grupoAdminsRef.removeEventListener(adminsListener);
        }
    }

    private void regresar() {
        finish();
        Transiciones.right(this);
    }
}
