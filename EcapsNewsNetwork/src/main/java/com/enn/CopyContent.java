package com.enn;

import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class CopyContent {
    // Copies given text to system clipboard
    public static void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
    public static void saveToPDF(String text, String filePath) {
        Document document = new Document();
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();  // Creates the directory if it doesn't exist

            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            document.add(new Paragraph(text));
            System.out.println("PDF saved to: " + filePath);
        } catch (Exception e) {
            System.err.println("Error saving PDF: " + e.getMessage());
        } finally {
            document.close();


        }
    }
}

