package com.pacosotelo.coro.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.UUID;
import java.util.Random;
import java.util.ArrayList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.tools.AdaptadorEsquemas;
import com.pacosotelo.coro.tools.Constantes;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListaEsquemasFragment extends Fragment {
    private final List<Esquema> listaEsquemas = new ArrayList<>();
    private final List<Esquema> listaRespaldo = new ArrayList<>();
    private RecyclerView rvEsquemas;
    private AdaptadorEsquemas adapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabNuevo;
    private View noGroupContainerEsquemas;
    private Button btnCrearGrupoEsquemas, btnUnirseGrupoEsquemas;
    private TextView tvNoGroupEsquemas;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lista_esquemas, container, false);

        rvEsquemas = root.findViewById(R.id.rvEsquemas);

        progressBar = root.findViewById(R.id.pbEsquemas);

        this.fabNuevo = root.findViewById(R.id.fabNuevoEsquema);
        this.fabNuevo.setOnClickListener(v -> {
            // Verificar que el usuario tenga un grupo seleccionado antes de permitir crear un esquema
            if (Constantes.GRUPO_SELECCIONADO == null || Constantes.GRUPO_SELECCIONADO.isEmpty()) {
                Toast.makeText(getActivity(), "Debes tener un grupo seleccionado para crear un esquema", Toast.LENGTH_SHORT).show();
            } else {
                nuevoEsquema();
            }
        });

        noGroupContainerEsquemas = root.findViewById(R.id.no_group_container_esquemas);
        tvNoGroupEsquemas = root.findViewById(R.id.tvNoGroupEsquemas);
        btnCrearGrupoEsquemas = root.findViewById(R.id.btnCrearGrupoEsquemas);
        btnUnirseGrupoEsquemas = root.findViewById(R.id.btnUnirseGrupoEsquemas);

        btnCrearGrupoEsquemas.setOnClickListener(v -> {
            FirebaseUser usuarioFirebase = FirebaseAuth.getInstance().getCurrentUser();
            if (usuarioFirebase != null) {
                FirebaseDatabase.getInstance().getReference("usuarios").child(usuarioFirebase.getUid()).get()
                        .addOnSuccessListener(dataSnapshot -> {
                            if (dataSnapshot.exists()) {
                                Usuario u = dataSnapshot.getValue(Usuario.class);
                                if (u != null) mostrarDialogoCrearGrupo(u);
                            }
                        });
            }
        });

        btnUnirseGrupoEsquemas.setOnClickListener(v -> {
            FirebaseUser usuarioFirebase = FirebaseAuth.getInstance().getCurrentUser();
            if (usuarioFirebase != null) {
                FirebaseDatabase.getInstance().getReference("usuarios").child(usuarioFirebase.getUid()).get()
                        .addOnSuccessListener(dataSnapshot -> {
                            if (dataSnapshot.exists()) {
                                Usuario u = dataSnapshot.getValue(Usuario.class);
                                if (u != null) mostrarDialogoUnirseGrupo(u);
                            }
                        });
            }
        });

        rvEsquemas.setOnTouchListener((v, event) -> {
            root.findViewById(R.id.rvEsquemas).getParent()
                    .requestDisallowInterceptTouchEvent(false);
            return false;
        });


        inicializarFirebase();

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater){
        inflater.inflate(R.menu.menu_lista_esquemas,menu);

        MenuItem item = menu.findItem(R.id.buscar);

        SearchView buscador = (SearchView) item.getActionView();
        buscador.setOnQueryTextListener(oyente);
        buscador.setQueryHint(getString(R.string.buscar));

        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void inicializarFirebase() {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        DatabaseReference dr = fd.getReference("usuarios");

        progressBar.setVisibility(View.VISIBLE);

        // Consultar grupo seleccionado y cargar cantos correspondientes
        dr.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                Usuario u = dataSnapshot.getValue(Usuario.class);
                if (u != null) {
                    // Si el usuario no tiene grupos, mostrar mensaje y ocultar lista
                    if (u.getGrupos() == null || u.getGrupos().isEmpty() || u.getGrupoActual() == null || u.getGrupoActual().isEmpty()) {
                        noGroupContainerEsquemas.setVisibility(View.VISIBLE);
                        rvEsquemas.setVisibility(View.GONE);
                        fabNuevo.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Constantes.GRUPO_SELECCIONADO = null;
                    } else {
                        noGroupContainerEsquemas.setVisibility(View.GONE);
                        rvEsquemas.setVisibility(View.VISIBLE);
                        fabNuevo.setVisibility(View.VISIBLE);
                        Constantes.GRUPO_SELECCIONADO = u.getGrupoActual();
                        cargarEsquemas();
                    }
                } else {
                    Toast.makeText(getActivity(), "Error al cargar usuario", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Usuario no encontrado en la base de datos", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show());
    }

    private void cargarEsquemas() {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        DatabaseReference dr = fd.getReference("esquemas");

        final String grupoSeleccionado = Constantes.GRUPO_SELECCIONADO.trim();

        dr.orderByChild("grupo_id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaEsquemas.clear();
                listaRespaldo.clear();

                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    if(objSnap!=null) {
                        try {
                            Esquema e = objSnap.getValue(Esquema.class);
                            if (e != null && e.getGrupo_id().trim().equals(grupoSeleccionado)) {
                                e.setNombre(e.getNombre());
                                listaEsquemas.add(e);
                            }
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Error al cargar el esquema: " + objSnap.getValue(), Toast.LENGTH_SHORT).show();
                            Log.e("Error", Objects.requireNonNull(objSnap.getKey()));
                        }
                    }
                }

                // Ordenamos la lista por nombre
                Collections.sort(listaEsquemas, (o1, o2) -> o1.getNombre().compareTo(o2.getNombre()));

                adapter = new AdaptadorEsquemas(listaEsquemas, getActivity());
                rvEsquemas.setHasFixedSize(true);
                rvEsquemas.setLayoutManager(new LinearLayoutManager(getActivity()));
                rvEsquemas.setAdapter(adapter);

                listaRespaldo.addAll(listaEsquemas);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mostrarDialogoCrearGrupo(Usuario usuario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Crear nuevo grupo");

        EditText input = new EditText(requireActivity());
        input.setHint("Nombre del grupo");
        builder.setView(input);
        builder.setCancelable(true);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Crear", (dialog, which) -> {
            String nombreGrupo = input.getText().toString().trim();
            if (!nombreGrupo.isEmpty()) {
                crearGrupoEnFirebase(usuario, nombreGrupo);
            } else {
                Toast.makeText(getActivity(), "Por favor ingresa un nombre para el grupo", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alerta = builder.create();
        alerta.show();
    }

    private void crearGrupoEnFirebase(Usuario usuario, String nombreGrupo) {
        String uuid = UUID.randomUUID().toString();
        generarCodigoUnicoYCrearGrupo(usuario, nombreGrupo, uuid);
    }

    private void generarCodigoUnicoYCrearGrupo(Usuario usuario, String nombreGrupo, String uuid) {
        String codigo = generarCodigoAleatorio();

        // Verificar que el código no exista
        FirebaseDatabase.getInstance().getReference("grupos").orderByChild("codigo").equalTo(codigo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // El código ya existe, generar uno nuevo
                            generarCodigoUnicoYCrearGrupo(usuario, nombreGrupo, uuid);
                        } else {
                            // Código disponible, crear el grupo
                            crearGrupoConCodigo(usuario, nombreGrupo, uuid, codigo);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // En caso de error, intentar crear de todas formas con el código
                        crearGrupoConCodigo(usuario, nombreGrupo, uuid, codigo);
                    }
                });
    }

    private void crearGrupoConCodigo(Usuario usuario, String nombreGrupo, String uuid, String codigo) {
        com.pacosotelo.coro.modelos.Grupo nuevoGrupo = new com.pacosotelo.coro.modelos.Grupo(uuid, nombreGrupo, codigo);

        DatabaseReference refGrupos = FirebaseDatabase.getInstance().getReference("grupos").child(uuid);
        refGrupos.setValue(nuevoGrupo).addOnSuccessListener(aVoid -> {
            // Agregar el grupo al usuario
            ArrayList<String> grupos = usuario.getGrupos();
            if (grupos == null) {
                grupos = new ArrayList<>();
            }
            grupos.add(uuid);
            usuario.setGrupos(grupos);
            usuario.setGrupoActual(uuid);

            // Actualizar usuario en Firebase
            FirebaseDatabase.getInstance().getReference("usuarios").child(usuario.getUid()).setValue(usuario);

            Constantes.GRUPO_SELECCIONADO = uuid;

            // Actualizar la lista de esquemas para el nuevo grupo
            inicializarFirebase();

            Toast.makeText(getActivity(), "Grupo '" + nombreGrupo + "' creado exitosamente\nCódigo: " + codigo, Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getActivity(), "Error al crear el grupo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void mostrarDialogoUnirseGrupo(Usuario usuario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Unirse a un grupo");
        builder.setMessage("Ingresa el código de 6 caracteres del grupo");

        EditText input = new EditText(requireActivity());
        input.setHint("Código del grupo (6 caracteres)");
        input.setAllCaps(true);
        builder.setView(input);
        builder.setCancelable(true);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Unirse", (dialog, which) -> {
            String codigo = input.getText().toString().trim().toUpperCase();
            if (codigo.length() == 6) {
                buscarYUnirseAGrupo(usuario, codigo);
            } else {
                Toast.makeText(getActivity(), "El código debe tener exactamente 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alerta = builder.create();
        alerta.show();
    }

    private void buscarYUnirseAGrupo(Usuario usuario, String codigo) {
        FirebaseDatabase.getInstance().getReference("grupos").orderByChild("codigo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean encontrado = false;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    com.pacosotelo.coro.modelos.Grupo grupo = dataSnapshot.getValue(com.pacosotelo.coro.modelos.Grupo.class);
                    if (grupo != null && grupo.getCodigo().equals(codigo)) {
                        agregarGrupoAlUsuario(usuario, grupo.getUuid(), grupo.getNombre());
                        encontrado = true;
                        break;
                    }
                }
                if (!encontrado) {
                    Toast.makeText(getActivity(), "No se encontró un grupo con ese código", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error al buscar el grupo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarGrupoAlUsuario(Usuario usuario, String grupoUuid, String nombreGrupo) {
        ArrayList<String> grupos = usuario.getGrupos();
        if (grupos == null) {
            grupos = new ArrayList<>();
        }

        // Verificar que el usuario aún no esté en este grupo
        if (grupos.contains(grupoUuid)) {
            Toast.makeText(getActivity(), "Ya eres miembro de este grupo", Toast.LENGTH_SHORT).show();
            return;
        }

        grupos.add(grupoUuid);
        usuario.setGrupos(grupos);
        usuario.setGrupoActual(grupoUuid);

        // Actualizar usuario en Firebase
        FirebaseDatabase.getInstance().getReference("usuarios").child(usuario.getUid()).setValue(usuario)
                .addOnSuccessListener(aVoid -> {
                    Constantes.GRUPO_SELECCIONADO = grupoUuid;

                    // Actualizar la lista de esquemas para el nuevo grupo
                    inicializarFirebase();

                    Toast.makeText(getActivity(), "Te has unido al grupo '" + nombreGrupo + "'", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error al unirse al grupo", Toast.LENGTH_SHORT).show();
                });
    }

    private void nuevoEsquema() {
        Activity activity = getActivity();

        Intent i = new Intent(activity, NuevoEsquemaActivity.class);
        i.putExtra("getTipo", 0);
        startActivity(i);

        assert activity != null;

        activity.overridePendingTransition(R.anim.left_in,R.anim.left_out);
    }

    public static String quitaDiacriticos(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    SearchView.OnQueryTextListener oyente = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            int longitud = s.length();
            if(longitud == 0)
            {
                listaEsquemas.clear();
                listaEsquemas.addAll(listaRespaldo);
            }else{
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    List<Esquema> collecion = listaRespaldo.stream().filter
                                    (i->quitaDiacriticos(i.getNombre()).toLowerCase().contains(quitaDiacriticos(s.toLowerCase()))).
                            collect(Collectors.toList());
                    listaEsquemas.clear();
                    listaEsquemas.addAll(collecion);
                }else {
                    listaEsquemas.clear();
                    for (Esquema z: listaRespaldo) {
                        if (quitaDiacriticos(z.getNombre()).toLowerCase().contains(quitaDiacriticos(s.toLowerCase()))){
                            listaEsquemas.add(z);
                        }
                    }
                }
            }

            adapter = new AdaptadorEsquemas(listaEsquemas, getActivity());
            rvEsquemas.setHasFixedSize(true);
            rvEsquemas.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvEsquemas.setAdapter(adapter);

            return true;
        }
    };
}