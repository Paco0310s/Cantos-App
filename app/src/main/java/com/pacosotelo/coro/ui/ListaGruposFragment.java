package com.pacosotelo.coro.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Grupo;
import com.pacosotelo.coro.tools.AdaptadorGrupos;
import com.pacosotelo.coro.tools.Constantes;
import com.pacosotelo.coro.tools.MenuHelper;
import androidx.lifecycle.ViewModelProvider;
import com.pacosotelo.coro.viewmodel.GruposViewModel;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListaGruposFragment extends Fragment {
    private final List<Grupo> listaGrupos = new ArrayList<>();
    private final List<Grupo> listaRespaldo = new ArrayList<>();
    private RecyclerView rvGrupos;
    private AdaptadorGrupos adapter;
    private ProgressBar progressBar;
    private GruposViewModel gruposViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lista_grupos, container, false);

        rvGrupos = root.findViewById(R.id.rvGrupos);
        progressBar = root.findViewById(R.id.pbGrupos);

        FloatingActionButton fabNuevo = root.findViewById(R.id.fabNuevoGrupo);
        fabNuevo.setOnClickListener(v -> nuevoGrupo());

        rvGrupos.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new AdaptadorGrupos(listaGrupos, getActivity(), 
        grupo -> {
            Intent intent = new Intent(getActivity(), MiembrosGrupoActivity.class);
            intent.putExtra("grupo", grupo);
            startActivity(intent);
        }, 
        (grupo, isActive) -> {
            if (Constantes.usuario != null && isActive) {
                Constantes.GRUPO_SELECCIONADO = grupo.getUuid();
                gruposViewModel.setGrupoActual(Constantes.usuario, grupo.getUuid());
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                Toast.makeText(getActivity(), getString(R.string.msg_grupo_activo, grupo.getNombre()), Toast.LENGTH_SHORT).show();
            }
        });
        rvGrupos.setAdapter(adapter);

        gruposViewModel = new ViewModelProvider(requireActivity()).get(GruposViewModel.class);

        gruposViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        gruposViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        });

        gruposViewModel.getGrupos().observe(getViewLifecycleOwner(), grupos -> {
            listaGrupos.clear();
            listaGrupos.addAll(grupos);
            listaRespaldo.clear();
            listaRespaldo.addAll(grupos);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });

        gruposViewModel.getUsuarioLiveData().observe(getViewLifecycleOwner(), u -> {
            if (u != null) {
                Constantes.usuario = u;
                Constantes.GRUPO_SELECCIONADO = u.getGrupoActual();
            }
        });

        inicializarFirebase();

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_lista, menu);

        MenuItem item = menu.findItem(R.id.buscar);
        SearchView buscador = (SearchView) item.getActionView();
        if (buscador != null) {
            buscador.setOnQueryTextListener(oyente);
            buscador.setQueryHint(getString(R.string.buscar));
        }

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
        if (currentUser != null) {
            gruposViewModel.startListeningUsuario(currentUser.getUid());
        }
    }

    private void nuevoGrupo() {
        if (Constantes.usuario == null) return;

        CharSequence[] opciones = {getString(R.string.btn_crear), getString(R.string.btn_unirse)};
        new MaterialAlertDialogBuilder(requireActivity(), R.style.CustomMaterialDialog)
                .setTitle(getString(R.string.titulo_gestion_grupos))
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        MenuHelper.mostrarDialogoCrearGrupo(requireActivity(), gruposViewModel);
                    } else if (which == 1) {
                        MenuHelper.mostrarDialogoUnirseGrupo(requireActivity(), gruposViewModel);
                    }
                })
                .setNegativeButton(getString(R.string.cancelar), null)
                .show();
    }

    public static String quitaDiacriticos(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    private final SearchView.OnQueryTextListener oyente = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            int longitud = s.length();
            List<Grupo> listaFiltrada = new ArrayList<>();

            if (longitud == 0) {
                listaFiltrada.addAll(listaRespaldo);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    List<Grupo> coleccion = listaRespaldo.stream().filter(
                            i -> quitaDiacriticos(i.getNombre().toLowerCase()).contains(quitaDiacriticos(s.toLowerCase()))
                    ).collect(Collectors.toList());
                    listaFiltrada.addAll(coleccion);
                } else {
                    for (Grupo z : listaRespaldo) {
                        if (quitaDiacriticos(z.getNombre().toLowerCase()).contains(quitaDiacriticos(s.toLowerCase()))) {
                            listaFiltrada.add(z);
                        }
                    }
                }
            }

            listaGrupos.clear();
            listaGrupos.addAll(listaFiltrada);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            return true;
        }
    };
}
