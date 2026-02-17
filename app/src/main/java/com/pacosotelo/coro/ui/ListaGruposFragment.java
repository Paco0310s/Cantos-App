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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.modelos.Grupo;
import com.pacosotelo.coro.tools.AdaptadorEsquemas;
import com.pacosotelo.coro.tools.AdaptadorGrupos;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListaGruposFragment extends Fragment {
    private final List<Grupo> listaGrupos = new ArrayList<>();
    private RecyclerView rvGrupos;
    private AdaptadorGrupos adapter;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lista_grupos, container, false);

        rvGrupos = root.findViewById(R.id.rvGrupos);

        progressBar = root.findViewById(R.id.pbGrupos);

        FloatingActionButton fabNuevo = root.findViewById(R.id.fabNuevoGrupo);
        fabNuevo.setOnClickListener(v -> nuevoGrupo()
        );

        rvGrupos.setOnTouchListener((v, event) -> {
            root.findViewById(R.id.rvGrupos).getParent()
                    .requestDisallowInterceptTouchEvent(false);
            return false;
        });

        inicializarFirebase();

        return root;
    }

    private void inicializarFirebase() {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        DatabaseReference dr = fd.getReference("grupos");

        progressBar.setVisibility(View.VISIBLE);

        dr.orderByChild("nombre").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaGrupos.clear();

                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    if(objSnap!=null) {
                        try {
                            Grupo g = objSnap.getValue(Grupo.class);
                            listaGrupos.add(g);
                        } catch (Exception e) {
                            Log.e("Error", Objects.requireNonNull(e.getMessage()));
                        }
                    }
                }

                adapter = new AdaptadorGrupos(listaGrupos, getActivity());
                rvGrupos.setHasFixedSize(true);
                rvGrupos.setLayoutManager(new LinearLayoutManager(getActivity()));
                rvGrupos.setAdapter(adapter);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void nuevoGrupo() {
    }
}