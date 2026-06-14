package com.aml.engine.rules;

import com.aml.engine.FraudRule;
import com.aml.engine.RuleResult;
import com.aml.model.Transaction;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class HighRiskCountryRule implements FraudRule {

    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of(
            "IR", "KP", "RU", "BY", "SY", "CU", "VE", "MM", "SD"
    );

    @Override
    public RuleResult evaluate(Transaction transaction) {
        String country = transaction.getReceiverCountry();
        if (HIGH_RISK_COUNTRIES.contains(country)) {
            return new RuleResult("HighRiskCountryRule", 35,
                    "Transfer to high-risk/sanctioned country: " + country);
        }
        return new RuleResult("HighRiskCountryRule", 0, null);
    }
}
