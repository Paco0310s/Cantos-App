package com.pacosotelo.coro.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.tools.Constantes;

import java.util.ArrayList;
import java.util.UUID;

public class ModificarEsquemaActivity extends AppCompatActivity {
    Button bAgregarCanto, bModificarEsquema;
    EditText etNombreEsquema;
    ListView rvCantosEsquema;
    DatabaseReference dr;
    FirebaseDatabase fd;
    private final ArrayList<Canto> listaCantos = new ArrayList<>();
    //AdaptadorCantoEsquema adapter2;
    ArrayAdapter<Canto> adapter2;
    Esquema esquema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_esquema);

        esquema = (Esquema) this.getIntent().getSerializableExtra("esquema");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Modificar Esquema");
        actionBar.setDisplayHomeAsUpEnabled(true);

        fd = FirebaseDatabase.getInstance();
        dr = fd.getReference();

        bAgregarCanto = findViewById(R.id.bAgregarCanto);
        bModificarEsquema = findViewById(R.id.bGuardarEsquema);
        etNombreEsquema = findViewById(R.id.etNombreEsquema);
        rvCantosEsquema = findViewById(R.id.rvCantosEsquema);

        adapter2 = new ArrayAdapter<>(this,
                R.layout.item_canto_esquema2);

        etNombreEsquema.setText(esquema.getNombre());

        listaCantos.addAll(esquema.getCantos());
        adapter2.addAll(listaCantos);

        rvCantosEsquema.setAdapter(adapter2);

        bAgregarCanto.setOnClickListener(v -> agregarCanto());
        bModificarEsquema.setOnClickListener(v -> modificarEsquema());

        rvCantosEsquema.setOnItemClickListener((adapterView, view, i, l) -> cantoPulsado(view, i));

    }

    private void cantoPulsado(View v, int indice) {
        final PopupMenu popupMenu = new PopupMenu(ModificarEsquemaActivity.this, v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_canto_esquema, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.verCanto:
                    Intent intent = new Intent(this, ModCantoEsquemaActivity.class);
                    intent.putExtra("canto", esquema.getCantos().get(indice));
                    intent.putExtra("esquema", esquema);
                    intent.putExtra("indice",indice);

                    this.startActivity(intent);
                    this.overridePendingTransition(R.anim.left_in,R.anim.left_out);
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

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_esquema,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.eliminar) {
            dr = fd.getReference("esquemas");
            FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

            if(usuario != null && usuario.getUid().equals("mYW9YLYZPmZdhaSwSS0ONF0EUe53")) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

                builder.setTitle("Confirmar");
                builder.setMessage("¿Desea eliminar el esquema?");

                builder.setPositiveButton("SI", (dialog, which) -> {
                    dr.child(esquema.getId()).removeValue();

                    Toast.makeText(ModificarEsquemaActivity.this, "Esquema eliminado",
                            Toast.LENGTH_SHORT).show();

                    onBackPressed();
                });

                builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());

                android.app.AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(ModificarEsquemaActivity.this, "No tienes permiso para esta operación",
                        Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
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

        builderSingle.setNegativeButton("Cancelar", (dialog, which) ->
                dialog.dismiss());

        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
            Canto c = arrayAdapter.getItem(which);
            listaCantos.add(c);

            adapter2.add(c);
            rvCantosEsquema.setAdapter(adapter2);
        });

        builderSingle.show();
    }

    private void modificarEsquema() {
        Esquema esquemaMod = new Esquema();

        esquemaMod.setId(esquema.getId());
        esquemaMod.setNombre(etNombreEsquema.getText().toString());
        esquemaMod.setCantos(listaCantos);

        dr = fd.getReference();
        dr.child("esquemas").child(esquemaMod.getId()).setValue(esquemaMod);

        Toast.makeText(this, "Esquema modificado", Toast.LENGTH_SHORT).show();

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
}