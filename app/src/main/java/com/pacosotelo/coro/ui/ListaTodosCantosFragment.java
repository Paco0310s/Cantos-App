package com.pacosotelo.coro.ui;

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
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.tools.AdaptadorCantos;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListaTodosCantosFragment extends Fragment {
    private final List<Canto> listaCantos = new ArrayList<>();
    private final List<Canto> listaRespaldo = new ArrayList<>();
    private RecyclerView lista;
    private AdaptadorCantos adapter;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lista_todos_cantos, container, false);

        lista = root.findViewById(R.id.listaTodosCantos);

        progressBar = root.findViewById(R.id.pbTodosCantos);

        FloatingActionButton fabNuevo = root.findViewById(R.id.fabTodosNuevoCanto);
        fabNuevo.setOnClickListener(v -> nuevoCanto());

        inicializarFirebase();

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

    private void inicializarFirebase() {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        DatabaseReference dr = fd.getReference("cantos");

        progressBar.setVisibility(View.VISIBLE);

        dr.orderByChild("nombre").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaCantos.clear();
                listaRespaldo.clear();

                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    if(objSnap!=null) {
                        Canto c = objSnap.getValue(Canto.class);
                        listaCantos.add(c);
                    }
                }

                adapter = new AdaptadorCantos(listaCantos, getActivity());
                lista.setHasFixedSize(true);
                lista.setLayoutManager(new LinearLayoutManager(getActivity()));
                lista.setAdapter(adapter);

                listaRespaldo.addAll(listaCantos);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void nuevoCanto() {
        Activity activity = getActivity();

        Intent i = new Intent(activity, NuevoCantoActivity.class);
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
            //s.replace("ÁáÉéÍíÓóÚúÜü","");
            int longitud = s.length();
            if(longitud == 0)
            {
                listaCantos.clear();
                listaCantos.addAll(listaRespaldo);
            }else{
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    List<Canto> collecion = listaRespaldo.stream().filter
                                    (i->quitaDiacriticos(i.getNombre().toLowerCase()).contains(quitaDiacriticos(s.toLowerCase()))).
                            collect(Collectors.toList());
                    listaCantos.clear();
                    listaCantos.addAll(collecion);
                }else {
                    listaCantos.clear();
                    for (Canto z: listaRespaldo) {
                        if (quitaDiacriticos(z.getNombre().toLowerCase()).contains(quitaDiacriticos(s.toLowerCase()))){
                            listaCantos.add(z);
                        }
                    }
                }
            }

            adapter = new AdaptadorCantos(listaCantos, getActivity());
            lista.setHasFixedSize(true);
            lista.setLayoutManager(new LinearLayoutManager(getActivity()));
            lista.setAdapter(adapter);

            return true;
        }
    };
}