package com.pacosotelo.coro.ui;

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
import android.os.Bundle;
import android.text.style.ClickableSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.tools.Constantes;
import com.pacosotelo.coro.tools.TemplatePDF;
import com.pacosotelo.coro.modelos.Canto;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class CantoActivity extends AppCompatActivity {
    private EditText eLetra;
    private String letra;
    private final int tamLetra = 16;
    private int ValorTexto = 0;
    private DatabaseReference dr;
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
        setContentView(R.layout.activity_canto);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        eLetra = findViewById(R.id.eLetra);

        canto = (Canto) this.getIntent().getSerializableExtra("canto");
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

            esquema = (Esquema) this.getIntent().getSerializableExtra("esquema");

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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.right_in,R.anim.right_out);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
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
        switch (item.getItemId()) {
            case R.id.pdf:
                crearPDF();
                break;
            case R.id.modificar:
                if(bandera) {
                    Intent intent = new Intent(this, ModCantoEsquemaActivity.class);
                    intent.putExtra("canto", esquema.getCantos().get(indice.get()));
                    intent.putExtra("esquema", esquema);
                    intent.putExtra("indice",indice.get());

                    this.startActivity(intent);
                    this.overridePendingTransition(R.anim.left_in,R.anim.left_out);
                } else {
                    modificarCanto();
                }
                break;
            case R.id.eliminar:
                if(bandera) {
                    Toast.makeText(this, "No se puede eliminar", Toast.LENGTH_SHORT).show();
                } else {
                    eliminarCanto();
                }
                break;
            case R.id.quitarTonos:
                quitarTonos(item);
                break;
            case R.id.subirTono:
                subirTono();
                break;
            case R.id.bajarTono:
                bajarTono();
                break;
            case R.id.aumentarLetra:
                aumentarLetra();
                break;
            case R.id.disminuirLetra:
                disminuirLetra();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1){
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                crearPDF();
            }else{
                Toast.makeText(this, "No se pudo acceder al almacenamiento: permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void inicializarFirebase() {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        dr = fd.getReference("cantos");

        dr.child(canto.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    canto.setNombre(Objects.requireNonNull(snapshot.child("nombre").getValue()).toString());
                    canto.setLetra(Objects.requireNonNull(snapshot.child("letra").getValue()).toString());
                    barra();

                    letra();

                    if(tonosQuitados) quitarTonos();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void barra() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(canto.getNombre());
    }

    private void letra() {
        this.letra = canto.getLetra();

        eLetra.setFocusable(false);
        eLetra.setCursorVisible(false);

        String letra2 = letra.replaceAll("'", "");
        /*SpannableString ss = new SpannableString(letra2);

        ss.setSpan(spanTono(),0,4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(Color.YELLOW),
                0, 4,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/

        eLetra.setText(letra2);

        if (ValorTexto == 0){
            ValorTexto = tamLetra;
            eLetra.setTextSize(ValorTexto);
        }
    }

    private ClickableSpan spanTono() {
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Toast.makeText(CantoActivity.this, "click", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void crearPDF() {
        try {
            TemplatePDF templatePDF = new TemplatePDF(getApplicationContext());
            templatePDF.openDocument(canto.getNombre());
            templatePDF.addMetaData(canto.getNombre(), "Canto", "Paco Sotelo");
            templatePDF.addTitle(canto.getNombre());
            templatePDF.addParagraph(eLetra.getText().toString());
            templatePDF.closeDocument();
            templatePDF.viewPDF();
            Toast.makeText(CantoActivity.this, "PDF creado correctamente", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(CantoActivity.this, "Error al crear PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ...existing onRequestPermissionsResult defined earlier handles permission results
    private void modificarCanto() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Toast.makeText(CantoActivity.this, "Usuario no autenticado",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!canto.getGrupo_id().equals(Constantes.GRUPO_SELECCIONADO)) {
            Toast.makeText(CantoActivity.this, "No puedes modificar este canto",
                    Toast.LENGTH_SHORT).show();
            return;
        }

//        if (usuario.getUid().equals(canto.getCreado_por()) || usuario.getUid().equals(canto.getModificado_por()) || usuario.getUid().equals("mYW9YLYZPmZdhaSwSS0ONF0EUe53")) {
            canto.setLetra(letra);
            Intent i = new Intent(CantoActivity.this, NuevoCantoActivity.class);
            i.putExtra("tipo", 1);
            i.putExtra("canto", canto);
            startActivity(i);
            overridePendingTransition(R.anim.left_in,R.anim.left_out);
            finish();
//        } else {
//            Toast.makeText(CantoActivity.this, "Solo el creador o modificador puede editar este canto",
//                    Toast.LENGTH_SHORT).show();
//        }
    }

    private void eliminarCanto() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Toast.makeText(CantoActivity.this, "Usuario no autenticado",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(usuario.getUid().equals("mYW9YLYZPmZdhaSwSS0ONF0EUe53")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Confirmar");
            builder.setMessage("¿Desea eliminar el canto?");

            builder.setPositiveButton("SI", (dialog, which) -> {
                dr.child(canto.getId()).removeValue();

                Toast.makeText(CantoActivity.this, "Canto eliminado",
                        Toast.LENGTH_SHORT).show();

                onBackPressed();
            });

            builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());

            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Toast.makeText(CantoActivity.this, "No tienes permiso para esta operación",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void aumentarLetra() {
        if (ValorTexto == 0){
            ValorTexto = tamLetra;
        }
        if (ValorTexto < 30){
            ValorTexto += 1;
            eLetra.setTextSize(ValorTexto);
        }
    }

    private void disminuirLetra() {
        if (ValorTexto == 0){
            ValorTexto = tamLetra;
        }
        if (ValorTexto > 10){
            ValorTexto -= 1;
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
        letra = letra.replaceAll("○", " ");
        letra = letra.replaceAll("\\|", " ");
        letra = letra.replaceAll("---", "   ");
        letra = letra.replaceAll("(?m)^[ \t]*\r?\n", "");
        eLetra.setText(letra);
    }

    private void subirTono() {
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
        eLetra.setText(letra.replaceAll("'", ""));
    }

    private void bajarTono() {
        String[] palabras = letra.split("'");
        StringBuilder nuevaLetra = new StringBuilder();

        for (String palabra : palabras) {
            if(tonosAbajo.get(palabra) != null) {
                nuevaLetra.append("'").append(tonosAbajo.get(palabra)).append("'");
            } else {
                nuevaLetra.append(palabra);
            }
        }

        letra = nuevaLetra.toString();
        eLetra.setText(letra.replaceAll("'", ""));
    }
}