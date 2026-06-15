package com.aml;

import com.aml.model.Alert;
import com.aml.model.Transaction;
import com.aml.report.SarPdfGenerator;
import com.aml.repository.AlertRepository;
import com.aml.repository.AuditLogRepository;
import com.aml.repository.SarReportRepository;
import com.aml.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlertIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired TransactionRepository transactionRepository;
    @Autowired AlertRepository alertRepository;
    @Autowired SarReportRepository sarReportRepository;
    @Autowired AuditLogRepository auditLogRepository;

    @MockBean SarPdfGenerator sarPdfGenerator;

    private UUID alertId;

    @BeforeEach
    void setup() {
        sarReportRepository.deleteAll();
        auditLogRepository.deleteAll();
        alertRepository.deleteAll();
        transactionRepository.deleteAll();

        when(sarPdfGenerator.generate(any())).thenReturn("sar-reports/test.pdf");

        Transaction tx = new Transaction();
        tx.setSenderAccount("ACC-001");
        tx.setSenderCountry("LU");
        tx.setReceiverAccount("ACC-002");
        tx.setReceiverCountry("LU");
        tx.setReceiverLastActive(LocalDate.now().minusMonths(6));
        tx.setAmount(new BigDecimal("15000"));
        tx.setCurrency("EUR");
        tx.setTimestamp(LocalDateTime.now());
        tx.setRiskScore(65);
        tx.setStatus("FLAGGED");
        tx.setFiredRules("[]");
        tx = transactionRepository.save(tx);

        Alert alert = new Alert();
        alert.setTransaction(tx);
        alert.setRiskScoreSnapshot(65);
        alert.setStatus("OPEN");
        alert.setUpdatedAt(LocalDateTime.now());
        alert = alertRepository.save(alert);
        alertId = alert.getId();
    }

    @Test
    void get_alerts_returns_list() throws Exception {
        mvc.perform(get("/api/alerts").with(httpBasic("analyst", "analyst")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void get_alerts_filtered_by_status() throws Exception {
        mvc.perform(get("/api/alerts?status=OPEN").with(httpBasic("analyst", "analyst")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(get("/api/alerts?status=DISMISSED").with(httpBasic("analyst", "analyst")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void dismiss_without_note_returns_400() throws Exception {
        mvc.perform(patch("/api/alerts/" + alertId + "/dismiss")
                .with(httpBasic("analyst", "analyst"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dismiss_with_note_changes_status_and_writes_audit_log() throws Exception {
        mvc.perform(patch("/api/alerts/" + alertId + "/dismiss")
                .with(httpBasic("analyst", "analyst"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\": \"reviewed, no issue\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISMISSED"));

        assertEquals("DISMISSED", alertRepository.findById(alertId).get().getStatus());
        assertEquals(1L, auditLogRepository.count());
    }

    @Test
    void escalate_open_alert_creates_sar_and_logs_audit() throws Exception {
        mvc.perform(patch("/api/alerts/" + alertId + "/escalate")
                .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ESCALATED"));

        assertEquals("ESCALATED", alertRepository.findById(alertId).get().getStatus());
        assertEquals(1L, sarReportRepository.count());
        assertEquals(2L, auditLogRepository.count()); // ALERT_ESCALATED + SAR_FILED
    }

    @Test
    void escalate_dismissed_alert_returns_409() throws Exception {
        // first dismiss
        mvc.perform(patch("/api/alerts/" + alertId + "/dismiss")
                .with(httpBasic("analyst", "analyst"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\": \"closed\"}"));

        // then try to escalate
        mvc.perform(patch("/api/alerts/" + alertId + "/escalate")
                .with(httpBasic("admin", "admin")))
                .andExpect(status().isConflict());
    }

    @Test
    void unauthenticated_request_is_rejected() throws Exception {
        mvc.perform(get("/api/alerts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dashboard_stats_reflect_seeded_data() throws Exception {
        mvc.perform(get("/api/dashboard/stats").with(httpBasic("analyst", "analyst")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").value(1))
                .andExpect(jsonPath("$.flaggedTransactions").value(1))
                .andExpect(jsonPath("$.openAlerts").value(1));
    }
}
