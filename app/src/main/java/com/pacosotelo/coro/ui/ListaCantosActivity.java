package com.pacosotelo.coro.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.tools.AdaptadorCantos;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListaCantosActivity extends AppCompatActivity {
    private final List<Canto> listaCantos = new ArrayList<>();
    private final List<Canto> listaRespaldo = new ArrayList<>();
    private RecyclerView lista;
    private AdaptadorCantos adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listacantos);

        lista = findViewById(R.id.lista);

        inicializarFirebase();

        /*lista.setOnItemClickListener((parent, view, position, id) -> {
            Canto canto = (Canto) parent.getItemAtPosition(position);

            verCanto(canto);
        });*/
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_lista,menu);

        MenuItem item = menu.findItem(R.id.buscar);

        SearchView buscador = (SearchView) item.getActionView();
        buscador.setOnQueryTextListener(oyente);
        buscador.setQueryHint(getString(R.string.buscar));

        return super.onCreateOptionsMenu(menu);
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
        }
        return true;
    }

    private void inicializarFirebase() {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        DatabaseReference dr = fd.getReference("Canto");

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

                    adapter = new AdaptadorCantos(listaCantos, ListaCantosActivity.this);
                    lista.setHasFixedSize(true);
                    lista.setLayoutManager(new LinearLayoutManager(ListaCantosActivity.this));
                    lista.setAdapter(adapter);
                }

                listaRespaldo.addAll(listaCantos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void alertaAcercaDe(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.acerca_de);
        String mensaje = "\u00a9 Paco Sotelo 2022 para el coro Angeles de Dios \n\n" +
                "Version 3.0";
        builder.setMessage(mensaje);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> dialog.dismiss());
        AlertDialog alerta = builder.create();
        alerta.show();
    }

    private void nuevoCanto() {
        Intent i = new Intent(ListaCantosActivity.this, NuevoCantoActivity.class);
        i.putExtra("getTipo", 0);
        startActivity(i);
        overridePendingTransition(R.anim.left_in,R.anim.left_out);
        finish();
    }

    private void verCanto(Canto canto) {
        Intent i = new Intent(ListaCantosActivity.this, CantoActivity.class);
        i.putExtra("getID", canto.getId());
        i.putExtra("getNombre",canto.getNombre());
        i.putExtra("getLetra", canto.getLetra());
        i.putExtra("getMomentos", canto.getMomentos());
        i.putExtra("getTiempos", canto.getTiempos());

        startActivity(i);
        overridePendingTransition(R.anim.left_in,R.anim.left_out);
        finish();
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
                listaCantos.clear();
                listaCantos.addAll(listaRespaldo);
            }else{
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    List<Canto> collecion = listaRespaldo.stream().filter
                                    (i->i.getNombre().toLowerCase().contains(s.toLowerCase())).
                            collect(Collectors.toList());
                    listaCantos.clear();
                    listaCantos.addAll(collecion);
                }else {
                    listaCantos.clear();
                    for (Canto z: listaRespaldo) {
                        if (z.getNombre().toLowerCase().contains(s.toLowerCase())){
                            listaCantos.add(z);
                        }
                    }
                }
            }

            adapter = new AdaptadorCantos(listaCantos, ListaCantosActivity.this);
            lista.setHasFixedSize(true);
            lista.setLayoutManager(new LinearLayoutManager(ListaCantosActivity.this));
            lista.setAdapter(adapter);

            return true;
        }
    };

}