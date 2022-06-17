package com.pacosotelo.coro;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class ViewPDFActivity extends AppCompatActivity {

    private PDFView pdfView;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pdfactivity);

        pdfView = findViewById(R.id.pdfView);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            file = new File(bundle.getString("path",""));
            if(file == null) Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();

            pdfView.fromFile(file)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .enableAntialiasing(true)
                    .load();
        }
    }


}