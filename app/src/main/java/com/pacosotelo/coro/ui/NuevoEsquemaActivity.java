package com.pacosotelo.coro.ui;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.tools.Constantes;
import com.pacosotelo.coro.tools.Transiciones;
import androidx.lifecycle.ViewModelProvider;
import com.pacosotelo.coro.viewmodel.EsquemasViewModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pacosotelo.coro.tools.CantoEsquemaEditAdapter;

public class NuevoEsquemaActivity extends AppCompatActivity {
    Button bAgregarCanto, bGuardarEsquema;
    EditText etNombreEsquema;
    RecyclerView rvCantosEsquema;
    DatabaseReference dr;
    FirebaseDatabase fd;
    private final ArrayList<Canto> listaCantos = new ArrayList<>();
    CantoEsquemaEditAdapter adapter2;
    private EsquemasViewModel esquemasViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Uncomment when androidx.edge:edge is available
        setContentView(R.layout.activity_nuevo_esquema);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                regresar();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(getString(R.string.titulo_nuevo_esquema));
        actionBar.setDisplayHomeAsUpEnabled(true);

        fd = FirebaseDatabase.getInstance();
        dr = fd.getReference();

        bAgregarCanto = findViewById(R.id.bAgregarCanto);
        bGuardarEsquema = findViewById(R.id.bGuardarEsquema);
        etNombreEsquema = findViewById(R.id.etNombreEsquema);
        rvCantosEsquema = findViewById(R.id.rvCantosEsquema);
        rvCantosEsquema.setLayoutManager(new LinearLayoutManager(this));

        esquemasViewModel = new ViewModelProvider(this).get(EsquemasViewModel.class);

        esquemasViewModel.getSaveSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, getString(R.string.msg_esquema_agregado), Toast.LENGTH_SHORT).show();
                regresar();
            }
        });

        esquemasViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        bAgregarCanto.setOnClickListener(v -> agregarCanto());
        bGuardarEsquema.setOnClickListener(v -> guardarEsquema());

        adapter2 = new CantoEsquemaEditAdapter(listaCantos, (view, position) -> {
            if (position >= 0 && position < listaCantos.size()) {
                Intent i = new Intent(this, CantoActivity.class);
                i.putExtra("canto", listaCantos.get(position));
                startActivity(i);
                Transiciones.left(this);
            }
        });
        rvCantosEsquema.setAdapter(adapter2);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();

                if (fromPosition != RecyclerView.NO_POSITION && toPosition != RecyclerView.NO_POSITION) {
                    Collections.swap(listaCantos, fromPosition, toPosition);
                    adapter2.notifyItemMoved(fromPosition, toPosition);
                    return true;
                }
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < listaCantos.size()) {
                    Canto cantoEliminado = listaCantos.remove(position);
                    adapter2.notifyItemRemoved(position);
                    adapter2.notifyItemRangeChanged(position, listaCantos.size());
                    Toast.makeText(NuevoEsquemaActivity.this, cantoEliminado.getNombre() + " removido", Toast.LENGTH_SHORT).show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvCantosEsquema);
    }

    private void agregarCanto() {
        if (Constantes.GRUPO_SELECCIONADO == null || Constantes.GRUPO_SELECCIONADO.trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_grupo_necesario_esquema), Toast.LENGTH_SHORT).show();
            return;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomMaterialDialog);
        builder.setTitle(getString(R.string.titulo_selecciona_canto));

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_searchable_list, null);
        SearchView sv = dialogView.findViewById(R.id.dialogSearchView);
        ListView lv = dialogView.findViewById(R.id.dialogListView);

        ArrayAdapter<Canto> arrayAdapter = new ArrayAdapter<>(this, R.layout.item_canto_esquema, R.id.textView);
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
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                .setNegativeButton(getString(R.string.cancelar), (d, which) -> d.dismiss())
                .create();

        lv.setOnItemClickListener((parent, view, position, id) -> {
            Canto c = arrayAdapter.getItem(position);
            if (c != null) {
                listaCantos.add(c);
                if (adapter2 != null) {
                    adapter2.notifyItemInserted(listaCantos.size() - 1);
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void cantoPulsado(View v, int indice) {
        final PopupMenu popupMenu = new PopupMenu(new ContextThemeWrapper(NuevoEsquemaActivity.this, R.style.CustomPopupMenuStyle), v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_canto_esquema, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.verCanto) {
                Intent i = new Intent(this, CantoActivity.class);
                i.putExtra("canto", listaCantos.get(indice));
                startActivity(i);
                Transiciones.left(this);
                return true;
            } else if (itemId == R.id.eliminarCanto) {
                listaCantos.remove(indice);
                adapter2.notifyItemRemoved(indice);
                adapter2.notifyItemRangeChanged(indice, listaCantos.size());
                return true;
            } else if (itemId == R.id.moverArriba) {
                if (indice > 0) {
                    Canto temp = listaCantos.get(indice);
                    listaCantos.set(indice, listaCantos.get(indice - 1));
                    listaCantos.set(indice - 1, temp);
                    adapter2.notifyItemMoved(indice, indice - 1);
                } else {
                    Toast.makeText(NuevoEsquemaActivity.this, getString(R.string.msg_canto_al_principio), Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.moverAbajo) {
                if (indice < listaCantos.size() - 1) {
                    Canto temp = listaCantos.get(indice);
                    listaCantos.set(indice, listaCantos.get(indice + 1));
                    listaCantos.set(indice + 1, temp);
                    adapter2.notifyItemMoved(indice, indice + 1);
                } else {
                    Toast.makeText(NuevoEsquemaActivity.this, getString(R.string.msg_canto_al_final), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        popupMenu.show();

    }

    private void guardarEsquema() {
        // Validar datos
        if(etNombreEsquema.getText().toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_ingresa_nombre_esquema), Toast.LENGTH_SHORT).show();
            return;
        }

        if(listaCantos.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_agrega_un_canto), Toast.LENGTH_SHORT).show();
            return;
        }

        Esquema esquema = new Esquema();

        LocalDateTime fechaActual = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fechaActual = LocalDateTime.now();
        }

        esquema.setGrupo_id(Constantes.GRUPO_SELECCIONADO);
        esquema.setApp("");
        esquema.setId(UUID.randomUUID().toString());
        esquema.setNombre(etNombreEsquema.getText().toString());

        esquema.setCantos(listaCantos);

        esquema.setFecha_creacion(fechaActual != null ? fechaActual.toString() : "");

        esquemasViewModel.saveEsquema(esquema);
    }

    @Override
    public boolean onSupportNavigateUp() {
        regresar();
        return false;
    }

    private void regresar() {
        finish();
        Transiciones.right(this);
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