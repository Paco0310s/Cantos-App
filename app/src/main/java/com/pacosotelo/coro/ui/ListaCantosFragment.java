package com.pacosotelo.coro.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.modelos.Grupo;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.tools.AdaptadorCantos;
import com.pacosotelo.coro.tools.Constantes;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import android.widget.EditText;

public class ListaCantosFragment extends Fragment {
    private List<Canto> listaCantos = new ArrayList<>();
    private List<Canto> listaRespaldo = new ArrayList<>();
    private RecyclerView lista;
    private AdaptadorCantos adapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabNuevo;
    private View noGroupContainer;
    private Button btnCrearGrupo, btnUnirseGrupo;
    private TextView tvNoGroup;

    // Control de listeners para tiempo real sin fugas de memoria
    private ValueEventListener cantosListener;
    private Query queryCantos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lista_cantos, container, false);

        lista = root.findViewById(R.id.lista);
        progressBar = root.findViewById(R.id.pbCantos);

        this.fabNuevo = root.findViewById(R.id.fabNuevoCanto);
        this.fabNuevo.setOnClickListener(v -> {
            if (Constantes.GRUPO_SELECCIONADO == null || Constantes.GRUPO_SELECCIONADO.isEmpty()) {
                Toast.makeText(getActivity(), "Debes tener un grupo seleccionado para crear un canto", Toast.LENGTH_SHORT).show();
                return;
            }
            nuevoCanto();
        });

        noGroupContainer = root.findViewById(R.id.no_group_container);
        tvNoGroup = root.findViewById(R.id.tvNoGroup);
        btnCrearGrupo = root.findViewById(R.id.btnCrearGrupo);
        btnUnirseGrupo = root.findViewById(R.id.btnUnirseGrupo);

        btnCrearGrupo.setOnClickListener(v -> {
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

        btnUnirseGrupo.setOnClickListener(v -> {
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

        // Configuración inicial del RecyclerView y Adaptador
        listaCantos = new ArrayList<>();
        listaRespaldo = new ArrayList<>();
        adapter = new AdaptadorCantos(listaCantos, getActivity());
        lista.setHasFixedSize(true);
        lista.setLayoutManager(new LinearLayoutManager(getActivity()));
        lista.setAdapter(adapter);

        // Se inicializa la primera carga al crear la vista únicamente
        inicializarFirebase();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiamos el listener cuando el fragmento destruye su interfaz
        detenerListenerCantos();
    }

    private void detenerListenerCantos() {
        if (queryCantos != null && cantosListener != null) {
            queryCantos.removeEventListener(cantosListener);
            cantosListener = null;
            queryCantos = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater){
        inflater.inflate(R.menu.menu_lista, menu);

        MenuItem item = menu.findItem(R.id.buscar);

        SearchView buscador = (SearchView) item.getActionView();
        if (buscador != null) {
            buscador.setOnQueryTextListener(oyente);
            buscador.setQueryHint(getString(R.string.buscar));
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nuevoCanto:
                if (Constantes.GRUPO_SELECCIONADO == null || Constantes.GRUPO_SELECCIONADO.isEmpty()) {
                    Toast.makeText(getActivity(), "Debes tener un grupo seleccionado para crear un canto", Toast.LENGTH_SHORT).show();
                    return true;
                }
                nuevoCanto();
                break;
            case R.id.info:
                alertaAcercaDe();
                break;
            case R.id.cambiarGrupo:
                cambiarGrupo();
                break;
            case R.id.cerrarSesion:
                cerrarSesion();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cambiarGrupo(){
        FirebaseUser usuarioFirebase = FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioFirebase == null) {
            Toast.makeText(getActivity(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("usuarios").child(usuarioFirebase.getUid()).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                Usuario u = dataSnapshot.getValue(Usuario.class);
                if (u != null) {
                    mostrarDialogoCambioGrupo(u);
                } else {
                    Toast.makeText(getActivity(), "Error al cargar usuario", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Usuario no encontrado en la base de datos", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show());
    }

    private void mostrarDialogoCambioGrupo(Usuario usuario) {
        if (usuario.getGrupos() == null || usuario.getGrupos().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("No tienes grupos");
            builder.setMessage("Crea un nuevo grupo o únete a uno existente");
            builder.setCancelable(true);
            builder.setNeutralButton("Crear grupo", (dialog, which) -> mostrarDialogoCrearGrupo(usuario));
            builder.setPositiveButton("Unirse a grupo", (dialog, which) -> mostrarDialogoUnirseGrupo(usuario));
            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
            AlertDialog alerta = builder.create();
            alerta.show();
            return;
        }

        obtenerNombresGruposYMostrarDialogo(usuario);
    }

    private void obtenerNombresGruposYMostrarDialogo(Usuario usuario) {
        ArrayList<String> grupoUuids = usuario.getGrupos();
        ArrayList<String> grupoNombres = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference("grupos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (String uuid : grupoUuids) {
                        DataSnapshot grupoSnapshot = snapshot.child(uuid);
                        if (grupoSnapshot.exists()) {
                            Grupo grupo = grupoSnapshot.getValue(Grupo.class);
                            if (grupo != null) {
                                grupoNombres.add(grupo.getNombre() + "\n(Código: " + grupo.getCodigo() + ")");
                            }
                        }
                    }
                }

                mostrarDialogoConNombres(usuario, grupoUuids, grupoNombres);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                for (String uuid : grupoUuids) {
                    grupoNombres.add(uuid);
                }
                mostrarDialogoConNombres(usuario, grupoUuids, grupoNombres);
            }
        });
    }

    private void mostrarDialogoConNombres(Usuario usuario, ArrayList<String> grupoUuids, ArrayList<String> grupoNombres) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Cambiar de grupo");

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_searchable_list, null);
        androidx.appcompat.widget.SearchView sv = dialogView.findViewById(R.id.dialogSearchView);
        ListView lv = dialogView.findViewById(R.id.dialogListView);

        String[] nombresArray = grupoNombres.toArray(new String[0]);
        ArrayAdapter<String> adapterLocal = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_single_choice, nombresArray);
        lv.setAdapter(adapterLocal);

        for (int i = 0; i < grupoUuids.size(); i++) {
            if (grupoUuids.get(i).equals(usuario.getGrupoActual())) {
                lv.setItemChecked(i, true);
                lv.setSelection(i);
                break;
            }
        }

        sv.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterLocal.getFilter().filter(newText);
                return true;
            }
        });

        AlertDialog alerta = builder.setView(dialogView)
                .setCancelable(true)
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Crear nuevo grupo", (dialog, which) -> {
                    dialog.dismiss();
                    mostrarDialogoCrearGrupo(usuario);
                })
                .setPositiveButton("Unirse a grupo", (dialog, which) -> {
                    dialog.dismiss();
                    mostrarDialogoUnirseGrupo(usuario);
                })
                .create();

        lv.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < grupoUuids.size()) {
                String grupoUuidSeleccionado = grupoUuids.get(position);
                FirebaseDatabase.getInstance().getReference("usuarios").child(usuario.getUid()).child("grupoActual").setValue(grupoUuidSeleccionado);
                Constantes.GRUPO_SELECCIONADO = grupoUuidSeleccionado;
                Toast.makeText(getActivity(), "Grupo cambiado a " + nombresArray[position], Toast.LENGTH_SHORT).show();
                alerta.dismiss();
                inicializarFirebase();
            }
        });

        alerta.show();
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

        FirebaseDatabase.getInstance().getReference("grupos").orderByChild("codigo").equalTo(codigo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            generarCodigoUnicoYCrearGrupo(usuario, nombreGrupo, uuid);
                        } else {
                            crearGrupoConCodigo(usuario, nombreGrupo, uuid, codigo);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        crearGrupoConCodigo(usuario, nombreGrupo, uuid, codigo);
                    }
                });
    }

    private void crearGrupoConCodigo(Usuario usuario, String nombreGrupo, String uuid, String codigo) {
        Grupo nuevoGrupo = new Grupo(uuid, nombreGrupo, codigo);

        DatabaseReference refGrupos = FirebaseDatabase.getInstance().getReference("grupos").child(uuid);
        refGrupos.setValue(nuevoGrupo).addOnSuccessListener(aVoid -> {
            ArrayList<String> grupos = usuario.getGrupos();
            if (grupos == null) {
                grupos = new ArrayList<>();
            }
            grupos.add(uuid);
            usuario.setGrupos(grupos);
            usuario.setGrupoActual(uuid);

            FirebaseDatabase.getInstance().getReference("usuarios").child(usuario.getUid()).setValue(usuario);
            Constantes.GRUPO_SELECCIONADO = uuid;

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
        FirebaseDatabase.getInstance().getReference("grupos").orderByChild("codigo").equalTo(codigo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Grupo grupo = dataSnapshot.getValue(Grupo.class);
                                if (grupo != null) {
                                    agregarGrupoAlUsuario(usuario, grupo.getUuid(), grupo.getNombre());
                                    return;
                                }
                            }
                        } else {
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

        if (grupos.contains(grupoUuid)) {
            Toast.makeText(getActivity(), "Ya eres miembro de este grupo", Toast.LENGTH_SHORT).show();
            return;
        }

        grupos.add(grupoUuid);
        usuario.setGrupos(grupos);
        usuario.setGrupoActual(grupoUuid);

        FirebaseDatabase.getInstance().getReference("usuarios").child(usuario.getUid()).setValue(usuario)
                .addOnSuccessListener(aVoid -> {
                    Constantes.GRUPO_SELECCIONADO = grupoUuid;
                    inicializarFirebase();
                    Toast.makeText(getActivity(), "Te has unido al grupo '" + nombreGrupo + "'", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error al unirse al grupo", Toast.LENGTH_SHORT).show();
                });
    }

    private void inicializarFirebase() {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        DatabaseReference dr = fd.getReference("usuarios");

        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        dr.child(currentUser.getUid()).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                Usuario u = dataSnapshot.getValue(Usuario.class);
                if (u != null) {
                    if (u.getGrupos() == null || u.getGrupos().isEmpty() || u.getGrupoActual() == null || u.getGrupoActual().isEmpty()) {
                        detenerListenerCantos();
                        noGroupContainer.setVisibility(View.VISIBLE);
                        lista.setVisibility(View.GONE);
                        fabNuevo.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Constantes.GRUPO_SELECCIONADO = null;
                    } else {
                        noGroupContainer.setVisibility(View.GONE);
                        lista.setVisibility(View.VISIBLE);
                        fabNuevo.setVisibility(View.VISIBLE);
                        Constantes.GRUPO_SELECCIONADO = u.getGrupoActual();
                        cargarCantos();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Error al cargar usuario", Toast.LENGTH_SHORT).show();
                }
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Usuario no encontrado en la base de datos", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarCantos() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        DatabaseReference dr = fd.getReference("cantos");

        if (Constantes.GRUPO_SELECCIONADO == null) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        final String grupoSeleccionado = Constantes.GRUPO_SELECCIONADO.trim();

        detenerListenerCantos();

        queryCantos = dr.orderByChild("grupo_id").equalTo(grupoSeleccionado);

        cantosListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaCantos.clear();
                listaRespaldo.clear();

                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    if (objSnap != null) {
                        try {
                            Canto c = objSnap.getValue(Canto.class);
                            if (c != null) {
                                listaCantos.add(c);
                            }
                        } catch (Exception e) {
                            Log.e("Error_Firebase", "Llave con error: " + objSnap.getKey(), e);
                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    listaCantos.sort((c1, c2) -> c1.getNombre().compareToIgnoreCase(c2.getNombre()));
                } else {
                    Collections.sort(listaCantos, (c1, c2) -> c1.getNombre().compareToIgnoreCase(c2.getNombre()));
                }

                listaRespaldo.addAll(listaCantos);

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Log.e("Firebase_Error", "Error en consulta: " + error.getMessage());
                Toast.makeText(getActivity(), "Error de conexión con la base de datos", Toast.LENGTH_SHORT).show();
            }
        };

        queryCantos.addValueEventListener(cantosListener);
    }

    private void cerrarSesion(){
        detenerListenerCantos();
        FirebaseAuth.getInstance().signOut();

        Activity activity = getActivity();
        if (activity != null) {
            Intent i = new Intent(activity, LoginActivity.class);
            startActivity(i);
            activity.overridePendingTransition(R.anim.right_in, R.anim.right_out);
            activity.finish();
        }
    }

    private void alertaAcercaDe(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.acerca_de);
        String mensaje = "\u00a9 Paco Sotelo 2026\nPara el mundo, desde 2021 \n\nCreditos: \nLogo: Santiago Romo \n\n" +
                "Versión: 5.0.4" + "\n\nUsuario: " + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail() + "\n\nGrupo Seleccionado: " + (Constantes.GRUPO_SELECCIONADO == null || Constantes.GRUPO_SELECCIONADO.isEmpty() ? "Ninguno" : Constantes.GRUPO_SELECCIONADO);
        builder.setMessage(mensaje);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> dialog.dismiss());
        AlertDialog alerta = builder.create();
        alerta.show();
    }

    private void nuevoCanto() {
        Activity activity = getActivity();
        if (activity != null) {
            Intent i = new Intent(activity, NuevoCantoActivity.class);
            i.putExtra("getTipo", 0);
            startActivity(i);
            activity.overridePendingTransition(R.anim.left_in, R.anim.left_out);
        }
    }

    private void verCanto(Canto canto) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent i = new Intent(activity, CantoActivity.class);
            i.putExtra("getID", canto.getId());
            i.putExtra("getNombre", canto.getNombre());
            i.putExtra("getLetra", canto.getLetra());
            i.putExtra("getMomentos", canto.getMomentos());
            i.putExtra("getTiempos", canto.getTiempos());

            startActivity(i);
            activity.overridePendingTransition(R.anim.left_in, R.anim.left_out);
        }
    }

    public static String quitaDiacriticos(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    // Buscador optimizado: No recrea el Adaptador en cada tecla presionada
    private final SearchView.OnQueryTextListener oyente = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            int longitud = s.length();
            listaCantos.clear();

            if (longitud == 0) {
                listaCantos.addAll(listaRespaldo);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    List<Canto> coleccion = listaRespaldo.stream().filter(
                            i -> quitaDiacriticos(i.getNombre().toLowerCase()).contains(quitaDiacriticos(s.toLowerCase()))
                    ).collect(Collectors.toList());
                    listaCantos.addAll(coleccion);
                } else {
                    for (Canto z : listaRespaldo) {
                        if (quitaDiacriticos(z.getNombre().toLowerCase()).contains(quitaDiacriticos(s.toLowerCase()))) {
                            listaCantos.add(z);
                        }
                    }
                }
            }

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            return true;
        }
    };
}