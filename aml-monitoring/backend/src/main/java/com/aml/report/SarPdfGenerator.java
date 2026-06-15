package com.aml.report;

import com.aml.model.Alert;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class SarPdfGenerator {

    private static final String OUTPUT_DIR = "sar-reports";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String generate(Alert alert) {
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
            String path = OUTPUT_DIR + "/SAR-" + alert.getId() + ".pdf";

            try (PdfDocument pdf = new PdfDocument(new PdfWriter(path));
                 Document doc = new Document(pdf)) {

                Paragraph title = new Paragraph("SUSPICIOUS ACTIVITY REPORT (SAR)").setBold().setFontSize(18);
                title.setMarginBottom(4);
                doc.add(title);
                doc.add(new Paragraph("AML Transaction Monitoring System").setFontSize(10).setItalic().setMarginBottom(20));

                doc.add(new Paragraph("Report Details").setBold().setFontSize(13).setMarginBottom(5));
                Table meta = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();
                addRow(meta, "Alert ID", alert.getId().toString());
                addRow(meta, "Risk Score", String.valueOf(alert.getRiskScoreSnapshot()));
                addRow(meta, "Status", alert.getStatus());
                addRow(meta, "Filed At", LocalDateTime.now().format(FMT));
                if (alert.getAnalystNote() != null && !alert.getAnalystNote().isBlank())
                    addRow(meta, "Analyst Note", alert.getAnalystNote());
                doc.add(meta.setMarginBottom(20));

                var tx = alert.getTransaction();
                if (tx != null) {
                    doc.add(new Paragraph("Transaction Details").setBold().setFontSize(13).setMarginBottom(5));
                    Table txTable = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();
                    addRow(txTable, "Transaction ID", tx.getId().toString());
                    addRow(txTable, "Sender", tx.getSenderAccount() + " (" + tx.getSenderCountry() + ")");
                    addRow(txTable, "Receiver", tx.getReceiverAccount() + " (" + tx.getReceiverCountry() + ")");
                    addRow(txTable, "Amount", tx.getAmount() + " " + tx.getCurrency());
                    addRow(txTable, "Timestamp", tx.getTimestamp().format(FMT));
                    doc.add(txTable.setMarginBottom(20));

                    doc.add(new Paragraph("Triggered Rules").setBold().setFontSize(13).setMarginBottom(5));
                    doc.add(new Paragraph(tx.getFiredRules()).setFontSize(9).setMarginBottom(20));
                }

                doc.add(new Paragraph("Generated automatically by the AML Monitoring System.").setItalic().setFontSize(9));
            }

            return path;
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate SAR PDF for alert " + alert.getId(), e);
        }
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold().setFontSize(10)));
        table.addCell(new Cell().add(new Paragraph(value).setFontSize(10)));
    }
}
