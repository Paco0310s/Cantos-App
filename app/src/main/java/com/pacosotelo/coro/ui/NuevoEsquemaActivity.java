package com.pacosotelo.coro.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.modelos.Usuario;
import com.pacosotelo.coro.tools.AdaptadorCantos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NuevoEsquemaActivity extends AppCompatActivity {
    Button bAgregarCanto, bGuardarEsquema;
    EditText etNombreEsquema;
    RecyclerView rvListaCantosEsquema;
    DatabaseReference dr;
    private final ArrayList<Canto> listaCantos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_esquema);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Nuevo Esquema");

        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        dr = fd.getReference();

        bAgregarCanto = findViewById(R.id.bAgregarCanto);
        bGuardarEsquema = findViewById(R.id.bGuardarEsquema);
        etNombreEsquema = findViewById(R.id.etNombreEsquema);
        rvListaCantosEsquema = findViewById(R.id.rvCantosEsquema);

        bAgregarCanto.setOnClickListener(v -> agregarCanto());
        bGuardarEsquema.setOnClickListener(v -> guardarEsquema());
    }

    private void agregarCanto() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Selecciona un canto");

        final ArrayAdapter<Canto> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);

        dr.child("Canto").get().addOnCompleteListener(task -> {
            // Verifica si se pudo obtener los cantos
            if (task.isSuccessful()) {
                for (DataSnapshot objSnap : task.getResult().getChildren()) {
                    if(objSnap!=null) {
                        Canto c = objSnap.getValue(Canto.class);
                        arrayAdapter.add(c);
                    }
                }
            }
        });

        builderSingle.setNegativeButton("Cancelar", (dialog, which) ->
                dialog.dismiss());

        builderSingle.setAdapter(arrayAdapter, (dialog, which) ->
                listaCantos.add(arrayAdapter.getItem(which)));

        builderSingle.show();
    }

    private void guardarEsquema() {
        Esquema esquema = new Esquema();

        esquema.setId(UUID.randomUUID().toString());
        esquema.setNombre(etNombreEsquema.getText().toString());
        esquema.setCantos(listaCantos);

        dr.child("esquemas").child(esquema.getId()).setValue(esquema);

        Toast.makeText(this, "Esquema agregado", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

}