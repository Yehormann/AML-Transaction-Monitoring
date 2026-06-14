package com.aml.engine.rules;

import com.aml.engine.FraudRule;
import com.aml.engine.RuleResult;
import com.aml.model.Transaction;
import com.aml.repository.TransactionRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class RoundTripRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    public RoundTripRule(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public RuleResult evaluate(Transaction transaction) {
        // Only flag round amounts (no cents)
        if (transaction.getAmount().stripTrailingZeros().scale() > 0) {
            return new RuleResult("RoundTripRule", 0, null);
        }

        LocalDateTime thirtyDaysAgo = transaction.getTimestamp().minusDays(30);
        List<Transaction> sameAmount = transactionRepository.findBySenderAccountAndAmountAndTimestampAfter(
                transaction.getSenderAccount(), transaction.getAmount(), thirtyDaysAgo);

        if (!sameAmount.isEmpty()) {
            return new RuleResult("RoundTripRule", 20,
                    "Round-trip pattern: identical round amount sent " + (sameAmount.size() + 1) + " times in 30 days");
        }
        return new RuleResult("RoundTripRule", 0, null);
    }
}
