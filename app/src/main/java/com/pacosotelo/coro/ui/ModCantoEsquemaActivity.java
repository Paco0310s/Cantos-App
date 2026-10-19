package com.pacosotelo.coro.ui;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.tools.Constantes;
import com.pacosotelo.coro.tools.Transiciones;
import com.pacosotelo.coro.viewmodel.EsquemasViewModel;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModCantoEsquemaActivity extends AppCompatActivity {
    private EditText etNombre, etLetra;
    private Canto canto;
    private Esquema esquema;
    private int indice;
    private EsquemasViewModel esquemasViewModel;
    private boolean isHighlighting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Uncomment when androidx.edge:edge is available
        setContentView(R.layout.activity_mod_canto_esquema);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                regresar();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etNombre = findViewById(R.id.etNombre);
        etLetra = findViewById(R.id.etLetra);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            canto = this.getIntent().getSerializableExtra("canto", Canto.class);
            esquema = this.getIntent().getSerializableExtra("esquema", Esquema.class);
        } else {
            canto = (Canto) this.getIntent().getSerializableExtra("canto");
            esquema = (Esquema) this.getIntent().getSerializableExtra("esquema");
        }
        indice = this.getIntent().getIntExtra("indice", 0);

        esquemasViewModel = new ViewModelProvider(this).get(EsquemasViewModel.class);

        esquemasViewModel.getSaveSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, getString(R.string.msg_canto_modificado), Toast.LENGTH_SHORT).show();
                regresar();
            }
        });

        esquemasViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.modificar_canto);

        if (canto != null) {
            if (canto.getNombre() != null) etNombre.setText(canto.getNombre());
            if (canto.getLetra() != null) {
                etLetra.setText(canto.getLetra());
                resaltarSintaxis(etLetra.getText());
            }
        }

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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nuevo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.comillas) {
            comillas();
            return true;
        } else if (item.getItemId() == R.id.transportarTono) {
            mostrarDialogoTransportar();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    public void modificarCanto(View view) {
        if (etNombre.getText().toString().trim().isEmpty()) {
            etNombre.setError(getString(R.string.msg_requerido));
            return;
        }

        if (etLetra.getText().toString().trim().isEmpty()) {
            etLetra.setError(getString(R.string.msg_requerido));
            return;
        }

        if (canto != null && esquema != null && esquema.getCantos() != null) {
            canto.setNombre(etNombre.getText().toString().trim());
            canto.setLetra(etLetra.getText().toString().trim());

            if (indice >= 0 && indice < esquema.getCantos().size()) {
                esquema.getCantos().set(indice, canto);
                esquemasViewModel.saveEsquema(esquema);
            }
        }
    }

    private void comillas() {
        String letra = etLetra.getText().toString();
        boolean comillas = letra.contains("'");

        if (comillas) {
            etLetra.setText(letra.replace("'", ""));
        } else {
            for (String tono : Constantes.tonos) {
                for (int i = Constantes.extras.length - 1; i >= 0; i--) {
                    String nuevoTono = tono + Constantes.extras[i];
                    letra = letra.replace(nuevoTono, "'" + nuevoTono + "'");
                }
            }
            letra = letra.replace("'''", "'");
            letra = letra.replace("''", "'");

            for (int i = 1; i < Constantes.extras.length; i++) {
                String nuevoExtra = "'" + Constantes.extras[i];
                letra = letra.replace(nuevoExtra, Constantes.extras[i]);
            }

            letra = letra.replace("'#", "#");
            letra = letra.replace("'b", "b");

            etLetra.setText(letra);
        }
        resaltarSintaxis(etLetra.getText());
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

    private void mostrarDialogoTransportar() {
        if (canto == null || canto.getTono() == null || canto.getTono().trim().isEmpty()) {
            new MaterialAlertDialogBuilder(this, R.style.CustomMaterialDialog)
                    .setTitle("Tono Requerido")
                    .setMessage("Para poder transportar este canto a otro tono, primero debes registrar su tono original.")
                    .setPositiveButton("Aceptar", null)
                    .show();
            return;
        }

        String tonoOriginalBruto = canto.getTono().trim();
        String tonoOriginal = tonoOriginalBruto.replaceAll("[mM]$", "");

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
            dialog.dismiss();
            Toast.makeText(this, "Tono transportado a " + tonoDestino, Toast.LENGTH_SHORT).show();
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
        canto.setTono(destino); // actualizamos el tono del objeto
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
}
