package com.pacosotelo.coro.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import java.util.ArrayList;
import java.util.Objects;

public class NuevoCantoActivity extends AppCompatActivity {
    private EditText etNombre, etLetra;
    private Button bAgregar;
    private DatabaseReference dr;
    private long maxid = 0;
    private int tipo = 0;
    private Canto canto;
    private ArrayList<String> momentos, tiempos;
    //private ListView listaMomentos, listaTiempos;
    private ArrayAdapter<String> adapter;
    private boolean[] checkedItemsMomentos, checkedItemsTiempos;
    private String[] arrayMomentos, arrayTiempos;
    private ArrayList<String> momentosSeleccionados, tiemposSeleccionados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevocanto);

        etNombre = findViewById(R.id.etNombre);
        etLetra = findViewById(R.id.etLetra);
        bAgregar = findViewById(R.id.bAgregar);
        //listaMomentos = findViewById(R.id.listaMomentos);
        //listaTiempos = findViewById(R.id.listaTiempos);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        momentos = new ArrayList<>();
        momentosSeleccionados = new ArrayList<>();
        tiempos = new ArrayList<>();
        tiemposSeleccionados = new ArrayList<>();

        inicializarFirebase();

        Bundle datos = this.getIntent().getExtras();

        tipo = datos.getInt("getTipo");

        switch (tipo) {
            case 0:
                actionBar.setTitle(R.string.nuevo_canto);
                nuevoCanto();
                break;
            case 1:
                actionBar.setTitle(R.string.modificar_canto);
                modificarCanto(datos);
                break;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_nuevo,menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.escanear:
                Toast.makeText(this, "Proximamente", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private void inicializarFirebase() {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();

        dr = fd.getReference("Momentos");

        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    momentos.add(Objects.requireNonNull(objSnap.child("momento").getValue()).toString());
                }

                arrayMomentos = new String[momentos.size()];
                for (int i = 0; i < arrayMomentos.length; i++) arrayMomentos[i] = momentos.get(i);
                checkedItemsMomentos = new boolean[arrayMomentos.length];
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dr = fd.getReference("Tiempos");
        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    tiempos.add(Objects.requireNonNull(objSnap.child("tiempo").getValue()).toString());
                }

                arrayTiempos = new String[tiempos.size()];
                for (int i = 0; i < arrayTiempos.length; i++) arrayTiempos[i] = tiempos.get(i);
                checkedItemsTiempos = new boolean[arrayTiempos.length];
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dr = fd.getReference("Canto");
    }

    private void nuevoCanto() {
        bAgregar.setText(R.string.guardar);

        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    maxid = (snapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void modificarCanto(Bundle datos) {
        bAgregar.setText(R.string.modificar);

        canto = new Canto();
        canto.setId(datos.getString("getID"));
        canto.setNombre(datos.getString("getNombre"));
        canto.setLetra(datos.getString("getLetra"));
        canto.setMomentos(datos.getStringArrayList("getMomentos"));
        canto.setTiempos(datos.getStringArrayList("getTiempos"));

        etNombre.setText(canto.getNombre());
        etLetra.setText(canto.getLetra());

        //Toast.makeText(this, arrayMomentos.length, Toast.LENGTH_SHORT).show();

        /*for (int i = 0; i < arrayMomentos.length; i++) {

        }
        if(arrayMomentos[i].equals(canto.getMomentos().get(i))) {
            checkedItemsMomentos[i] = true;
        }*/
    }

    @Override
    public void onBackPressed() {
        switch (tipo) {
            case 0:
                regresar_lista();
                break;
            case 1:
                verCanto();
                break;
        }
    }

    private void regresar_lista() {
        Intent i = new Intent(NuevoCantoActivity.this, ListaCantosActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.right_in,R.anim.right_out);
        finish();
    }

    private void verCanto() {
        Intent i = new Intent(NuevoCantoActivity.this, CantoActivity.class);
        i.putExtra("getID",canto.getId());
        i.putExtra("getNombre",canto.getNombre());
        i.putExtra("getLetra", canto.getLetra());
        i.putExtra("getMomentos", canto.getMomentos());
        i.putExtra("getTiempos", canto.getTiempos());
        startActivity(i);
        overridePendingTransition(R.anim.right_in,R.anim.right_out);
        finish();
    }

    public void agregarCanto(View view) {
        if(etNombre.getText().toString().equals("")) {
            etNombre.setError("Requerido");
            return;
        }

        if(etLetra.getText().toString().equals("")) {
            etLetra.setError("Requerido");
            return;
        }

        Canto canto_mod = new Canto();
        canto_mod.setNombre(etNombre.getText().toString());
        canto_mod.setLetra(etLetra.getText().toString());
        canto_mod.setMomentos(momentosSeleccionados);
        canto_mod.setTiempos(tiemposSeleccionados);

        switch (tipo) {
            case 0:
                canto_mod.setId(String.valueOf(maxid + 1));

                dr.child(canto_mod.getId()).setValue(canto_mod);

                Toast.makeText(this, "Canto agregado", Toast.LENGTH_SHORT).show();

                etNombre.setText("");
                etLetra.setText("");

                break;
            case 1:
                canto_mod.setId(canto.getId());

                dr.child(canto_mod.getId()).setValue(canto_mod);

                Toast.makeText(this, "Canto modificado", Toast.LENGTH_SHORT).show();

                canto = canto_mod;

                verCanto();
                break;
        }
    }

    public void mostrarMomentos(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Momentos:");

        builder.setMultiChoiceItems(arrayMomentos, checkedItemsMomentos, (dialog, which, isChecked) -> {

        });

        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> {


            for (int i = 0; i < checkedItemsMomentos.length; i++) {
                if(checkedItemsMomentos[i]) {
                    momentosSeleccionados.add(arrayMomentos[i]);
                }
            }

        });

        builder.setNegativeButton(R.string.cancelar, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void mostrarTiempos(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tiempos:");

        builder.setMultiChoiceItems(arrayTiempos, checkedItemsTiempos, (dialog, which, isChecked) -> {

        });

        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> {

            for (int i = 0; i < checkedItemsTiempos.length; i++) {
                if(checkedItemsTiempos[i]) {
                    tiemposSeleccionados.add(arrayTiempos[i]);
                }
            }

        });

        builder.setNegativeButton(R.string.cancelar, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}