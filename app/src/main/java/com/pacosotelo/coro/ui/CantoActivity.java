package com.pacosotelo.coro.ui;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.lifecycle.ViewModelProvider;
import com.pacosotelo.coro.viewmodel.CantosViewModel;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.tools.Constantes;
import com.pacosotelo.coro.tools.Transiciones;
import com.pacosotelo.coro.tools.TemplatePDF;
import com.pacosotelo.coro.modelos.Canto;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class CantoActivity extends AppCompatActivity {
    private EditText eLetra;
    private TextView tvCantoNombre, tvCantoAutor, tvCantoTono, tvCantoMomentos, tvCantoTiempos;
    private String letra;
    private final int tamLetra = 16;
    private int ValorTexto = 0;
    private float currentTextSize = 0f;
    private ScaleGestureDetector scaleGestureDetector;
    private final float MIN_TEXT_SIZE = 10f;
    private final float MAX_TEXT_SIZE = 72f;
    private boolean isScaling = false;
    private CantosViewModel cantosViewModel;
    private Canto canto;
    private Esquema esquema;
    private boolean tonosQuitados = false;
    private FloatingActionButton bAtras, bSiguiente;
    private boolean bandera;
    private AtomicInteger indice;

    Map<String, String> tonosArriba;
    Map<String, String> tonosAbajo;

    private final String[] extras = {"","m","7","m7"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Uncomment when androidx.edge:edge is available
        setContentView(R.layout.activity_canto);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                regresar();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        eLetra = findViewById(R.id.eLetra);
        tvCantoNombre = findViewById(R.id.tvCantoNombre);
        tvCantoAutor = findViewById(R.id.tvCantoAutor);
        tvCantoTono = findViewById(R.id.tvCantoTono);
        tvCantoMomentos = findViewById(R.id.tvCantoMomentos);
        tvCantoTiempos = findViewById(R.id.tvCantoTiempos);

        // Initialize pinch-to-zoom detector and touch handling for the lyric EditText
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        eLetra.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);

            if (event.getPointerCount() > 1 || isScaling) {
                if (v.getParent() != null) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                    v.performClick();
                }
                return true;
            }
            return false;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            canto = this.getIntent().getSerializableExtra("canto", Canto.class);
        } else {
            canto = (Canto) this.getIntent().getSerializableExtra("canto");
        }
        cantosViewModel = new ViewModelProvider(this).get(CantosViewModel.class);
        cantosViewModel.getDeleteSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(CantoActivity.this, getString(R.string.msg_canto_eliminado), Toast.LENGTH_SHORT).show();
                regresar();
            }
        });
        
        cantosViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        bandera = this.getIntent().getBooleanExtra("bandera", false);

        tonosArriba = new HashMap<>();
        tonosAbajo = new HashMap<>();

        for (String extra: extras) {

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

            tonosAbajo.put("DO" + extra, "SI" + extra);
            tonosAbajo.put("DO#" + extra, "DO" + extra);
            tonosAbajo.put("REb" + extra, "DO" + extra);
            tonosAbajo.put("RE" + extra, "DO#" + extra);
            tonosAbajo.put("RE#" + extra, "RE" + extra);
            tonosAbajo.put("MIb" + extra, "RE" + extra);
            tonosAbajo.put("MI" + extra, "MIb" + extra);
            tonosAbajo.put("FA" + extra, "MI" + extra);
            tonosAbajo.put("FA#" + extra, "FA" + extra);
            tonosAbajo.put("SOLb" + extra, "FA" + extra);
            tonosAbajo.put("SOL" + extra, "FA#" + extra);
            tonosAbajo.put("SOL#" + extra, "SOL" + extra);
            tonosAbajo.put("LAb" + extra, "SOL" + extra);
            tonosAbajo.put("LA" + extra, "SOL#" + extra);
            tonosAbajo.put("LA#" + extra, "LA" + extra);
            tonosAbajo.put("SIb" + extra, "LA" + extra);
            tonosAbajo.put("SI" + extra, "SIb" + extra);
        }

        if(bandera) {
            bAtras = findViewById(R.id.fabAntes);
            bSiguiente = findViewById(R.id.fabSiguiente);
            bAtras.setVisibility(View.VISIBLE);
            bSiguiente.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                esquema = this.getIntent().getSerializableExtra("esquema", Esquema.class);
            } else {
                esquema = (Esquema) this.getIntent().getSerializableExtra("esquema");
            }

            indice = new AtomicInteger();

            indice.set(this.getIntent().getIntExtra("indice", 0));

            verificar(indice.get());

            bAtras.setOnClickListener(view -> {
                indice.getAndDecrement();

                verificar(indice.get());
            });

            bSiguiente.setOnClickListener(view -> {
                indice.getAndIncrement();

                verificar(indice.get());
            });
        } else {
            inicializarFirebase();
        }
    }

    private void verificar(int indice) {
        if(indice == 0) {
            bAtras.setVisibility(View.INVISIBLE);
        } else {
            bAtras.setVisibility(View.VISIBLE);
        }

        if(indice == esquema.getCantos().size() - 1) {
            bSiguiente.setVisibility(View.INVISIBLE);
        } else {
            bSiguiente.setVisibility(View.VISIBLE);
        }

        canto = esquema.getCantos().get(indice);

        barra();
        letra();
    }

    public void regresar() {
        finish();
        Transiciones.right(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        regresar();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu){
        getMenuInflater().inflate(R.menu.menu_canto,menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.pdf) {
            crearPDF();
        } else if (itemId == R.id.modificar) {
            if(bandera) {
                Intent intent = new Intent(this, ModCantoEsquemaActivity.class);
                intent.putExtra("canto", esquema.getCantos().get(indice.get()));
                intent.putExtra("esquema", esquema);
                intent.putExtra("indice", indice.get());

                this.startActivity(intent);
                Transiciones.left(this);
            } else {
                modificarCanto();
            }
        } else if (itemId == R.id.eliminar) {
            if(bandera) {
                Toast.makeText(this, getString(R.string.msg_no_eliminar), Toast.LENGTH_SHORT).show();
            } else {
                eliminarCanto();
            }
        } else if (itemId == R.id.quitarTonos) {
            quitarTonos(item);
        } else if (itemId == R.id.transportarTono) {
            mostrarDialogoTransportar();
        } else if (itemId == R.id.aumentarLetra) {
            aumentarLetra();
        } else if (itemId == R.id.disminuirLetra) {
            disminuirLetra();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                crearPDF();
            }else{
                Toast.makeText(this, getString(R.string.msg_permiso_denegado), Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void inicializarFirebase() {
        if (canto != null) {
            barra();
            letra();
            if (tonosQuitados) {
                quitarTonos();
            }
        }
    }

    private void barra() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && canto != null) {
            actionBar.setTitle(canto.getNombre());
        }
    }

    private void letra() {
        if (canto == null) return;
        this.letra = canto.getLetra();

        eLetra.setFocusable(false);
        eLetra.setCursorVisible(false);

        if (tvCantoNombre != null && canto.getNombre() != null) {
            tvCantoNombre.setText(canto.getNombre());
        }

        if (tvCantoAutor != null) {
            if (canto.getAutor() != null && !canto.getAutor().trim().isEmpty()) {
                tvCantoAutor.setVisibility(View.VISIBLE);
                tvCantoAutor.setText("Autor: " + canto.getAutor().trim());
            } else {
                tvCantoAutor.setVisibility(View.GONE);
            }
        }

        if (tvCantoTono != null) {
            if (canto.getTono() != null && !canto.getTono().trim().isEmpty()) {
                tvCantoTono.setVisibility(View.VISIBLE);
                tvCantoTono.setText("Tono: " + canto.getTono().trim());
            } else {
                tvCantoTono.setVisibility(View.GONE);
            }
        }

        if (tvCantoMomentos != null) {
            if (canto.getMomentos() != null && !canto.getMomentos().isEmpty()) {
                tvCantoMomentos.setVisibility(View.VISIBLE);
                tvCantoMomentos.setText(String.join(", ", canto.getMomentos()));
            } else {
                tvCantoMomentos.setVisibility(View.GONE);
            }
        }

        if (tvCantoTiempos != null) {
            if (canto.getTiempos() != null && !canto.getTiempos().isEmpty()) {
                tvCantoTiempos.setVisibility(View.VISIBLE);
                tvCantoTiempos.setText(String.join(", ", canto.getTiempos()));
            } else {
                tvCantoTiempos.setVisibility(View.GONE);
            }
        }

        mostrarLetraFormateada();
    }

    private void mostrarLetraFormateada() {
        if (letra == null) {
            eLetra.setText("");
            return;
        }

        if (tonosQuitados) {
            eLetra.setText(letra);
            return;
        }

        Pattern pattern = Pattern.compile("'([^']+)'");
        String letraSinComillas = letra.replace("'", "");
        SpannableString spannable = new SpannableString(letraSinComillas);

        int chordColor = ContextCompat.getColor(this, R.color.chord_highlight_color);
        Matcher m = pattern.matcher(letra);
        int offset = 0;

        while (m.find()) {
            int originalStart = m.start();
            int originalEnd = m.end();

            int cleanStart = originalStart - offset;
            int cleanEnd = originalEnd - offset - 2;
            offset += 2;

            if (cleanStart >= 0 && cleanEnd <= spannable.length() && cleanStart < cleanEnd) {
                spannable.setSpan(new ForegroundColorSpan(chordColor), cleanStart, cleanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), cleanStart, cleanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        // Resaltar señas y anotaciones musicales sólo si están en MAYÚSCULAS (INTRO, CORO, VIOLIN, GUITARRA, MANDOLINA, TONO, 2 VECES, etc.)
        int directiveColor = ContextCompat.getColor(this, R.color.directive_highlight_color);
        Pattern patternDirective = Pattern.compile("\\b(INTRO|CORO|ESTROFA|PUENTE|OUTRO|FINAL|SOLO|INTERLUDIO|VIOLIN|VIOLÍN|GUITARRA|MANDOLINA|FLAUTA|PIANO|TROMPETA|TONO|TONOS|BIS|\\d+\\s*VECES|\\d+X)\\b|[\\[\\(][^\\]\\)]+[\\]\\)]");
        Matcher mDirective = patternDirective.matcher(letraSinComillas);

        while (mDirective.find()) {
            int start = mDirective.start();
            int end = mDirective.end();
            if (start >= 0 && end <= spannable.length()) {
                spannable.setSpan(new ForegroundColorSpan(directiveColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        eLetra.setText(spannable);

        if (ValorTexto == 0){
            ValorTexto = tamLetra;
            eLetra.setTextSize(ValorTexto);
            currentTextSize = ValorTexto;
        }
        if (currentTextSize == 0f) {
            currentTextSize = (ValorTexto == 0) ? tamLetra : ValorTexto;
        }
    }

    private ClickableSpan spanTono() {
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Toast.makeText(CantoActivity.this, getString(R.string.msg_click), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void crearPDF() {
        try {
            TemplatePDF templatePDF = new TemplatePDF(this);
            templatePDF.openDocument(canto.getNombre());
            templatePDF.addMetaData(canto.getNombre(), "Canto", "Coro");
            templatePDF.addTitles(canto.getNombre(), canto.getAutor() != null ? canto.getAutor() : "", "");
            templatePDF.addParagraph(eLetra.getText().toString());
            templatePDF.closeDocument();
            templatePDF.viewPDF();
            Toast.makeText(CantoActivity.this, getString(R.string.msg_pdf_creado), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(CantoActivity.this, getString(R.string.msg_error_crear_pdf, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    // ...existing onRequestPermissionsResult defined earlier handles permission results
    private void modificarCanto() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Toast.makeText(CantoActivity.this, getString(R.string.msg_no_usuario),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!canto.getGrupo_id().equals(Constantes.GRUPO_SELECCIONADO)) {
            Toast.makeText(CantoActivity.this, getString(R.string.msg_no_modificar_canto),
                    Toast.LENGTH_SHORT).show();
            return;
        }

//        if (usuario.getUid().equals(canto.getCreado_por()) || usuario.getUid().equals(canto.getModificado_por()) || usuario.getUid().equals("mYW9YLYZPmZdhaSwSS0ONF0EUe53")) {
            canto.setLetra(letra);
            Intent i = new Intent(CantoActivity.this, NuevoCantoActivity.class);
            i.putExtra("tipo", 1);
            i.putExtra("canto", canto);
            startActivity(i);
            Transiciones.left(this);
            finish();
//        } else {
//            Toast.makeText(CantoActivity.this, "Solo el creador o modificador puede editar este canto",
//                    Toast.LENGTH_SHORT).show();
//        }
    }

    private void eliminarCanto() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Toast.makeText(CantoActivity.this, getString(R.string.msg_no_usuario), Toast.LENGTH_SHORT).show();
            return;
        }

        String grupoId = canto != null && canto.getGrupo_id() != null ? canto.getGrupo_id() : Constantes.GRUPO_SELECCIONADO;
        if (grupoId == null || grupoId.isEmpty()) {
            Toast.makeText(this, "Error al identificar el grupo del canto", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("grupos")
                .child(grupoId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String adminId = snapshot.child("admin_id").getValue(String.class);
                        boolean esCoAdmin = snapshot.child("admins").hasChild(usuario.getUid());

                        if (usuario.getUid().equals(adminId) || esCoAdmin || adminId == null) {
                            mostrarConfirmacionEliminarCanto();
                        } else {
                            Toast.makeText(CantoActivity.this, getString(R.string.msg_no_permisos), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CantoActivity.this, getString(R.string.msg_no_permisos), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(CantoActivity.this, "Error de red al verificar permisos", Toast.LENGTH_SHORT).show());
    }

    private void mostrarConfirmacionEliminarCanto() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomMaterialDialog);
        builder.setTitle(getString(R.string.titulo_confirmar));
        builder.setMessage(getString(R.string.msg_eliminar_canto));
        builder.setPositiveButton(getString(R.string.btn_si), (dialog, which) -> {
            cantosViewModel.deleteCanto(canto.getId());
        });
        builder.setNegativeButton(getString(R.string.btn_no), (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void aumentarLetra() {
        if (ValorTexto == 0){
            ValorTexto = tamLetra;
        }
        if (ValorTexto < 30){
            ValorTexto += 1;
            currentTextSize = ValorTexto;
            eLetra.setTextSize(ValorTexto);
        }
    }

    private void disminuirLetra() {
        if (ValorTexto == 0){
            ValorTexto = tamLetra;
        }
        if (ValorTexto > 10){
            ValorTexto -= 1;
            currentTextSize = ValorTexto;
            eLetra.setTextSize(ValorTexto);
        }
    }

    private void quitarTonos(MenuItem item) {
        tonosQuitados = !tonosQuitados;

        if(tonosQuitados) {
            item.setTitle(R.string.ver_tonos);
            quitarTonos();
        } else {
            item.setTitle(R.string.quitar_tonos);
            letra();
        }
    }

    private void quitarTonos() {
        String[] palabras = letra.split("'");
        StringBuilder nuevaLetra = new StringBuilder();

        for (String palabra : palabras) {
            if(tonosArriba.get(palabra) == null) {
                nuevaLetra.append(palabra);
            }
        }

        letra = nuevaLetra.toString();
        letra = letra.replace("○", " ");
        letra = letra.replace("|", " ");
        letra = letra.replace("---", "   ");
        letra = letra.replaceAll("(?m)^[ \t]*\r?\n", "");
        eLetra.setText(letra);
    }



    private void mostrarDialogoTransportar() {
        if (canto == null || canto.getTono() == null || canto.getTono().trim().isEmpty()) {
            new MaterialAlertDialogBuilder(this, R.style.CustomMaterialDialog)
                    .setTitle("Tono Requerido")
                    .setMessage("Para poder transportar este canto a otro tono, primero debes registrar su tono original (puedes asignarlo al editar el canto).")
                    .setPositiveButton("Aceptar", null)
                    .show();
            return;
        }

        String tonoOriginalBruto = canto.getTono().trim();
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
            
            if (tvCantoTono != null) {
                tvCantoTono.setVisibility(View.VISIBLE);
                tvCantoTono.setText("Tono transpuesto: " + tonoDestino + sufijo);
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
        
        if (idxOriginal == -1 || idxDestino == -1) {
            letra = canto.getLetra();
            mostrarLetraFormateada();
            return;
        }
        
        // Empezamos con la letra original y la vamos subiendo
        letra = canto.getLetra();
        
        int diferencia = idxDestino - idxOriginal;
        if (diferencia < 0) diferencia += 12;

        for (int i = 0; i < diferencia; i++) {
            subirTonoLogico();
        }
        mostrarLetraFormateada();
        
        // Actualizamos el objeto Canto para que si presiona "Modificar" guarde el nuevo tono en Firebase
        canto.setTono(destino + (canto.getTono().trim().endsWith("m") || canto.getTono().trim().endsWith("M") ? canto.getTono().trim().substring(canto.getTono().trim().length() - 1) : ""));
    }

    private void subirTonoLogico() {
        String[] palabras = letra.split("'");
        StringBuilder nuevaLetra = new StringBuilder();

        for (String palabra : palabras) {
            if(tonosArriba.get(palabra) != null) {
                nuevaLetra.append("'").append(tonosArriba.get(palabra)).append("'");
            } else {
                nuevaLetra.append(palabra);
            }
        }
        letra = nuevaLetra.toString();
    }

    /**
     * Scale gesture listener that updates the EditText text size when the user pinches.
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isScaling = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScaling = false;
            super.onScaleEnd(detector);
        }
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();

            if (currentTextSize == 0f) {
                currentTextSize = (ValorTexto == 0) ? tamLetra : ValorTexto;
            }

            // Apply the relative scale factor to the current text size
            currentTextSize = Math.max(MIN_TEXT_SIZE, Math.min(MAX_TEXT_SIZE, currentTextSize * scaleFactor));

            // Update integer tracking value and the EditText size (in sp)
            ValorTexto = Math.round(currentTextSize);
            eLetra.setTextSize(ValorTexto);

            return true;
        }
    }
}