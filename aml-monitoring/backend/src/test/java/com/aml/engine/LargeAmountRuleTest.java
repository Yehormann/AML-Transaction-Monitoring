package com.aml.engine;

import com.aml.engine.rules.LargeAmountRule;
import com.aml.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LargeAmountRuleTest {

    private final LargeAmountRule rule = new LargeAmountRule();

    @Test
    void fires_when_amount_exceeds_threshold() {
        RuleResult r = rule.evaluate(tx("10001"));
        assertTrue(r.fired());
        assertEquals(40, r.score());
        assertEquals("LargeAmountRule", r.ruleName());
    }

    @Test
    void no_fire_exactly_at_threshold() {
        // 10000 is not strictly greater than 10000
        assertFalse(rule.evaluate(tx("10000")).fired());
    }

    @Test
    void no_fire_for_small_amount() {
        assertFalse(rule.evaluate(tx("500")).fired());
        assertFalse(rule.evaluate(tx("1")).fired());
    }

    @Test
    void fires_for_very_large_amount() {
        assertTrue(rule.evaluate(tx("999999")).fired());
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
