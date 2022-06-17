package com.pacosotelo.coro.vista;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.TemplatePDF;
import com.pacosotelo.coro.modelo.Canto;

import java.util.Objects;

public class VerCanto extends AppCompatActivity {
    private EditText eLetra;
    private String letra;
    private final int tamLetra = 16;
    private int ValorTexto = 0;
    private DatabaseReference dr;
    private Canto canto;
    private boolean tonosQuitados = false;

    private final String[] tonosMayores = {
            "DO","DO#","RE","RE#","MI","FA","FA#","SOL","SOL#","LA","SIB","SI"
    };

    private final String[] tonosMenores = {
            "DOm","DO#m","REm","RE#m","MIm","FAm","FA#m","SOLm","SOL#m","LAm","SIBm","SIm"
    };

    private final String[] tonosMayores7 = {
            "DO7","DO#7","RE7","RE#7","MI7","FA7","FA#7","SOL7","SOL#7","LA7","SIB7","SI7"
    };

    private final String[] tonosMenores7 = {
            "DOm7","DO#m7","REm7","RE#m7","MIm7","FAm7","FA#m7","SOLm7","SOL#m7","LAm7","SIBm7","SIm7"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canto);

        eLetra = findViewById(R.id.eLetra);

        Bundle datos = this.getIntent().getExtras();

        canto = new Canto();

        if (datos != null) {
            canto.setId(datos.getString("getID"));
            canto.setNombre(datos.getString("getNombre"));
            canto.setLetra(datos.getString("getLetra"));
            canto.setMomentos(datos.getStringArrayList("getMomentos"));
            canto.setTiempos(datos.getStringArrayList("getTiempos"));
        }

        barra();

        inicializarFirebase();

        letra();
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    public void onBackPressed() {
        Intent i = new Intent(VerCanto.this, Lista.class);
        startActivity(i);
        overridePendingTransition(R.anim.right_in,R.anim.right_out);
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_canto,menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pdf:
                TemplatePDF templatePDF = new TemplatePDF(getApplicationContext());
                templatePDF.openDocument(canto.getNombre());
                templatePDF.addMetaData(canto.getNombre(), "Canto", "Paco Sotelo");
                templatePDF.addTitle(canto.getNombre());
                templatePDF.addParagraph(eLetra.getText().toString());
                templatePDF.closeDocument();
                templatePDF.viewPDF();
                //templatePDF.appViewPDF(this);
                break;
            case R.id.modificar:
                modificarCanto();
                break;
            case R.id.eliminar:
                eliminarCanto();
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
        return true;
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
        eLetra.setText(letra.replaceAll("'", ""));

        if (ValorTexto == 0){
            ValorTexto = tamLetra;
            eLetra.setTextSize(ValorTexto);
        }
    }

    private void inicializarFirebase() {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        dr = fd.getReference("Canto");

        dr.child(canto.getId()).addValueEventListener(new ValueEventListener() {
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

    private void modificarCanto() {
        Intent i = new Intent(VerCanto.this, Nuevo.class);
        i.putExtra("getTipo", 1);
        i.putExtra("getID", canto.getId());
        i.putExtra("getNombre",canto.getNombre());
        i.putExtra("getLetra", canto.getLetra());
        i.putExtra("getMomentos",canto.getMomentos());
        i.putExtra("getTiempos", canto.getTiempos());
        startActivity(i);
        overridePendingTransition(R.anim.left_in,R.anim.left_out);
        finish();
    }

    private void eliminarCanto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirmar");
        builder.setMessage("¿Desea eliminar el canto?");

        builder.setPositiveButton("SI", (dialog, which) -> {
            dr.child(canto.getId()).removeValue();

            Toast.makeText(VerCanto.this, "Canto eliminado", Toast.LENGTH_SHORT).show();
            onBackPressed();
        });

        builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
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

    private boolean esTono(String palabra, String[] tonos) {
        boolean esTono = false;

        for (String tono : tonos) {
            if (palabra.equals(tono)) {
                esTono = true;
                break;
            }
        }

        return esTono;
    }
    private boolean esTono(String palabra) {
        return esTono(palabra, tonosMayores)
                || esTono(palabra, tonosMenores)
                || esTono(palabra, tonosMayores7)
                || esTono(palabra, tonosMenores7);
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

            if (!esTono(palabra)) {
                nuevaLetra.append(palabra);
            }
        }

        letra = nuevaLetra.toString();
        letra = letra.replaceAll("○", "");
        letra = letra.replaceAll("\\|", "");
        letra = letra.replaceAll("---", "");
        letra = letra.trim();
        eLetra.setText(letra);
    }

    private void subirTono() {
        String[] palabras = letra.split("'");
        StringBuilder nuevaLetra = new StringBuilder();

        /*for (int i = 0; i < palabras.length; i++) {
            for (int j = 0; j < tonosMayores.length; j++) {
                if(palabras[i].equals(tonosMayores[j])) {
                    if(j == (tonosMayores.length-1)) {
                        nuevaLetra.append("'").append(tonosMayores[j+1]).append("'");
                    } else {
                        nuevaLetra.append("'").append(tonosMayores[0]).append("'");
                    }
                } else {
                    nuevaLetra.append(palabras[i]);
                    i++;
                }
            }

        }*/

        for (String palabra : palabras) {
            switch (palabra) {
                case "DO":
                    nuevaLetra.append("'DO#'");
                    break;
                case "DO#":
                case "REb":
                    nuevaLetra.append("'RE'");
                    break;
                case "RE":
                    nuevaLetra.append("'RE#'");
                    break;
                case "RE#":
                case "MIb":
                    nuevaLetra.append("'MI'");
                    break;
                case "MI":
                    nuevaLetra.append("'FA'");
                    break;
                case "FA":
                    nuevaLetra.append("'FA#'");
                    break;
                case "FA#":
                case "SOLb":
                    nuevaLetra.append("'SOL'");
                    break;
                case "SOL":
                    nuevaLetra.append("'SOL#'");
                    break;
                case "SOL#":
                case "LAb":
                    nuevaLetra.append("'LA'");
                    break;
                case "LA":
                    nuevaLetra.append("'SIb'");
                    break;
                case "LA#":
                case "SIb":
                    nuevaLetra.append("'SI'");
                    break;
                case "SI":
                    nuevaLetra.append("'DO'");
                    break;
                case "DOm":
                    nuevaLetra.append("'DO#m'");
                    break;
                case "DO#m":
                case "REbm":
                    nuevaLetra.append("'REm'");
                    break;
                case "REm":
                    nuevaLetra.append("'RE#m'");
                    break;
                case "RE#m":
                case "MIbm":
                    nuevaLetra.append("'MIm'");
                    break;
                case "MIm":
                    nuevaLetra.append("'FAm'");
                    break;
                case "FAm":
                    nuevaLetra.append("'FA#m'");
                    break;
                case "FA#m":
                case "SOLbm":
                    nuevaLetra.append("'SOLm'");
                    break;
                case "SOLm":
                    nuevaLetra.append("'SOL#m'");
                    break;
                case "SOL#m":
                case "LAbm":
                    nuevaLetra.append("'LAm'");
                    break;
                case "LAm":
                    nuevaLetra.append("'SIbm'");
                    break;
                case "LA#m":
                case "SIbm":
                    nuevaLetra.append("'SIm'");
                    break;
                case "SIm":
                    nuevaLetra.append("'DOm'");
                    break;
                case "DO7":
                    nuevaLetra.append("'DO#7'");
                    break;
                case "DO#7":
                case "REb7":
                    nuevaLetra.append("'RE7'");
                    break;
                case "RE7":
                    nuevaLetra.append("'RE#7'");
                    break;
                case "RE#7":
                case "MIb7":
                    nuevaLetra.append("'MI7'");
                    break;
                case "MI7":
                    nuevaLetra.append("'FA7'");
                    break;
                case "FA7":
                    nuevaLetra.append("'FA#7'");
                    break;
                case "FA#7":
                case "SOLb7":
                    nuevaLetra.append("'SOL7'");
                    break;
                case "SOL7":
                    nuevaLetra.append("'SOL#7'");
                    break;
                case "SOL#7":
                case "LAb7":
                    nuevaLetra.append("'LA7'");
                    break;
                case "LA7":
                    nuevaLetra.append("'SIb7'");
                    break;
                case "LA#7":
                case "SIb7":
                    nuevaLetra.append("'SI7'");
                    break;
                case "SI7":
                    nuevaLetra.append("'DO7' ");
                    break;
                case "DOm7":
                    nuevaLetra.append("'DO#m7'");
                    break;
                case "DO#m7":
                case "REbm7":
                    nuevaLetra.append("'REm7'");
                    break;
                case "REm7":
                    nuevaLetra.append("'RE#m7'");
                    break;
                case "RE#m7":
                case "MIbm7":
                    nuevaLetra.append("'MIm7'");
                    break;
                case "MIm7":
                    nuevaLetra.append("'FAm7'");
                    break;
                case "FAm7":
                    nuevaLetra.append("'FA#m7'");
                    break;
                case "FA#m7":
                case "SOLbm7":
                    nuevaLetra.append("'SOLm7'");
                    break;
                case "SOLm7":
                    nuevaLetra.append("'SOL#m7'");
                    break;
                case "SOL#m7":
                case "LAbm7":
                    nuevaLetra.append("'LAm7'");
                    break;
                case "LAm7":
                    nuevaLetra.append("'SIbm7'");
                    break;
                case "LA#m7":
                case "SIbm7":
                    nuevaLetra.append("'SIm7'");
                    break;
                case "SIm7":
                    nuevaLetra.append("'DOm7'");
                    break;
                default:
                    nuevaLetra.append(palabra);
                    break;
            }
        }

        letra = nuevaLetra.toString();
        eLetra.setText(letra.replaceAll("'", ""));
    }

    private void bajarTono() {
        String[] partesLetra = letra.split("'");
        StringBuilder nuevaLetra = new StringBuilder();

        for (String palabra : partesLetra) {
            switch (palabra) {
                case "DO":
                    nuevaLetra.append("'SI'");
                    break;
                case "DO#":
                case "REb":
                    nuevaLetra.append("'DO'");
                    break;
                case "RE":
                    nuevaLetra.append("'DO#'");
                    break;
                case "RE#":
                case "MIb":
                    nuevaLetra.append("'RE'");
                    break;
                case "MI":
                    nuevaLetra.append("'RE#'");
                    break;
                case "FA":
                    nuevaLetra.append("'MI'");
                    break;
                case "FA#":
                case "SOLb":
                    nuevaLetra.append("'FA'");
                    break;
                case "SOL":
                    nuevaLetra.append("'FA#'");
                    break;
                case "SOL#":
                case "LAb":
                    nuevaLetra.append("'SOL'");
                    break;
                case "LA":
                    nuevaLetra.append("'SOL#'");
                    break;
                case "LA#":
                case "SIb":
                    nuevaLetra.append("'LA'");
                    break;
                case "SI":
                    nuevaLetra.append("'SIb'");
                    break;
                case "DOm":
                    nuevaLetra.append("'SIm'");
                    break;
                case "DO#m":
                case "REbm":
                    nuevaLetra.append("'DOm'");
                    break;
                case "REm":
                    nuevaLetra.append("'DO#m'");
                    break;
                case "RE#m":
                case "MIbm":
                    nuevaLetra.append("'REm'");
                    break;
                case "MIm":
                    nuevaLetra.append("'RE#m'");
                    break;
                case "FAm":
                    nuevaLetra.append("'MIm'");
                    break;
                case "FA#m":
                case "SOLbm":
                    nuevaLetra.append("'FAm'");
                    break;
                case "SOLm":
                    nuevaLetra.append("'FA#m'");
                    break;
                case "SOL#m":
                case "LAbm":
                    nuevaLetra.append("'SOLm'");
                    break;
                case "LAm":
                    nuevaLetra.append("'SOL#m'");
                    break;
                case "LA#m":
                case "SIbm":
                    nuevaLetra.append("'LAm'");
                    break;
                case "SIm":
                    nuevaLetra.append("'SIbm'");
                    break;
                case "DO7":
                    nuevaLetra.append("'SI7'");
                    break;
                case "DO#7":
                case "REb7":
                    nuevaLetra.append("'DO7'");
                    break;
                case "RE7":
                    nuevaLetra.append("'DO#7'");
                    break;
                case "RE#7":
                case "MIb7":
                    nuevaLetra.append("'RE7'");
                    break;
                case "MI7":
                    nuevaLetra.append("'RE#7'");
                    break;
                case "FA7":
                    nuevaLetra.append("'MI7'");
                    break;
                case "FA#7":
                case "SOLb7":
                    nuevaLetra.append("'FA7'");
                    break;
                case "SOL7":
                    nuevaLetra.append("'FA#7'");
                    break;
                case "SOL#7":
                case "LAb7":
                    nuevaLetra.append("'SOL7'");
                    break;
                case "LA7":
                    nuevaLetra.append("'SOL#7'");
                    break;
                case "LA#7":
                case "SIb7":
                    nuevaLetra.append("'LA7'");
                    break;
                case "SI7":
                    nuevaLetra.append("'SIb7'");
                    break;
                case "DOm7":
                    nuevaLetra.append("'SIm7'");
                    break;
                case "DO#m7":
                case "REbm7":
                    nuevaLetra.append("'DOm7'");
                    break;
                case "REm7":
                    nuevaLetra.append("'DO#m7'");
                    break;
                case "RE#m7":
                case "MIbm7":
                    nuevaLetra.append("'REm7'");
                    break;
                case "MIm7":
                    nuevaLetra.append("'RE#m7'");
                    break;
                case "FAm7":
                    nuevaLetra.append("'MIm7'");
                    break;
                case "FA#m7":
                case "SOLbm7":
                    nuevaLetra.append("'FAm7'");
                    break;
                case "SOLm7":
                    nuevaLetra.append("'FA#m7'");
                    break;
                case "SOL#m7":
                case "LAbm7":
                    nuevaLetra.append("'SOLm7'");
                    break;
                case "LAm7":
                    nuevaLetra.append("'SOL#m7'");
                    break;
                case "LA#m7":
                case "SIbm7":
                    nuevaLetra.append("'LAm7'");
                    break;
                case "SIm7":
                    nuevaLetra.append("'SIbm7'");
                    break;
                default:
                    nuevaLetra.append(palabra);
                    break;
            }
        }

        letra = nuevaLetra.toString();
        eLetra.setText(letra.replaceAll("'", ""));
    }
}