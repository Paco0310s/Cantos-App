package com.pacosotelo.coro.vista;

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
import com.pacosotelo.coro.AdaptadorCantos;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelo.Canto;
import java.util.ArrayList;
import java.util.List;

public class Lista extends AppCompatActivity {
    private final List<Canto> listaCantos = new ArrayList<>();
    private RecyclerView lista;
    private AdaptadorCantos adapter;
    //private ArrayAdapter<Canto> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

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

                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    Canto c = objSnap.getValue(Canto.class);
                    listaCantos.add(c);

                    //adapter = new ArrayAdapter<>(Lista.this, android.R.layout.simple_list_item_1, listaCantos);

                    adapter = new AdaptadorCantos(listaCantos, Lista.this);
                    lista.setHasFixedSize(true);
                    lista.setLayoutManager(new LinearLayoutManager(Lista.this));
                    lista.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void alertaAcercaDe(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.acerca_de);
        String mensaje = "\u00a9 Paco Sotelo 2021 para el coro Angeles de Dios \n\n" +
                "Version 0.2";
        builder.setMessage(mensaje);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> dialog.dismiss());
        AlertDialog alerta = builder.create();
        alerta.show();
    }

    private void nuevoCanto() {
        Intent i = new Intent(Lista.this, Nuevo.class);
        i.putExtra("getTipo", 0);
        startActivity(i);
        overridePendingTransition(R.anim.left_in,R.anim.left_out);
        finish();
    }

    private void verCanto(Canto canto) {
        Intent i = new Intent(Lista.this, VerCanto.class);
        i.putExtra("getID", canto.getId());
        i.putExtra("getNombre",canto.getNombre());
        i.putExtra("getLetra", canto.getLetra());
        i.putExtra("getMomentos", canto.getMomentos());
        i.putExtra("getTiempos", canto.getTiempos());

        startActivity(i);
        overridePendingTransition(R.anim.left_in,R.anim.left_out);
        finish();
    }

    private static List<Canto> filter(List<Canto> cantos, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<Canto> filteredModelList = new ArrayList<>();

        for (Canto canto : cantos) {
            final String nombre = canto.getNombre().toLowerCase();
            final String id = canto.getId();
            if (nombre.contains(lowerCaseQuery) || id.equals(query)) {
                filteredModelList.add(canto);
            }
        }

        return filteredModelList;
    }

    SearchView.OnQueryTextListener oyente = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            //adapter.getFilter().filter(newText);
            final List<Canto> filteredModelList = filter(listaCantos, query);
            adapter.setLista(filteredModelList);
            return true;
        }
    };

}