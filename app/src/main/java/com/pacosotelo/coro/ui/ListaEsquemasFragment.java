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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.tools.AdaptadorEsquemas;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ListaEsquemasFragment extends Fragment {
    private final List<Esquema> listaEsquemas = new ArrayList<>();
    private final List<Esquema> listaRespaldo = new ArrayList<>();
    private RecyclerView rvEsquemas;
    private AdaptadorEsquemas adapter;
    private ProgressBar progressBar;

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

        FloatingActionButton fabNuevo = root.findViewById(R.id.fabNuevoEsquema);
        fabNuevo.setOnClickListener(v -> nuevoEsquema());

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
        DatabaseReference dr = fd.getReference("esquemas");

        progressBar.setVisibility(View.VISIBLE);

        dr.orderByChild("nombre").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaEsquemas.clear();
                listaRespaldo.clear();

                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    if(objSnap!=null) {
                        Esquema e = objSnap.getValue(Esquema.class);
                        listaEsquemas.add(e);
                    }
                }

                //Collections.reverse(listaEsquemas);

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