package com.aml;

import com.aml.model.Alert;
import com.aml.report.SarPdfGenerator;
import com.aml.repository.AlertRepository;
import com.aml.repository.SarReportRepository;
import com.aml.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("rawtypes")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TransactionIntegrationTest {

    @Autowired TestRestTemplate http;
    @Autowired TransactionRepository transactionRepository;
    @Autowired AlertRepository alertRepository;
    @Autowired SarReportRepository sarReportRepository;

    // mock only the PDF file I/O — we test everything else for real
    @MockBean SarPdfGenerator sarPdfGenerator;

    @BeforeEach
    void clean() {
        sarReportRepository.deleteAll();
        alertRepository.deleteAll();
        transactionRepository.deleteAll();
        when(sarPdfGenerator.generate(any())).thenReturn("sar-reports/test.pdf");
    }

    @Test
    void unauthenticated_request_is_rejected() {
        ResponseEntity<Void> resp = http.postForEntity("/api/transactions", txBody("500", "LU", "2025-06-01"), Void.class);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void low_risk_transaction_is_approved_and_no_alert_created() {
        // amount 500, safe country, active receiver → score 0
        ResponseEntity<Map> resp = post(txBody("500", "LU", "2025-06-01"));

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals("APPROVED", resp.getBody().get("status"));
        assertEquals(0, ((Number) resp.getBody().get("riskScore")).intValue());
        assertEquals(0L, alertRepository.count());
    }

    @Test
    void large_amount_with_dormant_receiver_creates_open_alert() {
        // LargeAmountRule(+40) + DormantAccountRule(+25) = 65 → FLAGGED, alert OPEN
        ResponseEntity<Map> resp = post(txBody("15000", "LU", "2023-01-01"));

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals("FLAGGED", resp.getBody().get("status"));
        assertEquals(65, ((Number) resp.getBody().get("riskScore")).intValue());

        assertEquals(1L, alertRepository.count());
        Alert alert = alertRepository.findAll().get(0);
        assertEquals("OPEN", alert.getStatus());
        assertEquals(65, alert.getRiskScoreSnapshot());
    }

    @Test
    void very_high_risk_transaction_auto_escalates_and_files_sar() {
        // LargeAmount(+40) + HighRiskCountry(+35) + Dormant(+25) = 100 → auto-escalated
        ResponseEntity<Map> resp = post(txBody("15000", "RU", "2023-01-01"));

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals("FLAGGED", resp.getBody().get("status"));

        assertEquals(1L, alertRepository.count());
        assertEquals("ESCALATED", alertRepository.findAll().get(0).getStatus());
        assertEquals(1L, sarReportRepository.count());
    }

    @Test
    void fired_rules_are_persisted_in_transaction_record() {
        post(txBody("15000", "LU", "2025-06-01")); // only LargeAmountRule fires

        String firedRules = transactionRepository.findAll().get(0).getFiredRules();
        assertTrue(firedRules.contains("LargeAmountRule"));
    }

    @Test
    void get_all_transactions_returns_saved_records() {
        post(txBody("500", "LU", "2025-06-01"));
        post(txBody("600", "DE", "2025-06-01"));

        ResponseEntity<Object[]> resp = http.withBasicAuth("analyst", "analyst")
                .getForEntity("/api/transactions", Object[].class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(2, resp.getBody().length);
    }

    private ResponseEntity<Map> post(Map<String, Object> body) {
        return http.withBasicAuth("analyst", "analyst")
                .postForEntity("/api/transactions", body, Map.class);
    }

    private Map<String, Object> txBody(String amount, String receiverCountry, String lastActive) {
        Map<String, Object> body = new HashMap<>();
        body.put("senderAccount", "ACC-SENDER");
        body.put("senderCountry", "LU");
        body.put("receiverAccount", "ACC-RECEIVER");
        body.put("receiverCountry", receiverCountry);
        body.put("receiverLastActive", lastActive);
        body.put("amount", new BigDecimal(amount));
        body.put("currency", "EUR");
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }
}
