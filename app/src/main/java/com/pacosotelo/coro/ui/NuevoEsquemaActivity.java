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
import android.util.Log;
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
import com.pacosotelo.coro.tools.Constantes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona un canto");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_searchable_list, null);
        androidx.appcompat.widget.SearchView sv = dialogView.findViewById(R.id.dialogSearchView);
        ListView lv = dialogView.findViewById(R.id.dialogListView);

        ArrayAdapter<Canto> arrayAdapter = new ArrayAdapter<>(this, R.layout.item_canto_esquema);
        lv.setAdapter(arrayAdapter);

        dr = fd.getReference("cantos");
        dr.orderByChild("grupo_id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayAdapter.clear();

                for (DataSnapshot objSnap : snapshot.getChildren()) {
                    if(objSnap!=null) {
                        try {
                            Canto c = objSnap.getValue(Canto.class);

                            if (c != null && Constantes.GRUPO_SELECCIONADO.equals(c.getGrupo_id())) {
                                arrayAdapter.add(c);
                            }
                        } catch (Exception e) {
                            Log.e("Error", Objects.requireNonNull(objSnap.getKey()));
                        }
                    }
                }

                // Ordenar los cantos alfabéticamente por nombre
                arrayAdapter.sort((c1, c2) -> c1.getNombre().compareTo(c2.getNombre()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sv.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                arrayAdapter.getFilter().filter(newText);
                return true;
            }
        });

        AlertDialog dialog = builder.setView(dialogView)
                .setNegativeButton("Cancelar", (d, which) -> d.dismiss())
                .create();

        lv.setOnItemClickListener((parent, view, position, id) -> {
            Canto c = arrayAdapter.getItem(position);
            if (c != null) {
                listaCantos.add(c);
                adapter2.add(c);
                rvCantosEsquema.setAdapter(adapter2);
            }
            dialog.dismiss();
        });

        dialog.show();
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
                case R.id.moverArriba:
                    if (indice > 0) {
                        Canto temp = listaCantos.get(indice);
                        listaCantos.set(indice, listaCantos.get(indice - 1));
                        listaCantos.set(indice - 1, temp);
                        adapter2.clear();
                        adapter2.addAll(listaCantos);
                        rvCantosEsquema.setAdapter(adapter2);
                        rvCantosEsquema.setSelection(indice - 1);
                    } else {
                        Toast.makeText(NuevoEsquemaActivity.this, "El canto ya está al principio", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                case R.id.moverAbajo:
                    if (indice < listaCantos.size() - 1) {
                        Canto temp = listaCantos.get(indice);
                        listaCantos.set(indice, listaCantos.get(indice + 1));
                        listaCantos.set(indice + 1, temp);
                        adapter2.clear();
                        adapter2.addAll(listaCantos);
                        rvCantosEsquema.setAdapter(adapter2);
                        rvCantosEsquema.setSelection(indice + 1);
                    } else {
                        Toast.makeText(NuevoEsquemaActivity.this, "El canto ya está al final", Toast.LENGTH_SHORT).show();
                    }
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

        LocalDateTime fechaActual = LocalDateTime.now();

        String app = switch (Constantes.GRUPO_SELECCIONADO) {
            case "1ddb5b17-58ec-47aa-a6f5-297b26b06c2b" -> "SJTJ";
            case "2920155f-81ad-4ad6-9706-e92c8a057408" -> "SAP";
            case "816df4f3-542a-477a-b508-9b990f01ba47" -> "SJT";
            default -> "";
        };

        esquema.setGrupo_id(Constantes.GRUPO_SELECCIONADO);
        esquema.setApp(app);
        esquema.setId(UUID.randomUUID().toString());
        esquema.setNombre(etNombreEsquema.getText().toString());

        esquema.setCantos(listaCantos);

        esquema.setFecha_creacion(fechaActual.toString());

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