package com.pacosotelo.coro.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.tools.Constantes;

public class ModCantoEsquemaActivitySJT extends AppCompatActivity {
    private EditText etNombre, etLetra;
    private DatabaseReference dr;
    private Canto canto;
    private Esquema esquema;
    private int indice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_canto_esquema_sjt);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etNombre = findViewById(R.id.etNombre);
        etLetra = findViewById(R.id.etLetra);
        Button bAgregar = findViewById(R.id.bAgregar);

        canto = (Canto) this.getIntent().getSerializableExtra("canto");
        esquema = (Esquema) this.getIntent().getSerializableExtra("esquema");
        indice = this.getIntent().getIntExtra("indice",0);

        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        dr = fd.getReference("esquemas-sjt/" + esquema.getId() + "/cantos/" + indice + "/");

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setTitle(R.string.modificar_canto);

        etNombre.setText(canto.getNombre());
        etLetra.setText(canto.getLetra());
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_nuevo,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.comillas) {
            comillas();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.right_in,R.anim.right_out);
    }

    public void modificarCanto(View view) {
        if(etNombre.getText().toString().equals("")) {
            etNombre.setError("Requerido");
            return;
        }

        if(etLetra.getText().toString().equals("")) {
            etLetra.setError("Requerido");
            return;
        }

        canto.setNombre(etNombre.getText().toString());
        canto.setLetra(etLetra.getText().toString());

        dr.setValue(canto);
        Toast.makeText(this, "Canto modificado", Toast.LENGTH_SHORT).show();

        onBackPressed();
    }

    private void comillas() {
        String letra = etLetra.getText().toString();

        boolean comillas = letra.contains("'");

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
    }
}