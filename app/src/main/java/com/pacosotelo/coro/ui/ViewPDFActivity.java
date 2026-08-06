package com.pacosotelo.coro.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.pacosotelo.coro.R;

import java.io.File;

public class ViewPDFActivity extends AppCompatActivity {

    private PDFView pdfView;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Uncomment when androidx.edge:edge is available
        setContentView(R.layout.activity_viewpdf);

        pdfView = findViewById(R.id.pdfView);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            file = new File(bundle.getString("path",""));
//            if(file == null) Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();

            pdfView.fromFile(file)
                    .enableSwipe(true)
//                    .swipeHorizontal(false)
                    .enableDoubletap(true)
//                    .enableAntialiasing(true)
                    .load();
        }
    }


}