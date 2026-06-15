package com.aml.service;

import com.aml.dto.AlertResponse;
import com.aml.model.Alert;
import com.aml.model.Transaction;
import com.aml.repository.AlertRepository;
import com.aml.repository.AuditLogRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock AlertRepository alertRepository;
    @Mock AuditLogRepository auditLogRepository;
    @Mock SarService sarService;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(alertRepository, auditLogRepository, sarService);
    }

    @Test
    void dismiss_throws_400_when_note_is_blank() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> alertService.dismiss(UUID.randomUUID(), "", "analyst"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void dismiss_throws_400_when_note_is_null() {
        assertThrows(ResponseStatusException.class,
                () -> alertService.dismiss(UUID.randomUUID(), null, "analyst"));
    }

    @Test
    void dismiss_throws_404_when_alert_not_found() {
        UUID id = UUID.randomUUID();
        when(alertRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> alertService.dismiss(id, "looks fine", "analyst"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void dismiss_sets_status_and_note_and_writes_audit_log() {
        UUID id = UUID.randomUUID();
        Alert alert = buildAlert(id, "OPEN");
        when(alertRepository.findById(id)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any())).thenReturn(alert);

        alertService.dismiss(id, "no suspicious pattern", "analyst");

        assertEquals("DISMISSED", alert.getStatus());
        assertEquals("no suspicious pattern", alert.getAnalystNote());
        verify(auditLogRepository).save(any());
    }

    @Test
    void escalate_throws_409_on_dismissed_alert() {
        UUID id = UUID.randomUUID();
        when(alertRepository.findById(id)).thenReturn(Optional.of(buildAlert(id, "DISMISSED")));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> alertService.escalate(id, "admin"));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(sarService, never()).fileReport(any(), any());
    }

    @Test
    void escalate_sets_status_calls_sar_and_logs() {
        UUID id = UUID.randomUUID();
        Alert alert = buildAlert(id, "OPEN");
        when(alertRepository.findById(id)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any())).thenReturn(alert);

        alertService.escalate(id, "admin");

        assertEquals("ESCALATED", alert.getStatus());
        verify(sarService).fileReport(alert, "admin");
        verify(auditLogRepository).save(any());
    }

    @Test
    void get_all_returns_filtered_by_status() {
        Alert open = buildAlert(UUID.randomUUID(), "OPEN");
        when(alertRepository.findByStatus("OPEN")).thenReturn(List.of(open));

        List<AlertResponse> result = alertService.getAll("OPEN");

        assertEquals(1, result.size());
        assertEquals("OPEN", result.get(0).status());
        verify(alertRepository).findByStatus("OPEN");
    }

    @Test
    void get_all_returns_all_when_status_is_null() {
        when(alertRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        alertService.getAll(null);

        verify(alertRepository).findAllByOrderByCreatedAtDesc();
    }

    private Alert buildAlert(UUID id, String status) {
        Transaction tx = new Transaction();
        tx.setSenderAccount("ACC-001");
        tx.setSenderCountry("LU");
        tx.setReceiverAccount("ACC-002");
        tx.setReceiverCountry("DE");
        tx.setReceiverLastActive(LocalDate.now().minusMonths(6));
        tx.setAmount(new BigDecimal("5000"));
        tx.setCurrency("EUR");
        tx.setTimestamp(LocalDateTime.now());
        tx.setFiredRules("[]");
        tx.setStatus("FLAGGED");

        Alert alert = new Alert();
        alert.setTransaction(tx);
        alert.setRiskScoreSnapshot(55);
        alert.setStatus(status);
        alert.setUpdatedAt(LocalDateTime.now());
        return alert;
    }
}
