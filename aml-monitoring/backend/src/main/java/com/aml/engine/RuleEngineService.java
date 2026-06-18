package com.aml.engine;

import com.aml.model.Transaction;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RuleEngineService {

    private final List<FraudRule> rules;

    public RuleEngineService(List<FraudRule> rules) {
        this.rules = rules;
    }

    public List<RuleResult> evaluate(Transaction transaction) {
        return rules.stream()
                .map(rule -> rule.evaluate(transaction))
                .filter(RuleResult::fired)
                .toList();
    }

    public int totalScore(List<RuleResult> results) {
        return Math.min(100, results.stream().mapToInt(RuleResult::score).sum());
    }
}
