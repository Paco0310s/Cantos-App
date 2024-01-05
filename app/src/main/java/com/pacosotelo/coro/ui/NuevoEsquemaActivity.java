package com.pacosotelo.coro.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.tools.AdaptadorCantoEsquema;

import java.util.ArrayList;
import java.util.UUID;

public class NuevoEsquemaActivity extends AppCompatActivity {
    Button bAgregarCanto, bGuardarEsquema;
    EditText etNombreEsquema;
    ListView rvCantosEsquema;
    DatabaseReference dr;
    FirebaseDatabase fd;
    private final ArrayList<Canto> listaCantos = new ArrayList<>();
    //AdaptadorCantoEsquema adapter2;
    ArrayAdapter<Canto> adapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_esquema);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Nuevo Esquema");
        actionBar.setDisplayHomeAsUpEnabled(true);

        fd = FirebaseDatabase.getInstance();
        dr = fd.getReference();

        bAgregarCanto = findViewById(R.id.bAgregarCanto);
        bGuardarEsquema = findViewById(R.id.bGuardarEsquema);
        etNombreEsquema = findViewById(R.id.etNombreEsquema);
        rvCantosEsquema = findViewById(R.id.rvCantosEsquema);

        bAgregarCanto.setOnClickListener(v -> agregarCanto());
        bGuardarEsquema.setOnClickListener(v -> guardarEsquema());

        adapter2 = new ArrayAdapter<>(this,
                R.layout.item_canto_esquema2);

        rvCantosEsquema.setOnItemClickListener((adapterView, view, i, l) -> cantoPulsado(view, i));
    }

    private void agregarCanto() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Selecciona un canto");

        ArrayAdapter<Canto> arrayAdapter = new ArrayAdapter<>(this,
                R.layout.item_canto_esquema);

        dr = fd.getReference("cantos");

        dr.orderByChild("nombre").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayAdapter.clear();

                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    if(objSnap!=null) {
                        Canto c = objSnap.getValue(Canto.class);
                        arrayAdapter.add(c);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*dr.child("Canto").get().addOnCompleteListener(task -> {
            // Verifica si se pudo obtener los cantos
            if (task.isSuccessful()) {
                for (DataSnapshot objSnap : task.getResult().getChildren()) {
                    if(objSnap!=null) {
                        Canto c = objSnap.getValue(Canto.class);
                        //arrayAdapter.add(c);
                        lista.add(c);
                    }
                }
            }
        });*/

        builderSingle.setNegativeButton("Cancelar", (dialog, which) ->
                dialog.dismiss());

        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
                    Canto c = arrayAdapter.getItem(which);
                    listaCantos.add(c);

                    //adapter2 = new AdaptadorCantoEsquema(listaCantos, this);
                    //rvCantosEsquema.setHasFixedSize(true);
                    //rvCantosEsquema.setLayoutManager(new LinearLayoutManager(this));
                    adapter2.add(c);
                    rvCantosEsquema.setAdapter(adapter2);
                });

        builderSingle.show();
    }

    private void cantoPulsado(View v, int indice) {
        final PopupMenu popupMenu = new PopupMenu(NuevoEsquemaActivity.this, v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_canto_esquema, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.verCanto:
                    Intent i = new Intent(this, CantoActivity.class);
                    i.putExtra("canto", listaCantos.get(indice));
                    startActivity(i);
                    overridePendingTransition(R.anim.left_in,R.anim.left_out);
                    return true;
                case R.id.eliminarCanto:
                    listaCantos.remove(indice);
                    adapter2.clear();
                    adapter2.addAll(listaCantos);
                    rvCantosEsquema.setAdapter(adapter2);
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();

    }

    private void guardarEsquema() {
        // Validar datos
        if(etNombreEsquema.getText().toString().isEmpty()) {
            Toast.makeText(this, "Ingresa un nombre para el esquema", Toast.LENGTH_SHORT).show();
            return;
        }

        if(listaCantos.isEmpty()) {
            Toast.makeText(this, "Agrega al menos un canto", Toast.LENGTH_SHORT).show();
            return;
        }

        Esquema esquema = new Esquema();

        esquema.setId(UUID.randomUUID().toString());
        esquema.setNombre(etNombreEsquema.getText().toString());

        esquema.setCantos(listaCantos);

        dr = fd.getReference();
        dr.child("esquemas").child(esquema.getId()).setValue(esquema);

        Toast.makeText(this, "Esquema agregado", Toast.LENGTH_SHORT).show();

        regresar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        regresar();
    }

    private void regresar() {
        finish();
        overridePendingTransition(R.anim.right_in,R.anim.right_out);
    }

    /*public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_nuevo,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }*/

}