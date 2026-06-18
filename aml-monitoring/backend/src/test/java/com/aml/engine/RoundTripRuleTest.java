package com.aml.engine;

import com.aml.engine.rules.RoundTripRule;
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
class RoundTripRuleTest {

    @Mock TransactionRepository transactionRepository;
    @InjectMocks RoundTripRule rule;

    @Test
    void fires_when_same_round_amount_seen_before() {
        when(transactionRepository.findBySenderAccountAndAmountAndTimestampAfter(anyString(), any(), any()))
                .thenReturn(List.of(tx("1000")));

        RuleResult r = rule.evaluate(tx("1000"));
        assertTrue(r.fired());
        assertEquals(20, r.score());
        assertEquals("RoundTripRule", r.ruleName());
    }

    @Test
    void no_fire_for_fractional_amount() {
        // 1000.50 has cents — not a round amount
        assertFalse(rule.evaluate(tx("1000.50")).fired());
    }

    @Test
    void no_fire_when_amount_has_non_zero_cents() {
        assertFalse(rule.evaluate(tx("500.01")).fired());
    }

    @Test
    void no_fire_when_first_occurrence_of_round_amount() {
        when(transactionRepository.findBySenderAccountAndAmountAndTimestampAfter(anyString(), any(), any()))
                .thenReturn(Collections.emptyList());

        assertFalse(rule.evaluate(tx("500")).fired());
    }

    @Test
    void fires_for_round_amount_with_trailing_zeros() {
        // 1000.00 → stripTrailingZeros → 1E+3, scale <= 0, so it IS round
        when(transactionRepository.findBySenderAccountAndAmountAndTimestampAfter(anyString(), any(), any()))
                .thenReturn(List.of(tx("1000.00")));

        assertTrue(rule.evaluate(tx("1000.00")).fired());
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
