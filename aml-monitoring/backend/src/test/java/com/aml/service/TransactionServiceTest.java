package com.aml.service;

import com.aml.dto.TransactionRequest;
import com.aml.dto.TransactionResponse;
import com.aml.engine.RuleEngineService;
import com.aml.engine.RuleResult;
import com.aml.model.Alert;
import com.aml.model.Transaction;
import com.aml.repository.AlertRepository;
import com.aml.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AlertRepository alertRepository;
    @Mock RuleEngineService ruleEngineService;
    @Mock SarService sarService;

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionService(transactionRepository, alertRepository, ruleEngineService, sarService, new ObjectMapper());
    }

    @Test
    void submit_returns_approved_when_score_is_low() {
        when(ruleEngineService.evaluate(any())).thenReturn(Collections.emptyList());
        when(ruleEngineService.totalScore(any())).thenReturn(20);
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TransactionResponse r = service.submit(buildRequest(), "analyst");

        assertEquals("APPROVED", r.status());
        assertEquals(20, r.riskScore());
        verify(alertRepository, never()).save(any());
        verify(sarService, never()).fileReport(any(), any());
    }

    @Test
    void submit_creates_alert_when_score_exceeds_threshold() {
        when(ruleEngineService.evaluate(any())).thenReturn(List.of(new RuleResult("LargeAmountRule", 40, "amount > 10k")));
        when(ruleEngineService.totalScore(any())).thenReturn(55);
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(alertRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TransactionResponse r = service.submit(buildRequest(), "analyst");

        assertEquals("FLAGGED", r.status());
        verify(alertRepository).save(any(Alert.class));
        verify(sarService, never()).fileReport(any(), any());
    }

    @Test
    void submit_auto_escalates_and_files_sar_when_score_above_75() {
        when(ruleEngineService.evaluate(any())).thenReturn(List.of(
                new RuleResult("LargeAmountRule", 40, "large"),
                new RuleResult("HighRiskCountryRule", 35, "risky country"),
                new RuleResult("StructuringRule", 35, "structuring")
        ));
        when(ruleEngineService.totalScore(any())).thenReturn(100);
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(alertRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.submit(buildRequest(), "analyst");

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, atLeast(2)).save(alertCaptor.capture());

        Alert saved = alertCaptor.getAllValues().get(alertCaptor.getAllValues().size() - 1);
        assertEquals("ESCALATED", saved.getStatus());
        verify(sarService).fileReport(any(Alert.class), eq("SYSTEM"));
    }

    @Test
    void get_all_returns_all_transactions() {
        Transaction t = new Transaction();
        t.setSenderAccount("ACC-001");
        t.setSenderCountry("LU");
        t.setReceiverAccount("ACC-002");
        t.setReceiverCountry("DE");
        t.setReceiverLastActive(LocalDate.now());
        t.setAmount(new BigDecimal("500"));
        t.setCurrency("EUR");
        t.setTimestamp(LocalDateTime.now());
        t.setFiredRules("[]");

        when(transactionRepository.findAll()).thenReturn(List.of(t));

        List<TransactionResponse> result = service.getAll();
        assertEquals(1, result.size());
        assertEquals("ACC-001", result.get(0).senderAccount());
    }

    private TransactionRequest buildRequest() {
        return new TransactionRequest(
                "ACC-001", "LU", "ACC-002", "DE",
                LocalDate.now().minusMonths(3),
                new BigDecimal("15000"), "EUR",
                LocalDateTime.now()
        );
    }
}
