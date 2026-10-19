package com.pacosotelo.coro.ui;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.tools.Transiciones;

import java.io.File;

public class ViewPDFActivity extends AppCompatActivity {

    private PDFView pdfView;
    private File file;
    private ProgressBar pbPdfCargando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpdf);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                regresar();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Visor PDF");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        pdfView = findViewById(R.id.pdfView);
        pbPdfCargando = findViewById(R.id.pbPdfCargando);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            String path = bundle.getString("path","");
            file = new File(path);

            if (file.exists()) {
                if (actionBar != null) {
                    actionBar.setTitle(file.getName().replace(".pdf", ""));
                }
                
                pbPdfCargando.setVisibility(View.VISIBLE);
                pdfView.fromFile(file)
                        .enableSwipe(true)
                        .enableDoubletap(true)
                        .onLoad(nbPages -> pbPdfCargando.setVisibility(View.GONE))
                        .onError(t -> {
                            pbPdfCargando.setVisibility(View.GONE);
                            Toast.makeText(this, "Error al cargar el PDF", Toast.LENGTH_SHORT).show();
                        })
                        .load();
            } else {
                Toast.makeText(this, "No se encontró el archivo", Toast.LENGTH_SHORT).show();
                regresar();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pdf, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share_pdf) {
            compartirPdf();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            regresar();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void compartirPdf() {
        if (file == null || !file.exists()) {
            Toast.makeText(this, "Archivo no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri pdfUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Compartir Canto: " + file.getName().replace(".pdf", ""));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Compartir PDF vía..."));
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo compartir el archivo.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void regresar() {
        finish();
        Transiciones.right(this);
    }
}
