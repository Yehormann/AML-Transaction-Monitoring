package com.aml.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.aml.model.Alert;
import com.aml.model.AuditLog;
import com.aml.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

@Component
public class SarPdfGenerator {

    private static final String OUTPUT_DIR = "sar-reports";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public SarPdfGenerator(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public String generate(Alert alert) {
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
            String path = OUTPUT_DIR + "/SAR-" + alert.getId() + ".pdf";

            try (PdfDocument pdf = new PdfDocument(new PdfWriter(path));
                 Document doc = new Document(pdf)) {

                doc.add(new Paragraph("SUSPICIOUS ACTIVITY REPORT (SAR)")
                        .setBold().setFontSize(18).setMarginBottom(4));
                doc.add(new Paragraph("AML Transaction Monitoring System")
                        .setFontSize(10).setItalic().setMarginBottom(20));

                doc.add(section("Report Details"));
                Table meta = twoCol();
                row(meta, "Alert ID", alert.getId().toString());
                row(meta, "Risk Score", alert.getRiskScoreSnapshot() + " / 100");
                row(meta, "Alert Status", alert.getStatus());
                row(meta, "Filed At", LocalDateTime.now().format(FMT));
                if (alert.getAnalystNote() != null && !alert.getAnalystNote().isBlank())
                    row(meta, "Analyst Note", alert.getAnalystNote());
                doc.add(meta.setMarginBottom(20));

                var tx = alert.getTransaction();
                if (tx != null) {
                    doc.add(section("Transaction Details"));
                    Table txTable = twoCol();
                    row(txTable, "Transaction ID", tx.getId().toString());
                    row(txTable, "Sender", tx.getSenderAccount() + "  (" + tx.getSenderCountry() + ")");
                    row(txTable, "Receiver", tx.getReceiverAccount() + "  (" + tx.getReceiverCountry() + ")");
                    row(txTable, "Amount", tx.getAmount() + " " + tx.getCurrency());
                    row(txTable, "Timestamp", tx.getTimestamp().format(FMT));
                    doc.add(txTable.setMarginBottom(20));

                    doc.add(section("Triggered Rules"));
                    List<Map<String, Object>> rules = parseFiredRules(tx.getFiredRules());
                    if (rules.isEmpty()) {
                        doc.add(new Paragraph("No rules fired.").setFontSize(10).setMarginBottom(20));
                    } else {
                        Table rulesTable = new Table(UnitValue.createPercentArray(new float[]{35, 10, 55}))
                                .useAllAvailableWidth();
                        headerCell(rulesTable, "Rule");
                        headerCell(rulesTable, "Score");
                        headerCell(rulesTable, "Reason");
                        for (Map<String, Object> rule : rules) {
                            rulesTable.addCell(cell(String.valueOf(rule.getOrDefault("ruleName", ""))));
                            rulesTable.addCell(cell("+" + rule.getOrDefault("score", 0)));
                            rulesTable.addCell(cell(String.valueOf(rule.getOrDefault("reason", ""))));
                        }
                        doc.add(rulesTable.setMarginBottom(20));
                    }
                }

                doc.add(section("Audit Trail"));
                List<AuditLog> entries = auditLogRepository.findByEntityIdOrderByTimestampAsc(alert.getId());
                if (entries.isEmpty()) {
                    doc.add(new Paragraph("Automatically escalated by the system (score > 75).")
                            .setFontSize(10).setMarginBottom(20));
                } else {
                    Table auditTable = new Table(UnitValue.createPercentArray(new float[]{25, 20, 20, 35}))
                            .useAllAvailableWidth();
                    headerCell(auditTable, "Timestamp");
                    headerCell(auditTable, "Action");
                    headerCell(auditTable, "Performed By");
                    headerCell(auditTable, "Note");
                    for (AuditLog entry : entries) {
                        auditTable.addCell(cell(entry.getTimestamp().format(FMT)));
                        auditTable.addCell(cell(entry.getAction()));
                        auditTable.addCell(cell(entry.getPerformedBy()));
                        auditTable.addCell(cell(entry.getNote() != null ? entry.getNote() : "—"));
                    }
                    doc.add(auditTable.setMarginBottom(20));
                }

                doc.add(new Paragraph("Generated automatically by the AML Monitoring System.")
                        .setItalic().setFontSize(9));
            }

            return path;
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate SAR PDF for alert " + alert.getId(), e);
        }
    }

    private List<Map<String, Object>> parseFiredRules(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private Paragraph section(String title) {
        return new Paragraph(title).setBold().setFontSize(13).setMarginBottom(5);
    }

    private Table twoCol() {
        return new Table(UnitValue.createPercentArray(new float[]{35, 65})).useAllAvailableWidth();
    }

    private void row(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold().setFontSize(10)));
        table.addCell(new Cell().add(new Paragraph(value).setFontSize(10)));
    }

    private void headerCell(Table table, String text) {
        table.addHeaderCell(new Cell()
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .add(new Paragraph(text).setBold().setFontSize(10)));
    }

    private Cell cell(String text) {
        return new Cell().add(new Paragraph(text).setFontSize(9));
    }
}
