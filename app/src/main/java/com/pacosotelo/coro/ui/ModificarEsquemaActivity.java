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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
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
        // EdgeToEdge.enable(this); // Uncomment when androidx.edge:edge is available
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
                        Toast.makeText(ModificarEsquemaActivity.this, "El canto ya está al principio", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ModificarEsquemaActivity.this, "El canto ya está al final", Toast.LENGTH_SHORT).show();
                    }
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

            if (usuario == null) {
                Toast.makeText(ModificarEsquemaActivity.this, "Usuario no autenticado",
                        Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }

            if (!esquema.getGrupo_id().equals(Constantes.GRUPO_SELECCIONADO)) {
                Toast.makeText(ModificarEsquemaActivity.this, "No puede eliminar este esquema",
                        Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }

            if (usuario.getUid().equals("mYW9YLYZPmZdhaSwSS0ONF0EUe53")) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona un canto");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_searchable_list, null);
        androidx.appcompat.widget.SearchView sv = dialogView.findViewById(R.id.dialogSearchView);
        ListView lv = dialogView.findViewById(R.id.dialogListView);

        ArrayAdapter<Canto> arrayAdapter = new ArrayAdapter<>(this, R.layout.item_canto_esquema);
        lv.setAdapter(arrayAdapter);

        // 1. Declaramos la lista de respaldo local para este diálogo
        final ArrayList<Canto> listaRespaldoDialogo = new ArrayList<>();

        DatabaseReference drCantos = fd.getReference("cantos");
        final String grupoSeleccionado = Constantes.GRUPO_SELECCIONADO.trim();

        drCantos.orderByChild("grupo_id").equalTo(grupoSeleccionado)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        arrayAdapter.clear();
                        listaRespaldoDialogo.clear(); // Limpiamos por seguridad

                        for (DataSnapshot objSnap : snapshot.getChildren()) {
                            if (objSnap != null) {
                                try {
                                    Canto c = objSnap.getValue(Canto.class);
                                    if (c != null) {
                                        arrayAdapter.add(c);
                                        listaRespaldoDialogo.add(c); // <--- Guardamos una copia intacta aquí
                                    }
                                } catch (Exception e) {
                                    Log.e("Error_Firebase", "Llave con error en diálogo: " + objSnap.getKey(), e);
                                }
                            }
                        }

                        arrayAdapter.sort((c1, c2) -> c1.getNombre().compareToIgnoreCase(c2.getNombre()));
                        // También ordenamos la de respaldo para que al borrar la búsqueda coincida el orden
                        listaRespaldoDialogo.sort((c1, c2) -> c1.getNombre().compareToIgnoreCase(c2.getNombre()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase_Error", "Cancelado en diálogo: " + error.getMessage());
                    }
                });

        // 2. Vinculamos el buscador con nuestra función personalizada usando la lista de respaldo
        sv.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String query = newText.toLowerCase().trim();
                arrayAdapter.clear();

                if (query.isEmpty()) {
                    // Si el buscador se vacía, restauramos todos los cantos desde el respaldo
                    arrayAdapter.addAll(listaRespaldoDialogo);
                } else {
                    // Filtrado inteligente que no se rompe con espacios
                    for (Canto canto : listaRespaldoDialogo) {
                        if (canto != null && canto.getNombre() != null) {
                            String nombreCanto = canto.getNombre().toLowerCase();
                            if (nombreCanto.contains(query)) {
                                arrayAdapter.add(canto);
                            }
                        }
                    }
                }
                arrayAdapter.notifyDataSetChanged();
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
                if (adapter2 != null) {
                    adapter2.add(c);
                    adapter2.notifyDataSetChanged();
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void modificarEsquema() {
        Esquema esquemaMod = new Esquema();

        LocalDateTime fechaActual = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fechaActual = LocalDateTime.now();
        }

        String app = switch (Constantes.GRUPO_SELECCIONADO) {
            case "1ddb5b17-58ec-47aa-a6f5-297b26b06c2b" -> "SJTJ";
            case "2920155f-81ad-4ad6-9706-e92c8a057408" -> "SAP";
            case "816df4f3-542a-477a-b508-9b990f01ba47" -> "SJT";
            default -> "";
        };

        esquemaMod.setApp(app);
        esquemaMod.setId(esquema.getId());
        esquemaMod.setNombre(etNombreEsquema.getText().toString());
        esquemaMod.setCantos(listaCantos);
        esquemaMod.setFecha_modificacion(fechaActual != null ? fechaActual.toString() : "");
        esquemaMod.setGrupo_id(Constantes.GRUPO_SELECCIONADO);

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