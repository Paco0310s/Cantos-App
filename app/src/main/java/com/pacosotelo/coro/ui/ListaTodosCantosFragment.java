package com.pacosotelo.coro.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pacosotelo.coro.modelos.Grupo;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.tools.Constantes;
import com.pacosotelo.coro.tools.MenuHelper;
import com.pacosotelo.coro.viewmodel.CantosViewModel;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.tools.AdaptadorCantos;
import com.pacosotelo.coro.tools.Transiciones;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class ListaTodosCantosFragment extends Fragment {
    private final List<Canto> listaCantos = new ArrayList<>();
    // Caché en memoria (no persistente) para permitir búsqueda 'contains' sin reconsultar siempre
    private final List<Canto> listaCache = new ArrayList<>();
    private RecyclerView lista;
    private AdaptadorCantos adapter;
    private ProgressBar progressBar;
    private View containerSinInternet;
    private View containerInstruccion;
    private CantosViewModel cantosViewModel;
    private String ultimaBusqueda = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lista_todos_cantos, container, false);

        cantosViewModel = new ViewModelProvider(this).get(CantosViewModel.class);

        lista = root.findViewById(R.id.listaTodosCantos);

        progressBar = root.findViewById(R.id.pbTodosCantos);
        containerSinInternet = root.findViewById(R.id.containerSinInternet);
        containerInstruccion = root.findViewById(R.id.containerInstruccion);

        FloatingActionButton fabNuevo = root.findViewById(R.id.fabTodosNuevoCanto);
        fabNuevo.setOnClickListener(v -> nuevoCanto());

        cantosViewModel.getTodosCantos().observe(getViewLifecycleOwner(), cantos -> {
            listaCache.clear();
            listaCache.addAll(cantos);
            progressBar.setVisibility(View.GONE);
            filtrarYMostrar(ultimaBusqueda);
        });

        cantosViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                if (containerSinInternet != null) containerSinInternet.setVisibility(View.VISIBLE);
            }
        });

        cantosViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // No cargamos toda la lista al inicio para evitar persistir todo el listado localmente.
        // Esta pantalla hará consultas remotas sólo cuando el usuario escriba 3 o más letras.

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater){
        inflater.inflate(R.menu.menu_lista_todos_cantos,menu);

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

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        if (getActivity() != null) {
            Transiciones.fade(getActivity());
            getActivity().finish();
        }
    }



    // Método para obtener los datos y filtrar por 'contains' (se ejecuta sólo cuando el texto tiene 3+ caracteres)
    private void buscarEnFirebase(String texto) {
        ultimaBusqueda = texto;
        // Si no hay conexión, informar al usuario y no intentar la consulta
        if (!isNetworkAvailable()) {
            progressBar.setVisibility(View.GONE);
            if (containerSinInternet != null) containerSinInternet.setVisibility(View.VISIBLE);
            if (containerInstruccion != null) containerInstruccion.setVisibility(View.GONE);
            listaCantos.clear();
            adapter = new AdaptadorCantos(listaCantos, getActivity());
            lista.setHasFixedSize(true);
            lista.setLayoutManager(new LinearLayoutManager(getActivity()));
            lista.setAdapter(adapter);
            return;
        }

        if (containerSinInternet != null) containerSinInternet.setVisibility(View.GONE);
        
        // Si ya tenemos cache en memoria, filtramos localmente para 'contains'
        if (!listaCache.isEmpty()) {
            filtrarYMostrar(texto);
            return;
        }

        // Si no hay cache, le pedimos al ViewModel que los descargue
        cantosViewModel.loadAllCantos();
    }

    private void filtrarYMostrar(String s) {
        listaCantos.clear();

        String q = quitaDiacriticos(s.toLowerCase());

        for (Canto z : listaCache) {
            if (z == null || z.getNombre() == null) continue;
            String nombre = quitaDiacriticos(z.getNombre().toLowerCase());
            if (nombre.contains(q)) {
                listaCantos.add(z);
            }
        }

        adapter = new AdaptadorCantos(listaCantos, getActivity());
        lista.setHasFixedSize(true);
        lista.setLayoutManager(new LinearLayoutManager(getActivity()));
        lista.setAdapter(adapter);

        // Mostrar/ocultar mensajes
        if (listaCantos.isEmpty()) {
            if (containerInstruccion != null) containerInstruccion.setVisibility(View.GONE);
        } else {
            if (containerInstruccion != null) containerInstruccion.setVisibility(View.GONE);
            if (containerSinInternet != null) containerSinInternet.setVisibility(View.GONE);
        }
    }

    private boolean isNetworkAvailable() {
        Context ctx = getContext();
        if (ctx == null) return false;

        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = cm.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities nc = cm.getNetworkCapabilities(nw);
            return nc != null && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        }
    }

    private void nuevoCanto() {
        Activity activity = getActivity();

        Intent i = new Intent(activity, NuevoCantoActivity.class);
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
            int longitud = s.trim().length();

            if (longitud == 0 || longitud < 3) {
                // Sin texto o menos de 3 caracteres: mostrar instrucción inicial
                listaCantos.clear();
                adapter = new AdaptadorCantos(listaCantos, getActivity());
                lista.setHasFixedSize(true);
                lista.setLayoutManager(new LinearLayoutManager(getActivity()));
                lista.setAdapter(adapter);
                if (containerInstruccion != null) containerInstruccion.setVisibility(View.VISIBLE);
                if (containerSinInternet != null) containerSinInternet.setVisibility(View.GONE);
            } else {
                // 3 o más caracteres: ocultar instrucción y realizar búsqueda remota (contains)
                if (containerInstruccion != null) containerInstruccion.setVisibility(View.GONE);
                buscarEnFirebase(s);
            }

            return true;
        }
    };
}