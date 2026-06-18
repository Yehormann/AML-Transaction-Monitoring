package com.aml.engine;

import com.aml.engine.rules.VelocityRule;
import com.aml.model.Transaction;
import com.aml.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VelocityRuleTest {

    @Mock TransactionRepository transactionRepository;
    @InjectMocks VelocityRule rule;

    @Test
    void fires_when_19_existing_plus_current_hit_threshold() {
        // threshold is 20 — 19 in DB + 1 current = 20
        List<Transaction> history = nTxs(19);
        when(transactionRepository.findBySenderAccountAndTimestampAfter(anyString(), any())).thenReturn(history);

        RuleResult r = rule.evaluate(tx());
        assertTrue(r.fired());
        assertEquals(30, r.score());
    }

    @Test
    void no_fire_when_18_existing_plus_current_is_under_threshold() {
        List<Transaction> history = nTxs(18);
        when(transactionRepository.findBySenderAccountAndTimestampAfter(anyString(), any())).thenReturn(history);

        assertFalse(rule.evaluate(tx()).fired());
    }

    @Test
    void no_fire_when_no_history() {
        when(transactionRepository.findBySenderAccountAndTimestampAfter(anyString(), any())).thenReturn(Collections.emptyList());
        assertFalse(rule.evaluate(tx()).fired());
    }

    @Test
    void fires_well_above_threshold() {
        when(transactionRepository.findBySenderAccountAndTimestampAfter(anyString(), any())).thenReturn(nTxs(50));
        assertTrue(rule.evaluate(tx()).fired());
    }

    private List<Transaction> nTxs(int n) {
        return IntStream.range(0, n).mapToObj(i -> tx()).collect(Collectors.toList());
    }

    private Transaction tx() {
        Transaction t = new Transaction();
        t.setSenderAccount("ACC-001");
        t.setSenderCountry("LU");
        t.setReceiverAccount("ACC-002");
        t.setReceiverCountry("DE");
        t.setReceiverLastActive(LocalDate.now().minusMonths(3));
        t.setAmount(new BigDecimal("100"));
        t.setCurrency("EUR");
        t.setTimestamp(LocalDateTime.now());
        return t;
    }
}
