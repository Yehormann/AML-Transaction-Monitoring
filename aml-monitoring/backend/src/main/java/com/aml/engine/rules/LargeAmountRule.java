package com.aml.engine.rules;

import com.aml.engine.FraudRule;
import com.aml.engine.RuleResult;
import com.aml.model.Transaction;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class LargeAmountRule implements FraudRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("10000");

    @Override
    public RuleResult evaluate(Transaction transaction) {
        if (transaction.getAmount().compareTo(THRESHOLD) > 0) {
            return new RuleResult("LargeAmountRule", 40,
                    "Transaction amount €" + transaction.getAmount() + " exceeds €10,000 threshold");
        }
        return new RuleResult("LargeAmountRule", 0, null);
    }
}
