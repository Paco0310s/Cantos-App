package com.pacosotelo.coro;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.pacosotelo.coro.vista.Nuevo;
import com.pacosotelo.coro.vista.VerCanto;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class TemplatePDF {
    private File folder;
    private Context contexto;
    private File pdfFile;
    private Document document;
    private PdfWriter pdfWriter;
    private Paragraph paragraph;
    private Font fTitle = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD, BaseColor.RED);
    private Font fSubtitle = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private Font fText = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    private Font fHighText = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.RED);

    public TemplatePDF(Context contexto) {
        this.contexto = contexto;
    }

    public void openDocument(String nombre) {
        createFile(nombre);

        try {
            document = new Document(PageSize.A4);
            pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
        } catch(Exception e) {
            Log.e("openDocument", e.toString());
        }
    }

    private void createFile(String nombre) {
        folder = new File(Environment.getExternalStorageDirectory().toString(), "Cantos");

        if(!folder.exists()) folder.mkdir();

        pdfFile = new File(folder, nombre + ".pdf");
    }

    public void closeDocument() {
        document.close();
    }

    public void addMetaData(String title, String subject, String author) {
        document.addTitle(title);
        document.addSubject(subject);
        document.addAuthor(author);
    }

    public void addTitle(String title) {
        try {
            paragraph = new Paragraph();
            addChildP(new Paragraph(title, fTitle));
            paragraph.setSpacingAfter(20);
            document.add(paragraph);
        } catch (DocumentException e) {
            Log.e("addTitles",e.toString());
        }
    }

    public void addTitles(String title, String subTitle, String date) {
        try {
            paragraph = new Paragraph();
            addChildP(new Paragraph(title, fTitle));
            addChildP(new Paragraph(subTitle, fSubtitle));
            addChildP(new Paragraph("Generado: " + date, fHighText));
            paragraph.setSpacingAfter(30);
            document.add(paragraph);
        } catch (DocumentException e) {
            Log.e("addTitles",e.toString());
        }
    }

    private void addChildP(Paragraph childParagraph) {
        childParagraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(childParagraph);
    }

    public void addParagraph(String text) {
        try {
            paragraph = new Paragraph(text, fText);
            paragraph.setSpacingAfter(5);
            paragraph.setSpacingBefore(5);
            document.add(paragraph);
        } catch (DocumentException e) {
            Log.e("addParagraph",e.toString());
        }

    }

    public void createTable(String[] header, ArrayList<String[]> clients) {
        try {
            paragraph = new Paragraph();
            paragraph.setFont(fText);
            PdfPTable pdfPTable = new PdfPTable(header.length);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.setSpacingBefore(20);
            PdfPCell pdfPCell;

            int iColumn = 0;

            while (iColumn < header.length) {
                pdfPCell = new PdfPCell(new Phrase(header[iColumn++], fSubtitle));
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(BaseColor.GREEN);
                pdfPTable.addCell(pdfPCell);
            }

            for (int iRow = 0; iRow < clients.size(); iRow++) {
                String[] row = clients.get(iRow);
                for (iColumn = 0; iColumn < header.length; iColumn++) {
                    pdfPCell = new PdfPCell(new Phrase(row[iColumn]));
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setFixedHeight(40);
                    pdfPTable.addCell(pdfPCell);
                }
            }

            paragraph.add(pdfPTable);
            document.add(paragraph);

        } catch (DocumentException e) {
            Log.e("createTable",e.toString());
        }
    }

    public void addImgName (String imageName) {
        try{
            Image image = Image.getInstance(folder.getAbsolutePath() + File.separator + imageName + ".jpg");
            image.setSpacingBefore(5);
            image.setSpacingAfter(5);
            image.scaleToFit(400,400);
            image.setAlignment(Element.ALIGN_CENTER);
            document.add(image);
        }catch (Exception e){
            Log.e("addImgName ", e.toString());
        }
    }

    public void viewPDF() {
        Intent i = new Intent(contexto, ViewPDFActivity.class);
        i.putExtra("path", pdfFile.getAbsolutePath());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        contexto.startActivity(i);
    }

    public void appViewPDF(Activity activity) {
        if(pdfFile.exists()) {
            Uri uri = Uri.fromFile(pdfFile);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri, "application/pdf");

            try {
                activity.startActivity(i);
            } catch(ActivityNotFoundException e) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.adobe.reader")));
                Toast.makeText(activity.getApplicationContext(), "Instala un visualizador de PDF", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(activity.getApplicationContext(), "No se encontró el archivo", Toast.LENGTH_SHORT).show();
        }
    }
}
