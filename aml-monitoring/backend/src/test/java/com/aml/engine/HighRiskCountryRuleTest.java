package com.aml.engine;

import com.aml.engine.rules.HighRiskCountryRule;
import com.aml.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HighRiskCountryRuleTest {

    private final HighRiskCountryRule rule = new HighRiskCountryRule();

    @ParameterizedTest
    @ValueSource(strings = {"IR", "KP", "RU", "BY", "SY", "CU", "VE", "MM", "SD"})
    void fires_for_all_sanctioned_countries(String country) {
        RuleResult r = rule.evaluate(tx(country));
        assertTrue(r.fired());
        assertEquals(35, r.score());
    }

    @Test
    void no_fire_for_safe_countries() {
        assertFalse(rule.evaluate(tx("LU")).fired());
        assertFalse(rule.evaluate(tx("DE")).fired());
        assertFalse(rule.evaluate(tx("US")).fired());
        assertFalse(rule.evaluate(tx("FR")).fired());
    }

    @Test
    void rule_name_is_correct() {
        assertEquals("HighRiskCountryRule", rule.evaluate(tx("RU")).ruleName());
    }

    private Transaction tx(String receiverCountry) {
        Transaction t = new Transaction();
        t.setSenderAccount("ACC-001");
        t.setSenderCountry("LU");
        t.setReceiverAccount("ACC-002");
        t.setReceiverCountry(receiverCountry);
        t.setReceiverLastActive(LocalDate.now().minusMonths(3));
        t.setAmount(new BigDecimal("500"));
        t.setCurrency("EUR");
        t.setTimestamp(LocalDateTime.now());
        return t;
    }
}
