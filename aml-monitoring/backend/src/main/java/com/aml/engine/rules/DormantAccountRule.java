package com.aml.engine.rules;

import com.aml.engine.FraudRule;
import com.aml.engine.RuleResult;
import com.aml.model.Transaction;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class DormantAccountRule implements FraudRule {

    @Override
    public RuleResult evaluate(Transaction transaction) {
        LocalDate lastActive = transaction.getReceiverLastActive();
        if (lastActive == null) {
            return new RuleResult("DormantAccountRule", 0, null);
        }

        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);
        if (lastActive.isBefore(twoYearsAgo)) {
            return new RuleResult("DormantAccountRule", 25,
                    "Receiver account dormant since " + lastActive);
        }
        return new RuleResult("DormantAccountRule", 0, null);
    }
}
