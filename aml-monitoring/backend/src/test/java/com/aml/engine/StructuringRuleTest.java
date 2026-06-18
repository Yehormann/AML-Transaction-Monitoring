package com.aml.engine;

import com.aml.engine.rules.StructuringRule;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StructuringRuleTest {

    @Mock TransactionRepository transactionRepository;
    @InjectMocks StructuringRule rule;

    @Test
    void fires_when_enough_near_threshold_transactions_in_window() {
        // 4 existing + 1 current = 5, which hits the threshold
        List<Transaction> history = List.of(tx("9000"), tx("8500"), tx("9200"), tx("8800"));
        when(transactionRepository.findBySenderAccountAndTimestampAfter(anyString(), any())).thenReturn(history);

        RuleResult r = rule.evaluate(tx("9500"));
        assertTrue(r.fired());
        assertEquals(35, r.score());
    }

    @Test
    void no_fire_when_amount_below_range() {
        assertFalse(rule.evaluate(tx("7999")).fired());
    }

    @Test
    void no_fire_when_amount_above_range() {
        assertFalse(rule.evaluate(tx("10001")).fired());
    }

    @Test
    void no_fire_when_history_count_is_below_threshold() {
        // 3 existing + 1 current = 4, still below 5
        List<Transaction> history = List.of(tx("9000"), tx("8500"), tx("9200"));
        when(transactionRepository.findBySenderAccountAndTimestampAfter(anyString(), any())).thenReturn(history);

        assertFalse(rule.evaluate(tx("9500")).fired());
    }

    @Test
    void no_fire_when_history_is_outside_8k_10k_range() {
        // all history txs are small — shouldn't count toward structuring
        List<Transaction> history = List.of(tx("100"), tx("200"), tx("150"), tx("300"), tx("250"));
        when(transactionRepository.findBySenderAccountAndTimestampAfter(anyString(), any())).thenReturn(history);

        assertFalse(rule.evaluate(tx("9000")).fired());
    }

    @Test
    void no_fire_when_no_prior_history() {
        when(transactionRepository.findBySenderAccountAndTimestampAfter(anyString(), any())).thenReturn(Collections.emptyList());
        assertFalse(rule.evaluate(tx("9000")).fired());
    }

    private Transaction tx(String amount) {
        Transaction t = new Transaction();
        t.setSenderAccount("ACC-001");
        t.setSenderCountry("LU");
        t.setReceiverAccount("ACC-002");
        t.setReceiverCountry("DE");
        t.setReceiverLastActive(LocalDate.now().minusMonths(3));
        t.setAmount(new BigDecimal(amount));
        t.setCurrency("EUR");
        t.setTimestamp(LocalDateTime.now());
        return t;
    }
}
