package com.aml.engine;

import com.aml.engine.rules.DormantAccountRule;
import com.aml.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DormantAccountRuleTest {

    private final DormantAccountRule rule = new DormantAccountRule();

    @Test
    void fires_when_account_inactive_over_two_years() {
        RuleResult r = rule.evaluate(tx(LocalDate.now().minusYears(3)));
        assertTrue(r.fired());
        assertEquals(25, r.score());
        assertEquals("DormantAccountRule", r.ruleName());
    }

    @Test
    void no_fire_for_recently_active_account() {
        assertFalse(rule.evaluate(tx(LocalDate.now().minusMonths(6))).fired());
        assertFalse(rule.evaluate(tx(LocalDate.now().minusDays(1))).fired());
    }

    @Test
    void no_fire_exactly_at_two_year_boundary() {
        // isBefore(twoYearsAgo) — exactly equal is NOT before, so no fire
        assertFalse(rule.evaluate(tx(LocalDate.now().minusYears(2))).fired());
    }

    @Test
    void fires_one_day_past_two_years() {
        assertTrue(rule.evaluate(tx(LocalDate.now().minusYears(2).minusDays(1))).fired());
    }

    @Test
    void no_fire_when_last_active_is_null() {
        assertFalse(rule.evaluate(tx(null)).fired());
    }

    private Transaction tx(LocalDate lastActive) {
        Transaction t = new Transaction();
        t.setSenderAccount("ACC-001");
        t.setSenderCountry("LU");
        t.setReceiverAccount("ACC-002");
        t.setReceiverCountry("DE");
        t.setReceiverLastActive(lastActive);
        t.setAmount(new BigDecimal("1000"));
        t.setCurrency("EUR");
        t.setTimestamp(LocalDateTime.now());
        return t;
    }
}
