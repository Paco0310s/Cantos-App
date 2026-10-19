package com.pacosotelo.coro.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
import com.pacosotelo.coro.modelos.Grupo;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.tools.AdaptadorEsquemas;
import com.pacosotelo.coro.tools.Constantes;
import com.pacosotelo.coro.tools.MenuHelper;
import com.pacosotelo.coro.tools.Transiciones;
import androidx.lifecycle.ViewModelProvider;
import com.pacosotelo.coro.viewmodel.EsquemasViewModel;
import com.pacosotelo.coro.viewmodel.GruposViewModel;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListaEsquemasFragment extends Fragment {
    private List<Esquema> listaEsquemas = new ArrayList<>();
    private List<Esquema> listaRespaldo = new ArrayList<>();
    private RecyclerView rvEsquemas;
    private AdaptadorEsquemas adapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabNuevo;
    private View noGroupContainerEsquemas;
    private Button btnCrearGrupoEsquemas, btnUnirseGrupoEsquemas;
    private TextView tvNoGroupEsquemas;
    private EsquemasViewModel esquemasViewModel;
    private GruposViewModel gruposViewModel;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Constantes.GRUPO_SELECCIONADO == null || Constantes.GRUPO_SELECCIONADO.trim().isEmpty()) {
            inicializarFirebase();
        }
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
                Toast.makeText(getActivity(), getString(R.string.msg_grupo_necesario_esquema), Toast.LENGTH_SHORT).show();
            } else {
                nuevoEsquema();
            }
        });

        noGroupContainerEsquemas = root.findViewById(R.id.no_group_container_esquemas);
        tvNoGroupEsquemas = root.findViewById(R.id.tvNoGroupEsquemas);
        btnCrearGrupoEsquemas = root.findViewById(R.id.btnCrearGrupoEsquemas);
        btnUnirseGrupoEsquemas = root.findViewById(R.id.btnUnirseGrupoEsquemas);

        btnCrearGrupoEsquemas.setOnClickListener(v -> MenuHelper.mostrarDialogoCrearGrupo(requireActivity(), gruposViewModel));
        btnUnirseGrupoEsquemas.setOnClickListener(v -> MenuHelper.mostrarDialogoUnirseGrupo(requireActivity(), gruposViewModel));

        esquemasViewModel = new ViewModelProvider(this).get(EsquemasViewModel.class);
        gruposViewModel = new ViewModelProvider(requireActivity()).get(GruposViewModel.class);

        gruposViewModel.getGroupCreatedMessage().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                Toast.makeText(getActivity(), mensaje, Toast.LENGTH_LONG).show();
                gruposViewModel.clearGroupCreatedMessage();
            }
        });

        rvEsquemas.setOnTouchListener((v, event) -> {
            root.findViewById(R.id.rvEsquemas).getParent()
                    .requestDisallowInterceptTouchEvent(false);
            return false;
        });

        esquemasViewModel = new ViewModelProvider(requireActivity()).get(EsquemasViewModel.class);

        esquemasViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        esquemasViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        });

        esquemasViewModel.getEsquemas().observe(getViewLifecycleOwner(), esquemas -> {
            listaRespaldo.clear();
            listaRespaldo.addAll(esquemas);
            if (adapter != null) {
                adapter.actualizarLista(new ArrayList<>(esquemas));
            }
        });

        listaEsquemas = new ArrayList<>();
        listaRespaldo = new ArrayList<>();

        adapter = new AdaptadorEsquemas(listaEsquemas, getActivity());
        rvEsquemas.setHasFixedSize(true);
        rvEsquemas.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvEsquemas.setAdapter(adapter);

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.info) {
            MenuHelper.alertaAcercaDe(getActivity());
            return true;
        } else if (itemId == R.id.cerrarSesion) {
            MenuHelper.cerrarSesion(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void inicializarFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        gruposViewModel.startListeningUsuario(currentUser.getUid());
        gruposViewModel.getUsuarioLiveData().observe(getViewLifecycleOwner(), u -> {
            if (u != null) {
                Constantes.usuario = u;
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
            }
        });
    }

    private void cargarEsquemas() {
        if (Constantes.GRUPO_SELECCIONADO == null) {
            return;
        }

        final String grupoSeleccionado = Constantes.GRUPO_SELECCIONADO.trim();
        esquemasViewModel.loadEsquemasByGrupo(grupoSeleccionado);
    }









    private void nuevoEsquema() {
        Activity activity = getActivity();

        Intent i = new Intent(activity, NuevoEsquemaActivity.class);
        i.putExtra("getTipo", 0);
        startActivity(i);

        assert activity != null;

        Transiciones.left(activity);
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
            List<Esquema> listaFiltrada = new ArrayList<>();

            if (longitud == 0) {
                listaFiltrada.addAll(listaRespaldo);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    List<Esquema> collecion = listaRespaldo.stream().filter
                            (i -> quitaDiacriticos(i.getNombre()).toLowerCase().contains(quitaDiacriticos(s.toLowerCase()))).
                            collect(Collectors.toList());
                    listaFiltrada.addAll(collecion);
                } else {
                    for (Esquema z : listaRespaldo) {
                        if (quitaDiacriticos(z.getNombre()).toLowerCase().contains(quitaDiacriticos(s.toLowerCase()))) {
                            listaFiltrada.add(z);
                        }
                    }
                }
            }

            if (adapter != null) {
                adapter.actualizarLista(listaFiltrada);
            }

            return true;
        }
    };
}