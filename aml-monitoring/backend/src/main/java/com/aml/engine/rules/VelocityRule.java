package com.aml.engine.rules;

import com.aml.engine.FraudRule;
import com.aml.engine.RuleResult;
import com.aml.model.Transaction;
import com.aml.repository.TransactionRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class VelocityRule implements FraudRule {

    private static final int THRESHOLD = 20;

    private final TransactionRepository transactionRepository;

    public VelocityRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public RuleResult evaluate(Transaction transaction) {
        LocalDateTime twoHoursAgo = transaction.getTimestamp().minusHours(2);
        List<Transaction> recent = transactionRepository.findBySenderAccountAndTimestampAfter(
                transaction.getSenderAccount(), twoHoursAgo);

        // +1 for the current (not yet persisted) transaction
        if (recent.size() + 1 >= THRESHOLD) {
            return new RuleResult("VelocityRule", 30,
                    "High velocity: " + (recent.size() + 1) + " transactions from account in 2 hours");
        }
        return new RuleResult("VelocityRule", 0, null);
    }
}
