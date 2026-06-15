package com.aml.service;

import com.aml.model.Alert;
import com.aml.model.Transaction;
import com.aml.report.SarPdfGenerator;
import com.aml.repository.AuditLogRepository;
import com.aml.repository.SarReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SarServiceTest {

    @Mock SarReportRepository sarReportRepository;
    @Mock AuditLogRepository auditLogRepository;
    @Mock SarPdfGenerator sarPdfGenerator;

    private SarService sarService;

    @BeforeEach
    void setUp() {
        sarService = new SarService(sarReportRepository, auditLogRepository, sarPdfGenerator);
    }

    @Test
    void file_report_saves_sar_and_writes_audit_log() {
        Alert alert = buildAlert();
        when(sarPdfGenerator.generate(alert)).thenReturn("sar-reports/SAR-test.pdf");
        when(sarReportRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        sarService.fileReport(alert, "admin");

        verify(sarPdfGenerator).generate(alert);
        verify(sarReportRepository).save(any());
        verify(auditLogRepository).save(any());
    }

    @Test
    void get_report_pdf_throws_404_when_not_found() {
        UUID id = UUID.randomUUID();
        when(sarReportRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> sarService.getReportPdf(id));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    private Alert buildAlert() {
        Transaction tx = new Transaction();
        tx.setSenderAccount("ACC-001");
        tx.setSenderCountry("LU");
        tx.setReceiverAccount("ACC-002");
        tx.setReceiverCountry("RU");
        tx.setReceiverLastActive(LocalDate.now().minusYears(3));
        tx.setAmount(new BigDecimal("12000"));
        tx.setCurrency("EUR");
        tx.setTimestamp(LocalDateTime.now());
        tx.setFiredRules("[]");
        tx.setStatus("FLAGGED");

        Alert alert = new Alert();
        alert.setTransaction(tx);
        alert.setRiskScoreSnapshot(75);
        alert.setStatus("ESCALATED");
        alert.setUpdatedAt(LocalDateTime.now());
        return alert;
    }
}
