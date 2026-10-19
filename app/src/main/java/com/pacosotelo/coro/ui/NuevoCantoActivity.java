package com.pacosotelo.coro.ui;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class NuevoCantoActivity extends AppCompatActivity {
    private EditText etNombre, etAutor, etLetra;
    private AutoCompleteTextView etTono;
    private Button bAgregar;
    private DatabaseReference dr;
//    private long maxid = 0;
    private int tipo = 0;
    private Canto canto;
    private ArrayList<String> momentos, tiempos;
    //private ListView listaMomentos, listaTiempos;
    private ArrayAdapter<String> adapter;
    private boolean[] checkedItemsMomentos, checkedItemsTiempos;
    private String[] arrayMomentos, arrayTiempos;
    private ArrayList<String> momentosSeleccionados, tiemposSeleccionados;
    private boolean comillas = true;
    private boolean isHighlighting = false;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Uncomment when androidx.edge:edge is available
        setContentView(R.layout.activity_nuevocanto);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etNombre = findViewById(R.id.etNombre);
        etAutor = findViewById(R.id.etAutor);
        etTono = findViewById(R.id.etTono);
        etLetra = findViewById(R.id.etLetra);
        bAgregar = findViewById(R.id.bAgregar);

        if (etTono != null) {
            ArrayAdapter<String> adapterTonos = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Constantes.tonos);
            etTono.setAdapter(adapterTonos);
        }

        if (etLetra != null) {
            etLetra.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    resaltarSintaxis(s);
                }
            });
        }
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
                    resaltarSintaxis(etLetra.getText());
                }
                break;

            case R.id.transportarTono:
                mostrarDialogoTransportar();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        handleBackPress();
        return false;
    }

    private void inicializarFirebase() {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();

        dr = fd.getReference("momentos");

        dr.addListenerForSingleValueEvent(new ValueEventListener() {
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
                Log.e("FIREBASE_TRACE", "=====================> ERROR OBTENIENDO MOMENTOS", error.toException());
            }
        });

        dr = fd.getReference("tiempos");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
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
                Log.e("FIREBASE_TRACE", "=====================> ERROR OBTENIENDO TIEMPOS", error.toException());
            }
        });

        dr = fd.getReference("cantos");
    }

    private void nuevoCanto() {
        bAgregar.setText(R.string.guardar);

//        dr.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()) {
//                    maxid = (snapshot.getChildrenCount());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    private void modificarCanto() {
        bAgregar.setText(R.string.modificar);

        canto = (Canto) this.getIntent().getSerializableExtra("canto");

        if (canto != null) {
            if (canto.getNombre() != null) etNombre.setText(canto.getNombre());
            if (canto.getAutor() != null && etAutor != null) etAutor.setText(canto.getAutor());
            if (canto.getTono() != null && etTono != null) etTono.setText(canto.getTono());
            if (canto.getLetra() != null) {
                etLetra.setText(canto.getLetra());
                resaltarSintaxis(etLetra.getText());
            }

            if (canto.getMomentos() != null) {
                momentosSeleccionados.clear();
                momentosSeleccionados.addAll(canto.getMomentos());
            }

            if (canto.getTiempos() != null) {
                tiemposSeleccionados.clear();
                tiemposSeleccionados.addAll(canto.getTiempos());
            }
            actualizarEtiquetasVisibles();
        }
    }

    private void handleBackPress() {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, R.anim.right_in, R.anim.right_out);
        } else {
            overridePendingTransition(R.anim.right_in, R.anim.right_out);
        }
    }

    private void verCanto() {
        Intent i = new Intent(NuevoCantoActivity.this, CantoActivity.class);
        i.putExtra("canto", canto);
        startActivity(i);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.right_in, R.anim.right_out);
        } else {
            overridePendingTransition(R.anim.right_in, R.anim.right_out);
        }
        finish();
    }

    private void mostrarDialogoTransportar() {
        String tonoRegistrado = null;
        if (etTono != null && etTono.getText() != null && !etTono.getText().toString().trim().isEmpty()) {
            tonoRegistrado = etTono.getText().toString().trim();
        } else if (canto != null && canto.getTono() != null && !canto.getTono().trim().isEmpty()) {
            tonoRegistrado = canto.getTono().trim();
        }

        if (tonoRegistrado == null) {
            new MaterialAlertDialogBuilder(this, R.style.CustomMaterialDialog)
                    .setTitle("Tono Requerido")
                    .setMessage("Para poder transportar este canto a otro tono, primero debes ingresar o seleccionar su tono original en el campo 'Tono / Clave'.")
                    .setPositiveButton("Aceptar", null)
                    .show();
            return;
        }

        String tonoOriginalBruto = tonoRegistrado;
        String tonoOriginal = tonoOriginalBruto.replaceAll("[mM]$", "");
        String sufijo = tonoOriginalBruto.endsWith("m") || tonoOriginalBruto.endsWith("M") ? tonoOriginalBruto.substring(tonoOriginalBruto.length() - 1) : "";

        String[] tonosDisponibles = {"DO", "DO#", "RE", "MIb", "MI", "FA", "FA#", "SOL", "SOL#", "LA", "SIb", "SI"};
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomMaterialDialog);
        builder.setTitle("Transportar a otro tono");

        int checkedItem = -1;
        for (int i = 0; i < tonosDisponibles.length; i++) {
            if (tonosDisponibles[i].equals(tonoOriginal)) {
                checkedItem = i;
                break;
            }
        }

        builder.setSingleChoiceItems(tonosDisponibles, checkedItem, (dialog, which) -> {
            String tonoDestino = tonosDisponibles[which];
            transportarTonoDirecto(tonoOriginal, tonoDestino);
            
            if (etTono != null) {
                etTono.setText(tonoDestino + sufijo);
            }
            if (canto != null) {
                canto.setTono(tonoDestino + sufijo);
            }
            dialog.dismiss();
            Toast.makeText(this, "Tono transportado a " + tonoDestino + sufijo, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void transportarTonoDirecto(String original, String destino) {
        String[] escala = {"DO", "DO#", "RE", "MIb", "MI", "FA", "FA#", "SOL", "SOL#", "LA", "SIb", "SI"};
        int idxOriginal = -1;
        int idxDestino = -1;
        
        for (int i = 0; i < escala.length; i++) {
            if (escala[i].equals(original)) idxOriginal = i;
            if (escala[i].equals(destino)) idxDestino = i;
        }
        
        if (idxOriginal == -1 || idxDestino == -1) return;
        
        int diferencia = idxDestino - idxOriginal;
        if (diferencia < 0) diferencia += 12;

        String letraActual = etLetra.getText().toString();
        for (int i = 0; i < diferencia; i++) {
            letraActual = subirMedioTono(letraActual);
        }
        etLetra.setText(letraActual);
        resaltarSintaxis(etLetra.getText());
    }

    private String subirMedioTono(String texto) {
        String[] palabras = texto.split("'");
        StringBuilder nuevaLetra = new StringBuilder();

        Map<String, String> tonosArriba = new HashMap<>();
        for (String extra : Constantes.extras) {
            tonosArriba.put("DO" + extra, "DO#" + extra);
            tonosArriba.put("DO#" + extra, "RE" + extra);
            tonosArriba.put("REb" + extra, "RE" + extra);
            tonosArriba.put("RE" + extra, "MIb" + extra);
            tonosArriba.put("RE#" + extra, "MI" + extra);
            tonosArriba.put("MIb" + extra, "MI" + extra);
            tonosArriba.put("MI" + extra, "FA" + extra);
            tonosArriba.put("FA" + extra, "FA#" + extra);
            tonosArriba.put("FA#" + extra, "SOL" + extra);
            tonosArriba.put("SOLb" + extra, "SOL" + extra);
            tonosArriba.put("SOL" + extra, "SOL#" + extra);
            tonosArriba.put("SOL#" + extra, "LA" + extra);
            tonosArriba.put("LAb" + extra, "LA" + extra);
            tonosArriba.put("LA" + extra, "SIb" + extra);
            tonosArriba.put("LA#" + extra, "SI" + extra);
            tonosArriba.put("SIb" + extra, "SI" + extra);
            tonosArriba.put("SI" + extra, "DO" + extra);
        }

        for (String palabra : palabras) {
            if(tonosArriba.get(palabra) != null) {
                nuevaLetra.append("'").append(tonosArriba.get(palabra)).append("'");
            } else {
                nuevaLetra.append(palabra);
            }
        }
        return nuevaLetra.toString();
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

        String app = switch (Constantes.GRUPO_SELECCIONADO) {
            case "1ddb5b17-58ec-47aa-a6f5-297b26b06c2b" -> "SJTJ";
            case "2920155f-81ad-4ad6-9706-e92c8a057408" -> "SAP";
            case "816df4f3-542a-477a-b508-9b990f01ba47" -> "SJT";
            default -> "";
        };

        Canto canto_mod = new Canto();
        canto_mod.setApp(app);
        canto_mod.setGrupo_id(Constantes.GRUPO_SELECCIONADO);
        canto_mod.setNombre(etNombre.getText().toString().trim());
        if (etAutor != null) canto_mod.setAutor(etAutor.getText().toString().trim());
        if (etTono != null) canto_mod.setTono(etTono.getText().toString().trim());
        canto_mod.setLetra(etLetra.getText().toString().trim());
        canto_mod.setMomentos(momentosSeleccionados);
        canto_mod.setTiempos(tiemposSeleccionados);

        LocalDateTime fechaActual = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fechaActual = LocalDateTime.now();
        }

        switch (tipo) {
            case 0:
                canto_mod.setId(UUID.randomUUID().toString());

                assert currentUser != null;
                canto_mod.setCreado_por(currentUser.getUid());
                canto_mod.setModificado_por(currentUser.getUid());
                canto_mod.setFecha_creacion(fechaActual.toString());

                dr.child(canto_mod.getId()).setValue(canto_mod)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FIREBASE_TRACE", "=====================> DATOS GUARDADOS CON ÉXITO: Canto (" + canto_mod.getId() + ")");
                            Toast.makeText(this, "Canto agregado", Toast.LENGTH_SHORT).show();
                            
                            etNombre.setText("");
                            etLetra.setText("");

                            // Limpiar las listas de momentos y tiempos
                            momentosSeleccionados.clear();
                            tiemposSeleccionados.clear();

                            // Limpiar las selecciones de los checkboxes
                            for (int i = 0; i < checkedItemsMomentos.length; i++) {
                                checkedItemsMomentos[i] = false;
                            }
                            for (int i = 0; i < checkedItemsTiempos.length; i++) {
                                checkedItemsTiempos[i] = false;
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FIREBASE_TRACE", "=====================> ERROR GUARDANDO DATOS: Canto (" + canto_mod.getId() + ")", e);
                            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

                break;
            case 1:
                canto_mod.setId(canto.getId());

                assert currentUser != null;
                canto_mod.setModificado_por(currentUser.getUid());
                canto_mod.setFecha_modificacion(fechaActual.toString());

                dr.child(canto_mod.getId()).setValue(canto_mod)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FIREBASE_TRACE", "=====================> DATOS ACTUALIZADOS CON ÉXITO: Canto (" + canto_mod.getId() + ")");
                            Toast.makeText(this, "Canto modificado", Toast.LENGTH_SHORT).show();

                            canto = canto_mod;

                            verCanto();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FIREBASE_TRACE", "=====================> ERROR ACTUALIZANDO DATOS: Canto (" + canto_mod.getId() + ")", e);
                            Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                break;
        }
    }

    private void actualizarEtiquetasVisibles() {
        TextView tvMomentosSeleccionados = findViewById(R.id.tvMomentosSeleccionados);
        TextView tvTiemposSeleccionados = findViewById(R.id.tvTiemposSeleccionados);

        if (tvMomentosSeleccionados != null) {
            if (momentosSeleccionados != null && !momentosSeleccionados.isEmpty()) {
                tvMomentosSeleccionados.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tvMomentosSeleccionados.setText("Momentos: " + String.join(", ", momentosSeleccionados));
                } else {
                    tvMomentosSeleccionados.setText("Momentos: " + momentosSeleccionados.toString().replaceAll("[\\[\\]]", ""));
                }
            } else {
                tvMomentosSeleccionados.setVisibility(View.GONE);
            }
        }

        if (tvTiemposSeleccionados != null) {
            if (tiemposSeleccionados != null && !tiemposSeleccionados.isEmpty()) {
                tvTiemposSeleccionados.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tvTiemposSeleccionados.setText("Tiempos: " + String.join(", ", tiemposSeleccionados));
                } else {
                    tvTiemposSeleccionados.setText("Tiempos: " + tiemposSeleccionados.toString().replaceAll("[\\[\\]]", ""));
                }
            } else {
                tvTiemposSeleccionados.setVisibility(View.GONE);
            }
        }
    }

    public void mostrarMomentos(View view) {
        if (arrayMomentos == null) {
            Toast.makeText(this, "Cargando momentos...", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Momentos:");

        if (checkedItemsMomentos != null && arrayMomentos != null) {
            for (int i = 0; i < arrayMomentos.length; i++) {
                checkedItemsMomentos[i] = momentosSeleccionados.contains(arrayMomentos[i]);
            }
        }

        builder.setMultiChoiceItems(arrayMomentos, checkedItemsMomentos, (dialog, which, isChecked) -> {
            if (checkedItemsMomentos != null && which < checkedItemsMomentos.length) {
                checkedItemsMomentos[which] = isChecked;
            }
        });

        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> {
            momentosSeleccionados.clear();
            if (checkedItemsMomentos != null && arrayMomentos != null) {
                for (int i = 0; i < checkedItemsMomentos.length; i++) {
                    if (checkedItemsMomentos[i]) {
                        momentosSeleccionados.add(arrayMomentos[i]);
                    }
                }
            }
            actualizarEtiquetasVisibles();
        });

        builder.setNegativeButton(R.string.cancelar, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void mostrarTiempos(View view) {
        if (arrayTiempos == null) {
            Toast.makeText(this, "Cargando tiempos...", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tiempos:");

        if (checkedItemsTiempos != null && arrayTiempos != null) {
            for (int i = 0; i < arrayTiempos.length; i++) {
                checkedItemsTiempos[i] = tiemposSeleccionados.contains(arrayTiempos[i]);
            }
        }

        builder.setMultiChoiceItems(arrayTiempos, checkedItemsTiempos, (dialog, which, isChecked) -> {
            if (checkedItemsTiempos != null && which < checkedItemsTiempos.length) {
                checkedItemsTiempos[which] = isChecked;
            }
        });

        builder.setPositiveButton(R.string.aceptar, (dialog, which) -> {
            tiemposSeleccionados.clear();
            if (checkedItemsTiempos != null && arrayTiempos != null) {
                for (int i = 0; i < checkedItemsTiempos.length; i++) {
                    if (checkedItemsTiempos[i]) {
                        tiemposSeleccionados.add(arrayTiempos[i]);
                    }
                }
            }
            actualizarEtiquetasVisibles();
        });

        builder.setNegativeButton(R.string.cancelar, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void resaltarSintaxis(Editable s) {
        if (s == null || isHighlighting) return;
        isHighlighting = true;

        ForegroundColorSpan[] oldSpans = s.getSpans(0, s.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : oldSpans) {
            s.removeSpan(span);
        }
        StyleSpan[] oldStyleSpans = s.getSpans(0, s.length(), StyleSpan.class);
        for (StyleSpan span : oldStyleSpans) {
            s.removeSpan(span);
        }

        // 1. Resaltar acordes ('DO', 'SOLm')
        Pattern patternChord = Pattern.compile("'([^']+)'");
        Matcher matcherChord = patternChord.matcher(s.toString());
        int chordColor = ContextCompat.getColor(this, R.color.chord_highlight_color);

        while (matcherChord.find()) {
            s.setSpan(new ForegroundColorSpan(chordColor), matcherChord.start(), matcherChord.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new StyleSpan(Typeface.BOLD), matcherChord.start(), matcherChord.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 2. Resaltar señas (INTRO, CORO, VIOLIN, 2 VECES, etc.)
        Pattern patternDirective = Pattern.compile("\\b(INTRO|CORO|ESTROFA|PUENTE|OUTRO|FINAL|SOLO|INTERLUDIO|VIOLIN|VIOLÍN|GUITARRA|MANDOLINA|FLAUTA|PIANO|TROMPETA|TONO|TONOS|BIS|\\d+\\s*VECES|\\d+X)\\b|[\\[\\(][^\\]\\)]+[\\]\\)]");
        Matcher matcherDirective = patternDirective.matcher(s.toString());
        int directiveColor = ContextCompat.getColor(this, R.color.directive_highlight_color);

        while (matcherDirective.find()) {
            s.setSpan(new ForegroundColorSpan(directiveColor), matcherDirective.start(), matcherDirective.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), matcherDirective.start(), matcherDirective.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        isHighlighting = false;
    }
}