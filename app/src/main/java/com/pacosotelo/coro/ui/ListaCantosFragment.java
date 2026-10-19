package com.pacosotelo.coro.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
import com.pacosotelo.coro.tools.MenuHelper;
import com.pacosotelo.coro.tools.Transiciones;
import com.pacosotelo.coro.viewmodel.CantosViewModel;
import com.pacosotelo.coro.viewmodel.GruposViewModel;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ListaCantosFragment extends Fragment {
    private List<Canto> listaCantos;
    private List<Canto> listaRespaldo;
    private RecyclerView lista;
    private AdaptadorCantos adapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabNuevo;
    private View noGroupContainer;
    private Button btnCrearGrupo, btnUnirseGrupo;
    private TextView tvNoGroup;

    private CantosViewModel cantosViewModel;
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
        } else {
            cargarCantos();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lista_cantos, container, false);

        cantosViewModel = new ViewModelProvider(requireActivity()).get(CantosViewModel.class);
        gruposViewModel = new ViewModelProvider(requireActivity()).get(GruposViewModel.class);

        gruposViewModel.getGroupCreatedMessage().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                Toast.makeText(getActivity(), mensaje, Toast.LENGTH_LONG).show();
                gruposViewModel.clearGroupCreatedMessage();
            }
        });

        lista = root.findViewById(R.id.lista);
        progressBar = root.findViewById(R.id.pbCantos);

        this.fabNuevo = root.findViewById(R.id.fabNuevoCanto);
        this.fabNuevo.setOnClickListener(v -> {
            if (Constantes.GRUPO_SELECCIONADO == null || Constantes.GRUPO_SELECCIONADO.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.msg_grupo_necesario_canto), Toast.LENGTH_SHORT).show();
                return;
            }
            nuevoCanto();
        });

        noGroupContainer = root.findViewById(R.id.no_group_container);
        tvNoGroup = root.findViewById(R.id.tvNoGroup);
        btnCrearGrupo = root.findViewById(R.id.btnCrearGrupo);
        btnUnirseGrupo = root.findViewById(R.id.btnUnirseGrupo);

        btnCrearGrupo.setOnClickListener(v -> MenuHelper.mostrarDialogoCrearGrupo(requireActivity(), gruposViewModel));
        btnUnirseGrupo.setOnClickListener(v -> MenuHelper.mostrarDialogoUnirseGrupo(requireActivity(), gruposViewModel));

        // Observar estado de carga
        cantosViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observar errores
        cantosViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observar los cantos para la lista
        cantosViewModel.getCantos().observe(getViewLifecycleOwner(), cantos -> {
            listaRespaldo.clear();
            listaRespaldo.addAll(cantos);
            if (adapter != null) {
                adapter.actualizarLista(cantos);
            }
        });

        // Configuración inicial del RecyclerView y Adaptador
        listaCantos = new ArrayList<>();
        listaRespaldo = new ArrayList<>();
        adapter = new AdaptadorCantos(listaCantos, getActivity());
        lista.setHasFixedSize(true);
        lista.setLayoutManager(new LinearLayoutManager(getActivity()));
        lista.setAdapter(adapter);

        inicializarFirebase();

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater){
        inflater.inflate(R.menu.menu_lista_todos_cantos, menu);

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
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            return;
        }
        
        gruposViewModel.startListeningUsuario(currentUser.getUid());
        gruposViewModel.getUsuarioLiveData().observe(getViewLifecycleOwner(), u -> {
            if (u != null) {
                Constantes.usuario = u;
                if (u.getGrupos() == null || u.getGrupos().isEmpty() || u.getGrupoActual() == null || u.getGrupoActual().isEmpty()) {
                    if (noGroupContainer != null) noGroupContainer.setVisibility(View.VISIBLE);
                    if (lista != null) lista.setVisibility(View.GONE);
                    if (fabNuevo != null) fabNuevo.setVisibility(View.INVISIBLE);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Constantes.GRUPO_SELECCIONADO = null;
                } else {
                    if (noGroupContainer != null) noGroupContainer.setVisibility(View.GONE);
                    if (lista != null) lista.setVisibility(View.VISIBLE);
                    if (fabNuevo != null) fabNuevo.setVisibility(View.VISIBLE);
                    Constantes.GRUPO_SELECCIONADO = u.getGrupoActual();
                    cargarCantos();
                }
            }
        });
    }

    private void cargarCantos() {
        if (Constantes.GRUPO_SELECCIONADO == null) {
            return;
        }

        final String grupoSeleccionado = Constantes.GRUPO_SELECCIONADO.trim();
        cantosViewModel.loadCantosByGrupo(grupoSeleccionado);
    }

    private void nuevoCanto() {
        Activity activity = getActivity();
        if (activity != null) {
            Intent i = new Intent(activity, NuevoCantoActivity.class);
            i.putExtra("getTipo", 0);
            startActivity(i);
            Transiciones.left(activity);
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
            List<Canto> listaFiltrada = new ArrayList<>();

            if (longitud == 0) {
                listaFiltrada.addAll(listaRespaldo);
            } else {
                for (Canto z : listaRespaldo) {
                    if (z != null && z.getNombre() != null) {
                        String busqueda = quitaDiacriticos(s.toLowerCase());
                        String actual = quitaDiacriticos(z.getNombre().toLowerCase());
                        if (actual.contains(busqueda)) {
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
