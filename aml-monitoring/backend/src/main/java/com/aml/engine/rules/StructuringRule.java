package com.aml.engine.rules;

import com.aml.engine.FraudRule;
import com.aml.engine.RuleResult;
import com.aml.model.Transaction;
import com.aml.repository.TransactionRepository;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class StructuringRule implements FraudRule {

    private static final BigDecimal LOW       = new BigDecimal("8000");
    private static final BigDecimal HIGH      = new BigDecimal("10000");
    private static final int        THRESHOLD = 5;

    private final TransactionRepository transactionRepository;

    public StructuringRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public RuleResult evaluate(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        if (amount.compareTo(LOW) < 0 || amount.compareTo(HIGH) > 0) {
            return new RuleResult("StructuringRule", 0, null);
        }

        LocalDateTime sevenDaysAgo = transaction.getTimestamp().minusDays(7);
        List<Transaction> recent = transactionRepository.findBySenderAccountAndTimestampAfter(
                transaction.getSenderAccount(), sevenDaysAgo);

        long nearThreshold = recent.stream()
                .filter(t -> t.getAmount().compareTo(LOW) >= 0 && t.getAmount().compareTo(HIGH) <= 0)
                .count();

        // +1 for the current (not yet persisted) transaction
        if (nearThreshold + 1 >= THRESHOLD) {
            return new RuleResult("StructuringRule", 35,
                    "Structuring pattern: " + (nearThreshold + 1) + " transactions near €10k limit in 7 days");
        }
        return new RuleResult("StructuringRule", 0, null);
    }
}
