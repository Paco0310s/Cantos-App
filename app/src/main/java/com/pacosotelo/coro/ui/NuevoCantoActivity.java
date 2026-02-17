package com.pacosotelo.coro.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.tools.Constantes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private boolean comillas = true;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevocanto);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etNombre = findViewById(R.id.etNombre);
        etLetra = findViewById(R.id.etLetra);
        bAgregar = findViewById(R.id.bAgregar);
        //listaMomentos = findViewById(R.id.listaMomentos);
        //listaTiempos = findViewById(R.id.listaTiempos);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        momentos = new ArrayList<>();
        momentosSeleccionados = new ArrayList<>();
        tiempos = new ArrayList<>();
        tiemposSeleccionados = new ArrayList<>();

        inicializarFirebase();

        Bundle datos = this.getIntent().getExtras();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        tipo = datos.getInt("tipo");

        switch (tipo) {
            case 0:
                actionBar.setTitle(R.string.nuevo_canto);
                nuevoCanto();
                break;
            case 1:
                actionBar.setTitle(R.string.modificar_canto);
                modificarCanto();
                break;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_nuevo,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.comillas:

                String letra = etLetra.getText().toString();

                comillas = letra.contains("'");

                if(comillas) {
                    etLetra.setText(letra.replaceAll("'", ""));
                } else {
                    for (String tono: Constantes.tonos) {
                        for(int i = Constantes.extras.length - 1; i >= 0; i--) {
                            String nuevoTono = tono + Constantes.extras[i];
                            letra = letra.replaceAll(nuevoTono,"'" + nuevoTono + "'");
                        }
                    }
                    letra = letra.replaceAll("'''","'");
                    letra = letra.replaceAll("''","'");

                    for (int i = 1; i < Constantes.extras.length; i++) {
                        String nuevoExtra = "'" + Constantes.extras[i];
                        letra = letra.replaceAll(nuevoExtra, Constantes.extras[i]);
                    }

                    letra = letra.replaceAll("'#","#");
                    letra = letra.replaceAll("'b","b");

                    etLetra.setText(letra);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    private void inicializarFirebase() {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();

        dr = fd.getReference("momentos");

        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    momentos.add(Objects.requireNonNull(objSnap.child("momento").getValue()).toString());
                }

                arrayMomentos = new String[momentos.size()];
                for (int i = 0; i < arrayMomentos.length; i++) arrayMomentos[i] = momentos.get(i);
                checkedItemsMomentos = new boolean[arrayMomentos.length];

                if(tipo==1) {
                    for(int i = 0; i < arrayMomentos.length; i++) {
                        for (int j = 0; j < canto.getMomentos().size(); j++) {
                            if (Objects.equals(arrayMomentos[i], canto.getMomentos().get(j))) checkedItemsMomentos[i] = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dr = fd.getReference("tiempos");
        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    tiempos.add(Objects.requireNonNull(objSnap.child("tiempo").getValue()).toString());
                }

                arrayTiempos = new String[tiempos.size()];
                for (int i = 0; i < arrayTiempos.length; i++) arrayTiempos[i] = tiempos.get(i);
                checkedItemsTiempos = new boolean[arrayTiempos.length];

                if(tipo==1) {
                    for(int i = 0; i < arrayTiempos.length; i++) {
                        for (int j = 0; j < canto.getTiempos().size(); j++) {
                            if (Objects.equals(arrayTiempos[i], canto.getTiempos().get(j))) checkedItemsTiempos[i] = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dr = fd.getReference("cantos");
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

    private void modificarCanto() {
        bAgregar.setText(R.string.modificar);

        canto = (Canto) this.getIntent().getSerializableExtra("canto");

        etNombre.setText(canto.getNombre());
        etLetra.setText(canto.getLetra());

        //Toast.makeText(this, arrayMomentos.length, Toast.LENGTH_SHORT).show();

        /*for (int i = 0; i < arrayMomentos.length; i++) {
            if(arrayMomentos[i].equals(canto.getMomentos().get(i))) {
                checkedItemsMomentos[i] = true;
            }
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
        finish();
        overridePendingTransition(R.anim.right_in,R.anim.right_out);
    }

    private void verCanto() {
        Intent i = new Intent(NuevoCantoActivity.this, CantoActivity.class);
        i.putExtra("canto",canto);
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
        canto_mod.setApp(Constantes.APP);
        canto_mod.setNombre(etNombre.getText().toString());
        canto_mod.setLetra(etLetra.getText().toString());
        canto_mod.setMomentos(momentosSeleccionados);
        canto_mod.setTiempos(tiemposSeleccionados);

        LocalDateTime fechaActual = LocalDateTime.now();

        switch (tipo) {
            case 0:
                canto_mod.setId(String.valueOf(maxid + 1));

                assert currentUser != null;
                canto_mod.setCreado_por(currentUser.getUid());
                canto_mod.setModificado_por(currentUser.getUid());
                canto_mod.setFecha_creacion(fechaActual.toString());

                dr.child(canto_mod.getId()).setValue(canto_mod);

                Toast.makeText(this, "Canto agregado", Toast.LENGTH_SHORT).show();

                etNombre.setText("");
                etLetra.setText("");

                break;
            case 1:
                canto_mod.setId(canto.getId());

                assert currentUser != null;
                canto_mod.setModificado_por(currentUser.getUid());
                canto_mod.setFecha_modificacion(fechaActual.toString());

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