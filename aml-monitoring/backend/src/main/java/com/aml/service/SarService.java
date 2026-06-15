package com.aml.service;

import com.aml.model.Alert;
import com.aml.model.AuditLog;
import com.aml.model.SarReport;
import com.aml.report.SarPdfGenerator;
import com.aml.repository.AuditLogRepository;
import com.aml.repository.SarReportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Transactional
public class SarService {

    private final SarReportRepository sarReportRepository;
    private final AuditLogRepository auditLogRepository;
    private final SarPdfGenerator sarPdfGenerator;

    public SarService(SarReportRepository sarReportRepository, AuditLogRepository auditLogRepository, SarPdfGenerator sarPdfGenerator) {
        this.sarReportRepository = sarReportRepository;
        this.auditLogRepository = auditLogRepository;
        this.sarPdfGenerator = sarPdfGenerator;
    }

    public void fileReport(Alert alert, String performedBy) {
        String pdfPath = sarPdfGenerator.generate(alert);

        SarReport report = new SarReport();
        report.setAlert(alert);
        report.setPdfPath(pdfPath);
        sarReportRepository.save(report);

        String logNote = "SAR report generated: " + pdfPath;
        auditLogRepository.save(new AuditLog("SAR", alert.getId(), "SAR_FILED", performedBy, logNote));
    }

    @Transactional(readOnly = true)
    public byte[] getReportPdf(UUID reportId) {
        SarReport report = sarReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SAR report not found"));
        try {
            return Files.readAllBytes(Paths.get(report.getPdfPath()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF file not accessible");
        }
    }
}
