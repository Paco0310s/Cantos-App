package com.pacosotelo.coro.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.tools.AdaptadorCantos;
import com.pacosotelo.coro.tools.Constantes;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListaCantosFragment extends Fragment {
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
        View root = inflater.inflate(R.layout.fragment_lista_cantos, container, false);

        lista = root.findViewById(R.id.lista);

        progressBar = root.findViewById(R.id.pbCantos);

        FloatingActionButton fabNuevo = root.findViewById(R.id.fabNuevoCanto);
        fabNuevo.setOnClickListener(v -> nuevoCanto());

        inicializarFirebase();

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater){
        inflater.inflate(R.menu.menu_lista,menu);

        MenuItem item = menu.findItem(R.id.buscar);

        SearchView buscador = (SearchView) item.getActionView();
        buscador.setOnQueryTextListener(oyente);
        buscador.setQueryHint(getString(R.string.buscar));

        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nuevoCanto:
                nuevoCanto();
                break;
            case R.id.info:
                alertaAcercaDe();
                break;
            case R.id.cambiarApp:
                cambiarApp();
                break;
            case R.id.cerrarSesion:
                cerrarSesion();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cambiarApp(){
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Toast.makeText(getActivity(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!usuario.getUid().equals("mYW9YLYZPmZdhaSwSS0ONF0EUe53")) {
            Toast.makeText(getActivity(), "No tienes permisos para cambiar de app", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.cambiar_app);
        String[] apps = Constantes.APPS;
        int indiceActual = 0;
        for (int i = 0; i < apps.length; i++) {
            if(apps[i].equals(Constantes.APP)){
                indiceActual = i;
                break;
            }
        }
        builder.setSingleChoiceItems(apps, indiceActual, (dialog, which) -> {
            Constantes.APP = apps[which];
            Toast.makeText(getActivity(), "App cambiada a " + Constantes.APP, Toast.LENGTH_SHORT).show();
            inicializarFirebase();
            dialog.dismiss();
        });
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.dismiss());
        AlertDialog alerta = builder.create();
        alerta.show();
    }

    private void inicializarFirebase() {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        DatabaseReference dr = fd.getReference("cantos");

        progressBar.setVisibility(View.VISIBLE);

        dr.orderByChild("app").equalTo(Constantes.APP).addValueEventListener(new ValueEventListener() {
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

                // Ordenar la lista por nombre
                listaCantos.sort((c1, c2) -> c1.getNombre().compareTo(c2.getNombre()));

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

    private void cerrarSesion(){
        FirebaseAuth.getInstance().signOut();

        Activity activity = getActivity();
        Intent i = new Intent(activity, LoginActivity.class);
        startActivity(i);
        assert activity != null;
        activity.overridePendingTransition(R.anim.right_in,R.anim.right_out);
        activity.finish();
    }

    private void alertaAcercaDe(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.acerca_de);
        String mensaje = "\u00a9 Paco Sotelo 2026\nPara el mundo, desde 2021 \n\nCreditos: \nLogo: Santiago Romo \n\n" +
                "Versión: 4.8.0" + "\n\nUsuario: " + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail() + "\n\nApp: " + Constantes.APP;
        builder.setMessage(mensaje);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> dialog.dismiss());
        AlertDialog alerta = builder.create();
        alerta.show();
    }

    private void nuevoCanto() {
        Activity activity = getActivity();

        Intent i = new Intent(activity, NuevoCantoActivity.class);
        i.putExtra("getTipo", 0);
        startActivity(i);

        assert activity != null;

        activity.overridePendingTransition(R.anim.left_in,R.anim.left_out);
    }

    private void verCanto(Canto canto) {
        Activity activity = getActivity();

        Intent i = new Intent(activity, CantoActivity.class);
        i.putExtra("getID", canto.getId());
        i.putExtra("getNombre",canto.getNombre());
        i.putExtra("getLetra", canto.getLetra());
        i.putExtra("getMomentos", canto.getMomentos());
        i.putExtra("getTiempos", canto.getTiempos());

        startActivity(i);

        assert activity != null;

        activity.overridePendingTransition(R.anim.left_in,R.anim.left_out);
        activity.finish();
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